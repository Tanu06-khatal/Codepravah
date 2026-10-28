package gov.aicte.apaar.controller;

import gov.aicte.apaar.model.College;
import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.repository.CollegeRepository;
import gov.aicte.apaar.repository.StudentRepository;
import gov.aicte.apaar.repository.VerificationLogRepository;
import gov.aicte.apaar.service.ExcelReportService;
import gov.aicte.apaar.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aicte")
@CrossOrigin(origins = "*")
public class AicteController {

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;
    private final VerificationLogRepository logRepository;
    private final ExcelReportService excelReportService;
    private final PdfReportService pdfReportService;

    public AicteController(CollegeRepository collegeRepository,
                           StudentRepository studentRepository,
                           VerificationLogRepository logRepository,
                           ExcelReportService excelReportService,
                           PdfReportService pdfReportService) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
        this.logRepository = logRepository;
        this.excelReportService = excelReportService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/colleges")
    public ResponseEntity<?> getAllColleges() {
        List<College> list = collegeRepository.findAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "totalInstitutions", list.size(),
                "activeInstitutions", list.size(),
                "data", list
        ));
    }

    /**
     * GET /api/aicte/stats
     * Live national statistics computed from the actual database —
     * nothing is hard-coded. The state-wise chart aggregates the
     * institutional enrolment and verification records of every
     * registered college, so the visualisation reflects real registry
     * data at a proper national scale.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getNationalStats() {
        List<College> colleges = collegeRepository.findAll();
        List<Student> students = studentRepository.findAll();

        // ---- College-level aggregates (authoritative registry numbers) ----
        int sumApproved = 0, sumActual = 0, sumVerified = 0, sumPending = 0, sumDup = 0;
        int sumTfw = 0, sumNri = 0, sumFn = 0;
        int underAudit = 0;
        Map<String, Map<String, Long>> stateMap = new LinkedHashMap<>();

        for (College c : colleges) {
            int approved = nvl(c.getApprovedIntake());
            int actual = nvl(c.getActualEnrolment());
            int verified = nvl(c.getVerifiedCount());
            int pending = nvl(c.getPendingCount());
            int dup = nvl(c.getDuplicateCount());

            sumApproved += approved;
            sumActual += actual;
            sumVerified += verified;
            sumPending += pending;
            sumDup += dup;
            sumTfw += nvl(c.getSupernumeraryQuota());
            sumNri += nvl(c.getNriCount());
            sumFn += nvl(c.getFnCount());
            if (actual > approved) underAudit++;

            String state = (c.getState() == null || c.getState().isBlank()) ? "Other" : c.getState();
            Map<String, Long> agg = stateMap.computeIfAbsent(state, k -> new LinkedHashMap<>());
            agg.merge("institutions", 1L, Long::sum);
            agg.merge("approved", (long) approved, Long::sum);
            agg.merge("total", (long) actual, Long::sum);
            agg.merge("verified", (long) verified, Long::sum);
            agg.merge("pending", (long) pending, Long::sum);
            agg.merge("duplicates", (long) dup, Long::sum);
            if (actual > approved) agg.merge("underAudit", 1L, Long::sum);
        }

        List<Map<String, Object>> stateBreakdown = new ArrayList<>();
        stateMap.forEach((state, agg) -> {
            long total = agg.getOrDefault("total", 0L);
            long verified = agg.getOrDefault("verified", 0L);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("state", state);
            row.put("institutions", agg.getOrDefault("institutions", 0L));
            row.put("approved", agg.getOrDefault("approved", 0L));
            row.put("total", total);
            row.put("verified", verified);
            row.put("pending", agg.getOrDefault("pending", 0L));
            row.put("duplicates", agg.getOrDefault("duplicates", 0L));
            row.put("underAudit", agg.getOrDefault("underAudit", 0L));
            row.put("verifiedPercent", total > 0 ? Math.round((verified * 1000.0) / total) / 10.0 : 0.0);
            stateBreakdown.add(row);
        });

        // ---- Student-record level statuses (live verification workflow) ----
        long verifiedStudents = students.stream().filter(s -> "verified".equalsIgnoreCase(s.getStatus())).count();
        long pendingStudents = students.stream().filter(s -> "pending".equalsIgnoreCase(s.getStatus())).count();
        long duplicateStudents = students.stream().filter(s -> "duplicate".equalsIgnoreCase(s.getStatus())).count();
        long rejectedStudents = students.stream().filter(s -> "rejected".equalsIgnoreCase(s.getStatus())).count();
        long totalStudents = students.size();
        double studentVerifiedPercent = totalStudents > 0 ? (verifiedStudents * 100.0) / totalStudents : 0.0;

        // ---- Category distribution (statutory quotas) ----
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        categoryMap.put("general", (long) Math.max(0, sumActual - sumTfw - sumNri - sumFn));
        categoryMap.put("tfw", (long) sumTfw);
        categoryMap.put("nri", (long) sumNri);
        categoryMap.put("fn", (long) sumFn);

        // ---- Anomaly stream derived from real duplicate records and over-intake colleges ----
        List<Map<String, String>> anomalies = new ArrayList<>();
        int frdSeq = 81;
        for (Student s : students) {
            if ("duplicate".equalsIgnoreCase(s.getStatus())) {
                String other = s.getDuplicateCollegeName() != null ? s.getDuplicateCollegeName() : "another institution";
                anomalies.add(Map.of(
                        "type", "FRD-2026-" + (frdSeq++),
                        "severity", "CRITICAL",
                        "description", s.getName() + " (" + s.getApaarId() + ") active at " + s.getCollegeName()
                                + " and " + other + " simultaneously.",
                        "action", s.getFlagReason() != null && !s.getFlagReason().isBlank() ? "Notice Issued" : "Under Review"
                ));
            }
        }
        for (College c : colleges) {
            if (nvl(c.getActualEnrolment()) > nvl(c.getApprovedIntake())) {
                anomalies.add(Map.of(
                        "type", "INT-2026-" + (frdSeq++),
                        "severity", "HIGH",
                        "description", c.getName() + " (" + c.getState() + ") exceeded approved intake by "
                                + (nvl(c.getActualEnrolment()) - nvl(c.getApprovedIntake())) + " seats.",
                        "action", "Audit Scheduled"
                ));
            }
        }

        double verifiedPercent = sumActual > 0 ? (sumVerified * 100.0) / sumActual : 0.0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("institutions", colleges.size());
        stats.put("statesCovered", stateMap.size());
        stats.put("approvedIntake", sumApproved);
        stats.put("totalEnrolments", sumActual);
        stats.put("verified", sumVerified);
        stats.put("pending", sumPending);
        stats.put("duplicates", sumDup);
        stats.put("collegesUnderAudit", underAudit);
        stats.put("verifiedPercent", Math.round(verifiedPercent * 10.0) / 10.0);
        stats.put("categoryBreakdown", categoryMap);
        stats.put("studentRecords", Map.of(
                "total", totalStudents,
                "verified", verifiedStudents,
                "pending", pendingStudents,
                "duplicates", duplicateStudents,
                "rejected", rejectedStudents,
                "verifiedPercent", Math.round(studentVerifiedPercent * 10.0) / 10.0
        ));
        stats.put("stateBreakdown", stateBreakdown);
        stats.put("anomalies", anomalies);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
        ));
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", logRepository.findTop20ByOrderByTimestampDesc()
        ));
    }

    /**
     * GET /api/aicte/export-excel
     * Government-standard multi-sheet .xlsx compliance workbook.
     */
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcelReport() {
        try {
            byte[] excelBytes = excelReportService.generateNationalComplianceReport();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=AICTE_National_Compliance_Report_2026.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/aicte/export-pdf
     * Formatted official PDF compliance report (cover block, KPIs,
     * state summary, college register, anomaly register, signature).
     */
    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdfReport() {
        try {
            byte[] pdfBytes = pdfReportService.generateNationalCompliancePdf();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=AICTE_National_Compliance_Report_2026.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private static int nvl(Integer v) {
        return v != null ? v : 0;
    }
}
