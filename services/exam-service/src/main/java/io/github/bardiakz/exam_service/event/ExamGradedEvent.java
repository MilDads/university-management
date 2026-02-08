package io.github.bardiakz.exam_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ExamGradedEvent(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("submissionId") Long submissionId,
    @JsonProperty("examTitle") String examTitle,
    @JsonProperty("studentEmail") String studentEmail,
    @JsonProperty("score") Double score,
    @JsonProperty("maxScore") Double maxScore,
    @JsonProperty("feedback") String feedback,
    @JsonProperty("timestamp") LocalDateTime timestamp
) {}
