package gov.aicte.apaar.service;

import gov.aicte.apaar.model.College;
import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.repository.CollegeRepository;
import gov.aicte.apaar.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the official multi-sheet National Compliance & Verification workbook:
 * Sheet 1 — Report Summary (letterhead block, national KPIs, state summary)
 * Sheet 2 — College Compliance Register
 * Sheet 3 — Duplicate Enrolment Register
 */
@Service
public class ExcelReportService {

    private static final short NAVY = IndexedColors.DARK_BLUE.getIndex();
    private static final String ROYAL_BLUE_HEX = "1F3864";

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;

    public ExcelReportService(CollegeRepository collegeRepository, StudentRepository studentRepository) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
    }

    public byte[] generateNationalComplianceReport() throws IOException {
        List<College> colleges = collegeRepository.findAll();
        List<Student> students = studentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFWorkbook wb = (XSSFWorkbook) workbook;

            // ---------- Fonts ----------
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont subFont = wb.createFont();
            subFont.setBold(false);
            subFont.setItalic(true);
            subFont.setFontHeightInPoints((short) 10);
            subFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont sectionFont = wb.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);
            sectionFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);

            XSSFFont normalFont = wb.createFont();
            normalFont.setFontHeightInPoints((short) 10);

            XSSFFont boldFont = wb.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 10);

            XSSFFont alertFont = wb.createFont();
            alertFont.setBold(true);
            alertFont.setColor(IndexedColors.RED.getIndex());
            alertFont.setFontHeightInPoints((short) 10);

            XSSFFont okFont = wb.createFont();
            okFont.setBold(true);
            okFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            okFont.setFontHeightInPoints((short) 10);

            XSSFFont smallItalic = wb.createFont();
            smallItalic.setItalic(true);
            smallItalic.setFontHeightInPoints((short) 8);
            smallItalic.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            // ---------- Shared styles ----------
            CellStyle titleStyle = styled(wb, titleFont, NAVY, HorizontalAlignment.CENTER, false);
            CellStyle subStyle = styled(wb, subFont, NAVY, HorizontalAlignment.CENTER, false);
            CellStyle sectionStyle = styled(wb, sectionFont, IndexedColors.ROYAL_BLUE.getIndex(), HorizontalAlignment.LEFT, true);
            CellStyle headerStyle = styled(wb, headerFont, NAVY, HorizontalAlignment.CENTER, true);
            CellStyle normalStyle = boxed(wb, normalFont, HorizontalAlignment.LEFT);
            CellStyle boldStyle = boxed(wb, boldFont, HorizontalAlignment.LEFT);
            CellStyle centerStyle = boxed(wb, normalFont, HorizontalAlignment.CENTER);
            CellStyle centerBoldStyle = boxed(wb, boldFont, HorizontalAlignment.CENTER);
            CellStyle alertStyle = boxed(wb, alertFont, HorizontalAlignment.CENTER);
            CellStyle okStyle = boxed(wb, okFont, HorizontalAlignment.CENTER);
            CellStyle noteStyle = wb.createCellStyle();
            noteStyle.setFont(smallItalic);
            CellStyle metaLabel = boxed(wb, boldFont, HorizontalAlignment.RIGHT);
            CellStyle metaValue = boxed(wb, normalFont, HorizontalAlignment.LEFT);
            CellStyle moneyStyle = boxed(wb, normalFont, HorizontalAlignment.RIGHT);

            String generatedOn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
            String reportRef = "AICTE-NCR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + String.format("%04d", colleges.size() * 7 + students.size());

            // =================== SHEET 1 : REPORT SUMMARY ===================
            Sheet s1 = wb.createSheet("Report Summary");

            Row t1 = s1.createRow(0);
            t1.setHeightInPoints(34);
            Cell c1 = t1.createCell(0);
            c1.setCellValue("AICTE NATIONAL COMPLIANCE & APAAR VERIFICATION REPORT");
            c1.setCellStyle(titleStyle);
            s1.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row t2 = s1.createRow(1);
            t2.setHeightInPoints(18);
            Cell c2 = t2.createCell(0);
            c2.setCellValue("All India Council for Technical Education  •  Ministry of Education  •  Government of India");
            c2.setCellStyle(subStyle);
            s1.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            // Report meta block
            String[][] meta = {
                    {"Report Reference", reportRef},
                    {"Generated On", generatedOn + " IST"},
                    {"Data Source", "Live Platform Database (real-time registry)"},
                    {"Coverage", colleges.size() + " institutions across all listed States / UTs"},
                    {"Classification", "OFFICIAL — FOR REGULATORY USE ONLY"}
            };
            int r = 3;
            for (String[] m : meta) {
                Row row = s1.createRow(r);
                Cell label = row.createCell(1);
                label.setCellValue(m[0]);
                label.setCellStyle(metaLabel);
                Cell value = row.createCell(2);
                value.setCellValue(m[1]);
                value.setCellStyle(metaValue);
                s1.addMergedRegion(new CellRangeAddress(r, r, 2, 6));
                r++;
            }

            // National KPI table
            r += 1;
            Row kpiSection = s1.createRow(r++);
            Cell ks = kpiSection.createCell(0);
            ks.setCellValue("  1. NATIONAL KEY INDICATORS");
            ks.setCellStyle(sectionStyle);
            s1.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 8));

            int sumApproved = colleges.stream().mapToInt(c -> nvl(c.getApprovedIntake())).sum();
            int sumActual = colleges.stream().mapToInt(c -> nvl(c.getActualEnrolment())).sum();
            int sumVerified = colleges.stream().mapToInt(c -> nvl(c.getVerifiedCount())).sum();
            int sumPending = colleges.stream().mapToInt(c -> nvl(c.getPendingCount())).sum();
            int sumDup = colleges.stream().mapToInt(c -> nvl(c.getDuplicateCount())).sum();
            int sumTfw = colleges.stream().mapToInt(c -> nvl(c.getSupernumeraryQuota())).sum();
            int sumNri = colleges.stream().mapToInt(c -> nvl(c.getNriCount())).sum();
            int sumFn = colleges.stream().mapToInt(c -> nvl(c.getFnCount())).sum();
            long underAudit = colleges.stream().filter(c -> nvl(c.getActualEnrolment()) > nvl(c.getApprovedIntake())).count();
            long statesCovered = colleges.stream().map(College::getState).distinct().count();

            String[][] kpis = {
                    {"Approved Institutions", String.valueOf(colleges.size())},
                    {"States / UTs Covered", String.valueOf(statesCovered)},
                    {"Sanctioned Intake (seats)", String.valueOf(sumApproved)},
                    {"Actual Enrolment", String.valueOf(sumActual)},
                    {"APAAR Verified Records", String.valueOf(sumVerified)},
                    {"Verification Percentage", sumActual > 0 ? String.format("%.1f%%", sumVerified * 100.0 / sumActual) : "0.0%"},
                    {"Pending Verification", String.valueOf(sumPending)},
                    {"Duplicate Records Flagged", String.valueOf(sumDup)},
                    {"Institutions Under Audit", String.valueOf(underAudit)},
                    {"Supernumerary (TFW) Seats", String.valueOf(sumTfw)},
                    {"NRI / Foreign National Seats", sumNri + " / " + sumFn}
            };
            for (String[] k : kpis) {
                Row row = s1.createRow(r++);
                Cell a = row.createCell(1);
                a.setCellValue(k[0]);
                a.setCellStyle(boldStyle);
                Cell b = row.createCell(2);
                b.setCellValue(k[1]);
                b.setCellStyle(centerBoldStyle);
                s1.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 2, 4));
            }

            // State summary table
            r += 1;
            Row stateSection = s1.createRow(r++);
            Cell ss = stateSection.createCell(0);
            ss.setCellValue("  2. STATE-WISE VERIFICATION SUMMARY");
            ss.setCellStyle(sectionStyle);
            s1.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 8));

            String[] stateHeaders = {"State / UT", "Institutions", "Sanctioned Intake", "Enrolled",
                    "Verified", "Pending", "Duplicates", "Verified %", "Status"};
            Row shRow = s1.createRow(r++);
            for (int i = 0; i < stateHeaders.length; i++) {
                Cell cell = shRow.createCell(i);
                cell.setCellValue(stateHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Map<String, long[]> stateAgg = new LinkedHashMap<>();
            for (College c : colleges) {
                long[] a = stateAgg.computeIfAbsent(c.getState(), k -> new long[6]);
                a[0]++; a[1] += nvl(c.getApprovedIntake()); a[2] += nvl(c.getActualEnrolment());
                a[3] += nvl(c.getVerifiedCount()); a[4] += nvl(c.getPendingCount()); a[5] += nvl(c.getDuplicateCount());
            }
            for (Map.Entry<String, long[]> e : stateAgg.entrySet()) {
                long[] a = e.getValue();
                Row row = s1.createRow(r++);
                row.createCell(0).setCellValue(e.getKey());
                row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(a[0]);
                row.getCell(1).setCellStyle(centerStyle);
                row.createCell(2).setCellValue(a[1]);
                row.getCell(2).setCellStyle(centerStyle);
                row.createCell(3).setCellValue(a[2]);
                row.getCell(3).setCellStyle(centerStyle);
                row.createCell(4).setCellValue(a[3]);
                row.getCell(4).setCellStyle(centerStyle);
                row.createCell(5).setCellValue(a[4]);
                row.getCell(5).setCellStyle(centerStyle);
                row.createCell(6).setCellValue(a[5]);
                row.getCell(6).setCellStyle(centerStyle);
                Cell pct = row.createCell(7);
                pct.setCellValue(a[2] > 0 ? String.format("%.1f%%", a[3] * 100.0 / a[2]) : "0.0%");
                pct.setCellStyle(centerBoldStyle);
                Cell st = row.createCell(8);
                st.setCellValue(a[2] > a[1] ? "AUDIT" : "NOMINAL");
                st.setCellStyle(a[2] > a[1] ? alertStyle : okStyle);
            }

            Row footer1 = s1.createRow(r + 1);
            Cell f1 = footer1.createCell(0);
            f1.setCellValue("Auto-generated from the live platform database. Figures represent registered institutional records as on the generation timestamp.");
            f1.setCellStyle(noteStyle);
            s1.addMergedRegion(new CellRangeAddress(r + 1, r + 1, 0, 8));

            int[] w1 = {22, 20, 16, 14, 12, 12, 12, 12, 14};
            for (int i = 0; i < w1.length; i++) s1.setColumnWidth(i, w1[i] * 256);

            // =================== SHEET 2 : COLLEGE COMPLIANCE ===================
            Sheet s2 = wb.createSheet("College Compliance");
            String[] headers = {
                    "Institute Code", "Institution Name", "State", "City",
                    "Sanctioned Intake", "Actual Enrolment", "Intake Variance",
                    "Verified Students", "Pending", "Duplicates",
                    "Supernumerary (TFW)", "NRI Quota", "Foreign National", "Compliance Status"
            };

            Row ct = s2.createRow(0);
            ct.setHeightInPoints(30);
            Cell ctc = ct.createCell(0);
            ctc.setCellValue("COLLEGE-WISE INTAKE COMPLIANCE & VERIFICATION REGISTER");
            ctc.setCellStyle(titleStyle);
            s2.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

            Row cd = s2.createRow(1);
            Cell cdc = cd.createCell(0);
            cdc.setCellValue("Report Ref: " + reportRef + "   |   Generated: " + generatedOn + " IST   |   Source: Live Platform Database");
            cdc.setCellStyle(subStyle);
            s2.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.length - 1));

            Row hRow = s2.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = hRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 3;
            for (College c : colleges) {
                Row row = s2.createRow(rowIdx++);
                int variance = nvl(c.getActualEnrolment()) - nvl(c.getApprovedIntake());

                row.createCell(0).setCellValue(c.getCode() != null ? c.getCode() : "");
                row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(c.getName() != null ? c.getName() : "");
                row.getCell(1).setCellStyle(normalStyle);
                row.createCell(2).setCellValue(c.getState() != null ? c.getState() : "");
                row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(c.getCity() != null ? c.getCity() : "");
                row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(nvl(c.getApprovedIntake()));
                row.getCell(4).setCellStyle(centerStyle);
                row.createCell(5).setCellValue(nvl(c.getActualEnrolment()));
                row.getCell(5).setCellStyle(centerStyle);

                Cell varianceCell = row.createCell(6);
                if (variance > 0) {
                    varianceCell.setCellValue("+" + variance + " (OVER-INTAKE)");
                    varianceCell.setCellStyle(alertStyle);
                } else {
                    varianceCell.setCellValue("Compliant");
                    varianceCell.setCellStyle(okStyle);
                }

                row.createCell(7).setCellValue(nvl(c.getVerifiedCount()));
                row.getCell(7).setCellStyle(centerStyle);
                row.createCell(8).setCellValue(nvl(c.getPendingCount()));
                row.getCell(8).setCellStyle(centerStyle);
                row.createCell(9).setCellValue(nvl(c.getDuplicateCount()));
                row.getCell(9).setCellStyle(centerStyle);
                row.createCell(10).setCellValue(nvl(c.getSupernumeraryQuota()));
                row.getCell(10).setCellStyle(centerStyle);
                row.createCell(11).setCellValue(nvl(c.getNriCount()));
                row.getCell(11).setCellStyle(centerStyle);
                row.createCell(12).setCellValue(nvl(c.getFnCount()));
                row.getCell(12).setCellStyle(centerStyle);

                Cell statusCell = row.createCell(13);
                if (variance > 0) {
                    statusCell.setCellValue("UNDER AUDIT");
                    statusCell.setCellStyle(alertStyle);
                } else {
                    statusCell.setCellValue("APPROVED");
                    statusCell.setCellStyle(okStyle);
                }
            }

            // Totals row
            Row totalRow = s2.createRow(rowIdx++);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL (" + colleges.size() + " institutions)");
            totalLabel.setCellStyle(headerStyle);
            for (int i = 1; i <= 3; i++) totalRow.createCell(i).setCellStyle(headerStyle);
            totalRow.createCell(4).setCellValue(sumApproved);
            totalRow.createCell(5).setCellValue(sumActual);
            totalRow.createCell(6).setCellValue(sumActual > sumApproved ? "+" + (sumActual - sumApproved) : "Compliant");
            totalRow.createCell(7).setCellValue(sumVerified);
            totalRow.createCell(8).setCellValue(sumPending);
            totalRow.createCell(9).setCellValue(sumDup);
            for (int i = 4; i <= 9; i++) {
                Cell tc = totalRow.getCell(i);
                if (tc == null) tc = totalRow.createCell(i);
                tc.setCellStyle(headerStyle);
            }
            for (int i = 10; i <= 13; i++) totalRow.createCell(i).setCellStyle(headerStyle);

            s2.createFreezePane(0, 3);
            int[] w2 = {16, 46, 14, 12, 12, 12, 18, 12, 10, 11, 14, 10, 12, 15};
            for (int i = 0; i < w2.length; i++) s2.setColumnWidth(i, w2[i] * 256);

            // =================== SHEET 3 : DUPLICATE REGISTER ===================
            Sheet s3 = wb.createSheet("Duplicate Register");
            String[] dupHeaders = {"Ref No.", "APAAR ID", "Student Name", "Enrolled Institution",
                    "Conflicting Institution", "Reason Flagged", "Record Status"};

            Row dt = s3.createRow(0);
            dt.setHeightInPoints(30);
            Cell dtc = dt.createCell(0);
            dtc.setCellValue("DUPLICATE ENROLMENT ANOMALY REGISTER");
            dtc.setCellStyle(titleStyle);
            s3.addMergedRegion(new CellRangeAddress(0, 0, 0, dupHeaders.length - 1));

            Row dd = s3.createRow(1);
            Cell ddc = dd.createCell(0);
            ddc.setCellValue("All dual-active enrolments currently flagged by the national risk engine");
            ddc.setCellStyle(subStyle);
            s3.addMergedRegion(new CellRangeAddress(1, 1, 0, dupHeaders.length - 1));

            Row dh = s3.createRow(2);
            for (int i = 0; i < dupHeaders.length; i++) {
                Cell cell = dh.createCell(i);
                cell.setCellValue(dupHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int dIdx = 3;
            int ref = 81;
            for (Student s : students) {
                if (!"duplicate".equalsIgnoreCase(s.getStatus())) continue;
                Row row = s3.createRow(dIdx++);
                row.createCell(0).setCellValue("FRD-2026-" + (ref++));
                row.getCell(0).setCellStyle(centerBoldStyle);
                row.createCell(1).setCellValue(s.getApaarId());
                row.getCell(1).setCellStyle(centerStyle);
                row.createCell(2).setCellValue(s.getName());
                row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(s.getCollegeName() != null ? s.getCollegeName() : "");
                row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(s.getDuplicateCollegeName() != null ? s.getDuplicateCollegeName() : "Another institution");
                row.getCell(4).setCellStyle(normalStyle);
                row.createCell(5).setCellValue(s.getFlagReason() != null ? s.getFlagReason() : "Dual active enrolment detected");
                row.getCell(5).setCellStyle(normalStyle);
                row.createCell(6).setCellValue("NOTICE ISSUED");
                row.getCell(6).setCellStyle(alertStyle);
            }
            if (dIdx == 3) {
                Row none = s3.createRow(3);
                Cell noneCell = none.createCell(0);
                noneCell.setCellValue("No duplicate enrolments are currently flagged — the registry is clean.");
                noneCell.setCellStyle(okStyle);
                s3.addMergedRegion(new CellRangeAddress(3, 3, 0, dupHeaders.length - 1));
            }

            int[] w3 = {14, 18, 24, 40, 40, 52, 16};
            for (int i = 0; i < w3.length; i++) s3.setColumnWidth(i, w3[i] * 256);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ---------- helpers ----------
    private static int nvl(Integer v) {
        return v != null ? v : 0;
    }

    private static CellStyle styled(XSSFWorkbook wb, XSSFFont font, short fg, HorizontalAlignment align, boolean boxed) {
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(fg);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (boxed) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }
        return style;
    }

    private static CellStyle boxed(XSSFWorkbook wb, XSSFFont font, HorizontalAlignment align) {
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
