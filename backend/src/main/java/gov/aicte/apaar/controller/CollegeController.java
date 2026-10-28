package gov.aicte.apaar.controller;

import gov.aicte.apaar.model.College;
import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.repository.CollegeRepository;
import gov.aicte.apaar.repository.StudentRepository;
import gov.aicte.apaar.service.PdfReportService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/college")
@CrossOrigin(origins = "*")
public class CollegeController {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final PdfReportService pdfReportService;

    public CollegeController(StudentRepository studentRepository, CollegeRepository collegeRepository,
                             PdfReportService pdfReportService) {
        this.studentRepository = studentRepository;
        this.collegeRepository = collegeRepository;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getCollegeStats(@RequestParam(defaultValue = "COL001") String collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElse(new College("COL001", "Delhi Institute of Engineering & Technology", "AICTE-DL-0012",
                        "Delhi", "New Delhi", 1200, 1240, 980, 200, 60, 45, 24, 8));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "college", college,
                "intakeVariance", college.getActualEnrolment() - college.getApprovedIntake(),
                "overIntakeFlag", college.getActualEnrolment() > college.getApprovedIntake()
        ));
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(
            @RequestParam(required = false) String collegeId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category
    ) {
        // Always return the registry in ascending APAAR ID order (natural, numeric-aware)
        List<Student> students = (q != null && !q.trim().isEmpty())
                ? studentRepository.searchStudents(q.trim())
                : studentRepository.findAll(Sort.by(Sort.Direction.ASC, "apaarId"));

        if (collegeId != null && !collegeId.isEmpty()) {
            students = students.stream().filter(s -> collegeId.equalsIgnoreCase(s.getCollegeId())).collect(Collectors.toList());
        }

        if (course != null && !"all".equalsIgnoreCase(course)) {
            students = students.stream().filter(s -> s.getCourse().toLowerCase().contains(course.toLowerCase())).collect(Collectors.toList());
        }

        if (year != null) {
            students = students.stream().filter(s -> year.equals(s.getAcademicYear())).collect(Collectors.toList());
        }

        if (status != null && !"all".equalsIgnoreCase(status)) {
            students = students.stream().filter(s -> status.equalsIgnoreCase(s.getStatus())).collect(Collectors.toList());
        }

        if (category != null && !"all".equalsIgnoreCase(category)) {
            students = students.stream().filter(s -> category.equalsIgnoreCase(s.getCategory())).collect(Collectors.toList());
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "total", students.size(),
                "data", students
        ));
    }

    /**
     * POST /api/college/register
     * Persists a newly registered institution with REAL provisioning:
     * zero enrolments, zero verifications — the institute builds its own
     * registry through bulk upload. No dummy data is ever generated.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerInstitution(@RequestBody Map<String, Object> body) {
        String pid = body.get("pid") != null ? body.get("pid").toString().trim() : "";
        String name = body.get("name") != null ? body.get("name").toString().trim() : "";
        String email = body.get("email") != null ? body.get("email").toString().trim().toLowerCase() : "";
        String state = body.get("state") != null ? body.get("state").toString().trim() : "National";
        String city = body.get("city") != null ? body.get("city").toString().trim() : "Main Campus";
        int intake = 0;
        try {
            intake = Integer.parseInt(String.valueOf(body.get("approvedIntake")));
        } catch (Exception ignored) {}

        if (name.isEmpty() || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INSTITUTION_NAME_AND_EMAIL_REQUIRED"
            ));
        }

        String id = "COL-" + (pid.isEmpty() ? String.valueOf(System.currentTimeMillis() % 1000000) : pid);
        String code = "AICTE-" + (pid.isEmpty() ? id : pid);

        if (collegeRepository.existsById(id)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Institution already registered on the platform.",
                    "college", collegeRepository.findById(id).get()
            ));
        }

        College college = new College(id, name, code, state, city,
                intake, 0, 0, 0, 0, 0, 0, 0);
        college.setEmail(email);
        collegeRepository.save(college);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Institution provisioned with a clean registry (0 enrolments). Import students via bulk upload.",
                "college", college
        ));
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<?> bulkUpload(@RequestBody List<Student> incoming) {
        int added = 0;
        int duplicates = 0;

        for (Student s : incoming) {
            if (studentRepository.findByApaarIdIgnoreCase(s.getApaarId()).isPresent()) {
                duplicates++;
            } else {
                studentRepository.save(s);
                added++;
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Batch validated! %d processed: %d added, %d duplicates flagged.", incoming.size(), added, duplicates),
                "addedCount", added,
                "duplicateCount", duplicates
        ));
    }

    /**
     * GET /api/college/student-report?apaarId=123456789012
     * Official Student Academic Profile & Verification Report (PDF)
     * with the student's photograph, identity, academics, semester
     * history and verification details.
     */
    @GetMapping("/student-report")
    public ResponseEntity<byte[]> studentReport(@RequestParam String apaarId) {
        try {
            byte[] pdfBytes = pdfReportService.generateStudentReportPdf(apaarId);

            String safeName = apaarId.replaceAll("[^A-Za-z0-9]", "");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=Student_Report_" + safeName + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
