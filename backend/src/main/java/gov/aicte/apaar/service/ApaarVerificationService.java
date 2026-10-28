package gov.aicte.apaar.service;

import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.model.VerificationLog;
import gov.aicte.apaar.repository.StudentRepository;
import gov.aicte.apaar.repository.VerificationLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ApaarVerificationService {

    private final StudentRepository studentRepository;
    private final VerificationLogRepository logRepository;
    private final KafkaEventPublisherService kafkaService;

    public ApaarVerificationService(StudentRepository studentRepository,
                                    VerificationLogRepository logRepository,
                                    KafkaEventPublisherService kafkaService) {
        this.studentRepository = studentRepository;
        this.logRepository = logRepository;
        this.kafkaService = kafkaService;
    }

    /**
     * Verifies APAAR ID against National Academic Depository (NAD) / DigiLocker
     * Simulates 1 second delay as required by SIH Problem Statement specifications
     */
    public Optional<Student> verifyApaarId(String apaarId) {
        try {
            // Simulated 1-second DigiLocker gateway handshake
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String query = apaarId.trim();
        Optional<Student> studentOpt = studentRepository.findByApaarIdIgnoreCase(query);

        if (studentOpt.isEmpty()) {
            if ("APAAR12345678".equalsIgnoreCase(query)) {
                studentOpt = studentRepository.findByApaarIdIgnoreCase("123456789012");
            } else if ("123456789012".equalsIgnoreCase(query)) {
                studentOpt = studentRepository.findByApaarIdIgnoreCase("APAAR12345678");
            } else if (query.toUpperCase().startsWith("APAAR")) {
                String numeric = query.substring(5);
                studentOpt = studentRepository.findByApaarIdIgnoreCase(numeric);
            } else {
                studentOpt = studentRepository.findByApaarIdIgnoreCase("APAAR" + query);
            }
        }

        studentOpt.ifPresent(student -> {
            logRepository.save(new VerificationLog(
                    LocalDateTime.now(),
                    student.getApaarId(),
                    student.getName(),
                    "VERIFICATION_QUERY",
                    "student_portal@aicte.gov.in",
                    "STUDENT",
                    "103.24.188.1"
            ));

            if ("duplicate".equalsIgnoreCase(student.getStatus())) {
                kafkaService.publishDuplicateAnomaly(student);
            }
        });

        return studentOpt;
    }

    public Student updateStudentStatus(String apaarId, String newStatus, String actor, String role) {
        Student student = studentRepository.findByApaarIdIgnoreCase(apaarId)
                .orElseThrow(() -> new IllegalArgumentException("Student record not found for APAAR ID: " + apaarId));

        student.setStatus(newStatus.toLowerCase());
        if ("verified".equalsIgnoreCase(newStatus)) {
            student.setDigilockerVerified(true);
        }
        Student saved = studentRepository.save(student);

        logRepository.save(new VerificationLog(
                LocalDateTime.now(),
                student.getApaarId(),
                student.getName(),
                "STATUS_UPDATED_" + newStatus.toUpperCase(),
                actor != null ? actor : "admin@aicte.ac.in",
                role != null ? role : "COLLEGE_ADMIN",
                "49.36.12.8"
        ));

        return saved;
    }
}
