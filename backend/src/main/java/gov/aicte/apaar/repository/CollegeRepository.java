package gov.aicte.apaar.repository;

import gov.aicte.apaar.model.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, String> {
    Optional<College> findByCodeIgnoreCase(String code);
}
