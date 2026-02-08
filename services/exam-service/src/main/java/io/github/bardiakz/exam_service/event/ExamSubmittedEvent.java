package io.github.bardiakz.exam_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ExamSubmittedEvent(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("submissionId") Long submissionId,
    @JsonProperty("examTitle") String examTitle,
    @JsonProperty("studentEmail") String studentEmail,
    @JsonProperty("submittedAt") LocalDateTime submittedAt,
    @JsonProperty("timestamp") LocalDateTime timestamp
) {}
