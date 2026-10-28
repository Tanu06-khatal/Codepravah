package gov.aicte.apaar.controller;

import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.service.ApaarVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/verify")
@CrossOrigin(origins = "*")
public class VerificationController {

    private final ApaarVerificationService verificationService;

    public VerificationController(ApaarVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /**
     * GET /api/verify/{apaarId}
     * Simulates 1s DigiLocker handshake and returns student credentials or 404
     */
    @GetMapping("/{apaarId}")
    public ResponseEntity<?> verifyApaar(@PathVariable String apaarId) {
        if (apaarId == null || apaarId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INVALID_APAAR_ID",
                    "message", "APAAR ID parameter cannot be blank."
            ));
        }

        Optional<Student> studentOpt = verificationService.verifyApaarId(apaarId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "STUDENT_NOT_FOUND",
                    "message", "No student record found matching APAAR ID: " + apaarId + ". Please verify with institution."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", studentOpt.get(),
                "metadata", Map.of(
                        "verifiedAt", LocalDateTime.now().toString(),
                        "encryptionStandard", "AES-256-GCM",
                        "dataDepository", "DigiLocker / National Academic Depository (NAD)",
                        "compliance", "AICTE APH 2026-27 Clause 8.4"
                )
        ));
    }

    /**
     * POST /api/verify/{apaarId}/status
     * Allows College Admin or AICTE Admin to approve/reject
     */
    @PostMapping("/{apaarId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String apaarId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String actor = body.get("actorEmail");
        String role = body.get("role");

        if (status == null || (!status.equalsIgnoreCase("verified") && !status.equalsIgnoreCase("rejected"))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Status must be 'verified' or 'rejected'"));
        }

        try {
            Student updated = verificationService.updateStudentStatus(apaarId, status, actor, role);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
