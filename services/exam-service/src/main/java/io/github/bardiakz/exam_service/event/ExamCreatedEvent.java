package io.github.bardiakz.exam_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public record ExamCreatedEvent(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("examId") Long examId,
    @JsonProperty("examTitle") String examTitle,
    @JsonProperty("scheduledAt") LocalDateTime scheduledAt,
    @JsonProperty("duration") Integer duration,
    @JsonProperty("studentEmails") List<String> studentEmails,
    @JsonProperty("timestamp") LocalDateTime timestamp
) {}
