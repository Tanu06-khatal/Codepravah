package gov.aicte.apaar.repository;

import gov.aicte.apaar.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByApaarIdIgnoreCase(String apaarId);
    List<Student> findByCollegeId(String collegeId);
    List<Student> findByStatus(String status);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.apaarId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.enrollmentNo) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Student> searchStudents(String query);

    Long countByStatus(String status);
}
