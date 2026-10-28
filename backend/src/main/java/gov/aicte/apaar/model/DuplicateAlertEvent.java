package gov.aicte.apaar.model;

import java.time.LocalDateTime;

public class DuplicateAlertEvent {
    private String eventId;
    private String apaarId;
    private String studentName;
    private String collegeA;
    private String collegeB;
    private String severity;
    private String description;
    private LocalDateTime timestamp;

    public DuplicateAlertEvent() {}

    public DuplicateAlertEvent(String eventId, String apaarId, String studentName,
                               String collegeA, String collegeB, String severity,
                               String description, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.apaarId = apaarId;
        this.studentName = studentName;
        this.collegeA = collegeA;
        this.collegeB = collegeB;
        this.severity = severity;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCollegeA() { return collegeA; }
    public void setCollegeA(String collegeA) { this.collegeA = collegeA; }

    public String getCollegeB() { return collegeB; }
    public void setCollegeB(String collegeB) { this.collegeB = collegeB; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
