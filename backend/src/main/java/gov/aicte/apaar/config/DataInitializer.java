package gov.aicte.apaar.config;

import gov.aicte.apaar.model.College;
import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.model.VerificationLog;
import gov.aicte.apaar.repository.CollegeRepository;
import gov.aicte.apaar.repository.StudentRepository;
import gov.aicte.apaar.repository.VerificationLogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the national registry with a realistic, diverse dataset:
 * 14 institutions across 14 States/UTs and ~140 students with
 * region-appropriate names, courses, categories and verification states.
 * Every KPI, chart and report on the platform is derived from these real records.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final VerificationLogRepository logRepository;

    public DataInitializer(StudentRepository studentRepository,
                           CollegeRepository collegeRepository,
                           VerificationLogRepository logRepository) {
        this.studentRepository = studentRepository;
        this.collegeRepository = collegeRepository;
        this.logRepository = logRepository;
    }

    @Override
    public void run(String... args) {
        // ---------------- Institutions: 14 States / UTs ----------------
        List<College> colleges = List.of(
                new College("COL001", "Delhi Institute of Engineering & Technology", "AICTE-DL-0012",
                        "Delhi", "New Delhi", 1200, 1240, 1128, 72, 14, 62, 45, 24),
                new College("COL002", "Maharashtra National Institute of Technology", "AICTE-MH-0845",
                        "Maharashtra", "Pune", 960, 942, 889, 38, 6, 47, 30, 12),
                new College("COL003", "Bangalore Technical & Science Academy", "AICTE-KA-0491",
                        "Karnataka", "Bengaluru", 840, 828, 791, 26, 5, 41, 24, 10),
                new College("COL004", "Tamil Nadu Engineering & Management Campus", "AICTE-TN-0219",
                        "Tamil Nadu", "Chennai", 900, 891, 842, 30, 8, 44, 26, 12),
                new College("COL005", "Uttar Pradesh Council of Technology", "AICTE-UP-0672",
                        "Uttar Pradesh", "Lucknow", 780, 815, 717, 61, 12, 39, 20, 8),
                new College("COL006", "Gujarat Institute of Technology & Management", "AICTE-GJ-0338",
                        "Gujarat", "Ahmedabad", 720, 706, 664, 27, 4, 35, 18, 9),
                new College("COL007", "Bengal College of Engineering & Technology", "AICTE-WB-0527",
                        "West Bengal", "Kolkata", 660, 648, 601, 29, 5, 32, 16, 7),
                new College("COL008", "Rajasthan Technical Studies Institute", "AICTE-RJ-0614",
                        "Rajasthan", "Jaipur", 600, 587, 542, 28, 3, 29, 15, 6),
                new College("COL009", "Kerala Institute of Engineering & Applied Science", "AICTE-KL-0459",
                        "Kerala", "Kochi", 540, 531, 506, 17, 2, 26, 12, 5),
                new College("COL010", "Telangana State College of Engineering", "AICTE-TG-0392",
                        "Telangana", "Hyderabad", 840, 822, 768, 34, 6, 41, 22, 11),
                new College("COL011", "Punjab Technical Campus", "AICTE-PB-0288",
                        "Punjab", "Ludhiana", 480, 466, 421, 27, 4, 23, 11, 6),
                new College("COL012", "Madhya Pradesh Engineering Academy", "AICTE-MP-0736",
                        "Madhya Pradesh", "Indore", 600, 594, 549, 30, 5, 30, 14, 7),
                new College("COL013", "Bihar Institute of Technology", "AICTE-BR-0176",
                        "Bihar", "Patna", 540, 561, 489, 44, 9, 27, 13, 5),
                new College("COL014", "Haryana Engineering & Technology College", "AICTE-HR-0541",
                        "Haryana", "Gurugram", 480, 472, 438, 21, 3, 24, 10, 6)
        );
        collegeRepository.saveAll(colleges);

        // ---------------- Students ----------------
        List<Student> students = new ArrayList<>();

        // Featured students (portraits available under /photos for report generation)
        students.add(new Student("123456789012", "Aarav Sharma", "2004-05-14", "Male", "+91 98765 43210",
                "4829", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Computer Science & Engineering",
                3, 96, 94.6, 92.4, "verified", "General", "2022-CSE-001", "2022-08-16", true, "Delhi", null, null, null));
        students.get(students.size() - 1).setPhotoUrl("/photos/123456789012.jpg");

        students.add(new Student("100000000005", "Mohammad Farhan Akhtar", "2004-07-12", "Male", "+91 98112 34567",
                "5087", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Mechanical Engineering",
                3, 88, 84.0, 82.5, "duplicate", "General", "2022-ME-089", "2022-08-14", true, "Delhi",
                "Uttar Pradesh Council of Technology", "COL005", "Simultaneous regular enrollment active at UP Council of Technology under UID UP-2022-892"));

        students.add(new Student("100000000009", "Tariq Ahmad Mir", "2005-06-19", "Male", "+91 94190 22334",
                "8521", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Civil Engineering",
                2, 62, 82.4, 80.6, "verified", "General", "2023-CE-033", "2023-08-11", true, "Delhi", null, null, null));

        students.add(new Student("100000000012", "Ananya Banerjee", "2005-08-14", "Female", "+91 98310 99887",
                "3984", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Computer Science & Engineering",
                1, 30, 96.0, 94.2, "duplicate", "General", "2024-CSE-112", "2024-08-01", true, "Delhi",
                "Bangalore Technical & Science Academy", "COL003", "Dual enrolment recorded across Delhi and Karnataka colleges in AY 2024-25"));

        students.add(new Student("100000000024", "Pratik Bhalerao", "2004-12-28", "Male", "+91 97660 33221",
                "6719", "COL002", "Maharashtra National Institute of Technology", "B.Tech Instrumentation & Control",
                3, 88, 86.5, 84.0, "verified", "General", "2022-IC-019", "2022-08-16", true, "Maharashtra", null, null, null));
        students.get(students.size() - 1).setPhotoUrl("/photos/100000000024.jpg");

        students.add(new Student("100000000025", "Tanvi Seshadri", "2004-03-08", "Female", "+91 94450 66778",
                "9532", "COL004", "Tamil Nadu Engineering & Management Campus", "B.Tech Biomedical Engineering",
                3, 92, 94.0, 92.8, "duplicate", "General", "2022-BME-007", "2022-08-15", true, "Tamil Nadu",
                "Maharashtra National Institute of Technology", "COL002", "Student migration unclosed; simultaneous active record detected in MH portal"));
        students.get(students.size() - 1).setPhotoUrl("/photos/100000000025.jpg");

        students.add(new Student("100000000041", "Kavya Hegde", "2005-01-22", "Female", "+91 98860 45123",
                "3177", "COL003", "Bangalore Technical & Science Academy", "B.Tech Electronics & Communication",
                2, 58, 91.8, 90.2, "verified", "General", "2024-ECE-044", "2024-08-12", true, "Karnataka", null, null, null));
        students.get(students.size() - 1).setPhotoUrl("/photos/100000000041.jpg");

        students.add(new Student("100000000071", "Sourav Chatterjee", "2004-09-03", "Male", "+91 98300 78456",
                "7241", "COL007", "Bengal College of Engineering & Technology", "B.Tech Mechanical Engineering",
                3, 86, 85.4, 83.8, "verified", "Supernumerary (TFW)", "2022-ME-105", "2022-08-14", true, "West Bengal", null, null, null));
        students.get(students.size() - 1).setPhotoUrl("/photos/100000000071.jpg");

        students.add(new Student("100000000111", "Simran Kaur", "2003-11-17", "Female", "+91 98780 33410",
                "8814", "COL011", "Punjab Technical Campus", "B.Tech Information Technology",
                4, 118, 93.2, 91.6, "verified", "General", "2022-IT-021", "2022-08-15", true, "Punjab", null, null, null));
        students.get(students.size() - 1).setPhotoUrl("/photos/100000000111.jpg");

        // Additional named students per state — diverse, deterministic, realistic.
        String[][] pools = {
                // collegeId, state, first names (comma) , surnames (comma), courses (comma)
                {"COL001", "Delhi",
                 "Ishaan,Sanya,Vansh,Ritika,Kabir,Naina,Aditya,Trisha,Dhruv,Meera,Arjun,Saanvi",
                 "Kapoor,Malhotra,Verma,Mehta,Sethi,Chopra,Bansal,Kohli,Anand,Joshi",
                 "B.Tech Computer Science & Engineering,B.Tech Information Technology,B.Tech Electronics & Communication,B.Tech Mechanical Engineering,B.Tech Civil Engineering"},
                {"COL002", "Maharashtra",
                 "Omkar,Aditi,Rohan,Pooja,Siddharth,Aishwarya,Ketan,Sneha,Mayur,Vaishnavi",
                 "Deshmukh,Patil,Kulkarni,Joshi,Shinde,Pawar,Deshpande,Jadhav,Rane",
                 "B.Tech Instrumentation & Control,B.Tech Computer Science & Engineering,B.Tech Mechanical Engineering,B.Tech Electronics & Communication,B.Tech Chemical Engineering"},
                {"COL003", "Karnataka",
                 "Manjunath,Kavya,Prakash,Anusha,Raghav,Divya,Suresh,Anjali,Naveen,Rashmi",
                 "Gowda,Reddy,Hegde,Shetty,Rao,Bhat,Kamath,Prabhu,Nair",
                 "B.Tech Computer Science & Engineering,B.Tech Electronics & Communication,B.Tech Information Technology,B.Tech Electrical & Electronics,B.Tech Mechanical Engineering"},
                {"COL004", "Tamil Nadu",
                 "Karthik,Priya,Vikram,Divya,Hariharan,Lakshmi,Santhosh,Nithya,Balaji,Keerthana",
                 "Iyer,Murugan,Subramanian,Raman,Krishnan,Pillai,Chidambaram,Sundaram",
                 "B.Tech Biomedical Engineering,B.Tech Computer Science & Engineering,B.Tech Electronics & Communication,B.Tech Mechanical Engineering,B.Tech Information Technology"},
                {"COL005", "Uttar Pradesh",
                 "Adarsh,Priyanka,Gaurav,Neha,Rohan,Shreya,Aman,Swati,Deepak,Kajal",
                 "Kumar,Singh,Yadav,Gupta,Tiwari,Mishra,Chauhan,Pandey",
                 "B.Tech Computer Science & Engineering,B.Tech Civil Engineering,B.Tech Mechanical Engineering,B.Tech Electrical Engineering,B.Tech Information Technology"},
                {"COL006", "Gujarat",
                 "Jay,Kinjal,Meet,Nehal,Darshan,Foram,Parth,Vrunda,Het,Krupa",
                 "Patel,Shah,Desai,Trivedi,Bhatt,Chauhan,Amin",
                 "B.Tech Computer Science & Engineering,B.Tech Information Technology,B.Tech Mechanical Engineering,B.Tech Electronics & Communication,B.Tech Civil Engineering"},
                {"COL007", "West Bengal",
                 "Sourav,Debasmita,Arindam,Ritam,Sayantani,Aniket,Orpita,Subhodip",
                 "Chatterjee,Das,Mukherjee,Ghosh,Banerjee,Dutta,Saha,Roy",
                 "B.Tech Mechanical Engineering,B.Tech Computer Science & Engineering,B.Tech Electrical Engineering,B.Tech Civil Engineering,B.Tech Electronics & Communication"},
                {"COL008", "Rajasthan",
                 "Vikas,Kunal,Ritu,Mahesh,Pooja,Lokesh,Bhavna,Gaurav",
                 "Choudhary,Rathore,Agarwal,Sharma,Bishnoi,Joshi,Solanki",
                 "B.Tech Computer Science & Engineering,B.Tech Electronics & Communication,B.Tech Mechanical Engineering,B.Tech Civil Engineering,B.Tech Information Technology"},
                {"COL009", "Kerala",
                 "Vishnu,Meera,Alan,Anjali,Aravind,Gayathri,Joel,Annapurna",
                 "Menon,Nair,Pillai,Thomas,Kurian,Varma,Krishnan",
                 "B.Tech Computer Science & Engineering,B.Tech Electronics & Communication,B.Tech Electrical & Electronics,B.Tech Civil Engineering,B.Tech Mechanical Engineering"},
                {"COL010", "Telangana",
                 "Sravan,Sneha,Kalyan,Harika,Rakesh,Vaishnavi,Pranay,Sushma",
                 "Reddy,Rao,Goud,Yadav,Chary,Kandula,Maganti",
                 "B.Tech Computer Science & Engineering,B.Tech Information Technology,B.Tech Electronics & Communication,B.Tech Mechanical Engineering,B.Tech Civil Engineering"},
                {"COL011", "Punjab",
                 "Simran,Gurpreet,Harman,Jasleen,Arshdeep,Rajveer,Mehak,Karan",
                 "Singh,Kaur,Gill,Sandhu,Dhillon,Bains,Virk",
                 "B.Tech Information Technology,B.Tech Computer Science & Engineering,B.Tech Electronics & Communication,B.Tech Mechanical Engineering,B.Tech Civil Engineering"},
                {"COL012", "Madhya Pradesh",
                 "Aryan,Nidhi,Devansh,Shreya,Aman,Palak,Rishabh,Ananya",
                 "Tiwari,Dwivedi,Saxena,Bundela,Raghuwanshi,Namdev,Jain",
                 "B.Tech Computer Science & Engineering,B.Tech Mechanical Engineering,B.Tech Civil Engineering,B.Tech Electrical Engineering,B.Tech Information Technology"},
                {"COL013", "Bihar",
                 "Amit,Khushi,Rohit,Chandni,Manish,Shalini,Praveen,Nidhi",
                 "Jha,Mishra,Ranjan,Bhattacharya,Singh,Verma,Kumar",
                 "B.Tech Civil Engineering,B.Tech Computer Science & Engineering,B.Tech Electrical Engineering,B.Tech Mechanical Engineering,B.Tech Electronics & Communication"},
                {"COL014", "Haryana",
                 "Nitin,Bhavna,Sahil,Komal,Deepak,Priya,Rohit,Sneha",
                 "Chauhan,Malik,Hooda,Sheoran,Kadian,Dalal,Rangi",
                 "B.Tech Computer Science & Engineering,B.Tech Electronics & Communication,B.Tech Mechanical Engineering,B.Tech Civil Engineering,B.Tech Information Technology"}
        };

        // Status rotation keeps national ratios realistic:
        // most verified, a few pending, a fixed handful of duplicates / rejected.
        String[] statusCycle = {
                "verified", "verified", "verified", "pending", "verified",
                "verified", "verified", "verified", "pending", "verified"
        };
        String[] categoryCycle = {
                "General", "General", "General", "Supernumerary (TFW)", "General",
                "General", "NRI", "General", "General", "Supernumerary (TFW)"
        };
        // Generated cross-state duplicates (student index -> conflicting college)
        java.util.Map<Integer, String[]> generatedDup = new java.util.HashMap<>();
        generatedDup.put(62, new String[]{"Madhya Pradesh Engineering Academy", "COL012",
                "Same APAAR identity active simultaneously in Gujarat and MP registries for AY 2023-24"});
        generatedDup.put(83, new String[]{"Haryana Engineering & Technology College", "COL014",
                "Concurrent active enrolment detected across Punjab and Haryana institutions"});
        generatedDup.put(117, new String[]{"Telangana State College of Engineering", "COL010",
                "Unclosed migration; original seat still active in Bihar registry"});

        long apaarSeed = 100000000201L; // 12-digit numeric APAAR IDs (no overlap with featured records)
        int counter = 0;

        for (String[] pool : pools) {
            String collegeId = pool[0];
            String state = pool[1];
            String[] firsts = pool[2].split(",");
            String[] surnames = pool[3].split(",");
            String[] courses = pool[4].split(",");

            College col = colleges.stream().filter(c -> c.getId().equals(collegeId)).findFirst().orElse(null);
            String collegeName = col != null ? col.getName() : "Unknown Institution";

            for (int j = 0; j < firsts.length; j++) {
                counter++;
                String first = firsts[j];
                String last = surnames[j % surnames.length];
                String name = first + " " + last;
                String apaar = String.valueOf(apaarSeed + counter);
                String course = courses[counter % courses.length];
                int year = (counter % 4) + 1;
                int credits = year * 30 - (counter % 3) * 2;
                double tenth = 78.0 + (counter % 18) + (counter % 3) * 0.5;
                double twelfth = 75.0 + (counter % 20) + (counter % 4) * 0.5;
                String status = statusCycle[counter % statusCycle.length];
                String category = categoryCycle[counter % categoryCycle.length];
                String gender = "aeiou".indexOf(Character.toLowerCase(first.charAt(0))) >= 0
                        || first.endsWith("a") || first.endsWith("i") ? "Female" : "Male";
                // Gender corrections for common names
                if ("Sanya,Naina,Trisha,Meera,Saanvi,Aditi,Pooja,Sneha,Vaishnavi,Kavya,Anusha,Divya,Anjali,Rashmi,Priya,Nithya,Keerthana,Priyanka,Neha,Shreya,Swati,Kajal,Kinjal,Foram,Vrunda,Krupa,Debasmita,Sayantani,Orpita,Ritu,Bhavna,Annapurna,Gayathri,Harika,Sushma,Simran,Jasleen,Mehak,Nidhi,Khushi,Chandni,Shalini,Bhavna,Komal".contains(first)) {
                    gender = "Female";
                }
                if ("Jay,Meet,Darshan,Het,Parth,Alan,Joel".contains(first)) {
                    gender = "Male";
                }

                int admissionYear = 2025 - year;
                String enrolmentNo = admissionYear + "-" + course.replace("B.Tech ", "").substring(0, 2).toUpperCase()
                        + "-" + String.format("%03d", (counter * 7) % 900 + 100);
                String dob = String.format("200%d-0%d-%02d", 4 + (counter % 2), (counter % 9) + 1, (counter % 27) + 1);
                String mobile = "+91 9" + String.format("%09d", 800000000 + counter * 13711);

                String dupColName = null, dupColId = null, dupReason = null;
                if (generatedDup.containsKey(counter)) {
                    status = "duplicate";
                    dupColName = generatedDup.get(counter)[0];
                    dupColId = generatedDup.get(counter)[1];
                    dupReason = generatedDup.get(counter)[2];
                }
                if ("rejected".equals(status)) {
                    status = "verified"; // keep registry constructive; rejections are logged separately
                }

                boolean digilocker = "verified".equals(status);

                Student s = new Student(apaar, name, dob, gender, mobile,
                        String.format("%04d", 1000 + counter),
                        collegeId, collegeName, course, year, credits, tenth, twelfth,
                        status, category, enrolmentNo, admissionYear + "-08-15", digilocker, state,
                        dupColName, dupColId, dupReason);
                students.add(s);
            }
        }

        studentRepository.saveAll(students);

        // ---------------- Audit Ledger seeds ----------------
        LocalDateTime now = LocalDateTime.now();
        logRepository.save(new VerificationLog(now.minusHours(6), "123456789012", "Aarav Sharma",
                "VERIFIED", "admin@delhi-tech.edu.in", "COLLEGE_ADMIN", "14.139.60.2"));
        logRepository.save(new VerificationLog(now.minusHours(5), "100000000024", "Pratik Bhalerao",
                "VERIFIED", "registrar@mnit-pune.edu.in", "COLLEGE_ADMIN", "103.21.58.9"));
        logRepository.save(new VerificationLog(now.minusHours(4), "100000000005", "Mohammad Farhan Akhtar",
                "DUPLICATE_FLAGGED", "system_risk_engine", "AICTE_SUPER_ADMIN", "164.100.158.4"));
        logRepository.save(new VerificationLog(now.minusHours(3), "100000000025", "Tanvi Seshadri",
                "DUPLICATE_FLAGGED", "system_risk_engine", "AICTE_SUPER_ADMIN", "164.100.158.4"));
        logRepository.save(new VerificationLog(now.minusHours(2), "100000000012", "Ananya Banerjee",
                "DUPLICATE_FLAGGED", "system_risk_engine", "AICTE_SUPER_ADMIN", "164.100.158.4"));
        logRepository.save(new VerificationLog(now.minusMinutes(75), "100000000041", "Kavya Hegde",
                "VERIFICATION_QUERY", "student_portal@aicte.gov.in", "STUDENT", "103.24.188.1"));
        logRepository.save(new VerificationLog(now.minusMinutes(52), "100000000111", "Simran Kaur",
                "STATUS_UPDATED_VERIFIED", "admin@ptc-ludhiana.edu.in", "COLLEGE_ADMIN", "49.36.12.8"));
        logRepository.save(new VerificationLog(now.minusMinutes(30), "100000000071", "Sourav Chatterjee",
                "VERIFIED", "office@bengal-engg.edu.in", "COLLEGE_ADMIN", "117.99.44.2"));
    }
}
