package gov.aicte.apaar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_logs")
public class VerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String apaarId;
    private String studentName;
    private String action;
    private String actor;
    private String role;
    private String ipAddress;

    public VerificationLog() {}

    public VerificationLog(LocalDateTime timestamp, String apaarId, String studentName,
                           String action, String actor, String role, String ipAddress) {
        this.timestamp = timestamp;
        this.apaarId = apaarId;
        this.studentName = studentName;
        this.action = action;
        this.actor = actor;
        this.role = role;
        this.ipAddress = ipAddress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
