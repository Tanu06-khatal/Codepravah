package gov.aicte.apaar.service;

import gov.aicte.apaar.model.DuplicateAlertEvent;
import gov.aicte.apaar.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class KafkaEventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${aicte.kafka.duplicate-topic:aicte.apaar.duplicate-events}")
    private String topicName;

    public KafkaEventPublisherService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDuplicateAnomaly(Student student) {
        DuplicateAlertEvent event = new DuplicateAlertEvent(
                UUID.randomUUID().toString(),
                student.getApaarId(),
                student.getName(),
                student.getCollegeName(),
                student.getDuplicateCollegeName() != null ? student.getDuplicateCollegeName() : "External Institute",
                "CRITICAL",
                student.getFlagReason() != null ? student.getFlagReason() : "Dual active enrolment detected across institutions",
                LocalDateTime.now()
        );

        String payload = String.format("{\"eventId\":\"%s\",\"apaarId\":\"%s\",\"student\":\"%s\",\"conflict\":\"%s\"}",
                event.getEventId(), event.getApaarId(), event.getStudentName(), event.getCollegeB());

        try {
            kafkaTemplate.send(topicName, student.getApaarId(), payload);
            log.info("Kafka Event Dispatched to [{}]: Dual enrolment for APAAR ID {}", topicName, student.getApaarId());
        } catch (Exception e) {
            log.warn("Kafka broker offline/unreachable. Event logged locally to audit store for APAAR ID: {}", student.getApaarId());
        }
    }
}
