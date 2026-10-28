package gov.aicte.apaar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 32)
    private String apaarId;

    @Column(nullable = false)
    private String name;

    private String dob;
    private String gender;
    private String mobile;
    private String aadhaarLast4;

    private String collegeId;
    private String collegeName;
    private String course;
    private Integer academicYear;
    private Integer abcCredits;

    private Double tenthPercent;
    private Double twelfthPercent;

    @Column(length = 20)
    private String status; // verified, pending, duplicate, rejected

    @Column(length = 50)
    private String category; // General, Supernumerary (TFW), NRI, OCI, Foreign National

    private String enrollmentNo;
    private String admissionDate;
    private Boolean digilockerVerified;
    private String state;

    private String duplicateCollegeName;
    private String duplicateCollegeId;
    private String flagReason;

    /** Optional passport-style photo path served from the platform (used in official reports). */
    @Column(length = 255)
    private String photoUrl;

    public Student() {}

    public Student(String apaarId, String name, String dob, String gender, String mobile,
                   String aadhaarLast4, String collegeId, String collegeName, String course,
                   Integer academicYear, Integer abcCredits, Double tenthPercent,
                   Double twelfthPercent, String status, String category, String enrollmentNo,
                   String admissionDate, Boolean digilockerVerified, String state,
                   String duplicateCollegeName, String duplicateCollegeId, String flagReason) {
        this.apaarId = apaarId;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.mobile = mobile;
        this.aadhaarLast4 = aadhaarLast4;
        this.collegeId = collegeId;
        this.collegeName = collegeName;
        this.course = course;
        this.academicYear = academicYear;
        this.abcCredits = abcCredits;
        this.tenthPercent = tenthPercent;
        this.twelfthPercent = twelfthPercent;
        this.status = status;
        this.category = category;
        this.enrollmentNo = enrollmentNo;
        this.admissionDate = admissionDate;
        this.digilockerVerified = digilockerVerified;
        this.state = state;
        this.duplicateCollegeName = duplicateCollegeName;
        this.duplicateCollegeId = duplicateCollegeId;
        this.flagReason = flagReason;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAadhaarLast4() { return aadhaarLast4; }
    public void setAadhaarLast4(String aadhaarLast4) { this.aadhaarLast4 = aadhaarLast4; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }

    public Integer getAbcCredits() { return abcCredits; }
    public void setAbcCredits(Integer abcCredits) { this.abcCredits = abcCredits; }

    public Double getTenthPercent() { return tenthPercent; }
    public void setTenthPercent(Double tenthPercent) { this.tenthPercent = tenthPercent; }

    public Double getTwelfthPercent() { return twelfthPercent; }
    public void setTwelfthPercent(Double twelfthPercent) { this.twelfthPercent = twelfthPercent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEnrollmentNo() { return enrollmentNo; }
    public void setEnrollmentNo(String enrollmentNo) { this.enrollmentNo = enrollmentNo; }

    public String getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

    public Boolean getDigilockerVerified() { return digilockerVerified; }
    public void setDigilockerVerified(Boolean digilockerVerified) { this.digilockerVerified = digilockerVerified; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDuplicateCollegeName() { return duplicateCollegeName; }
    public void setDuplicateCollegeName(String duplicateCollegeName) { this.duplicateCollegeName = duplicateCollegeName; }

    public String getDuplicateCollegeId() { return duplicateCollegeId; }
    public void setDuplicateCollegeId(String duplicateCollegeId) { this.duplicateCollegeId = duplicateCollegeId; }

    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
