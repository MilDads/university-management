package io.github.bardiakz.notification_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ExamGradedEvent {
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("submissionId")
    private Long submissionId;
    @JsonProperty("examTitle")
    private String examTitle;
    @JsonProperty("studentEmail")
    private String studentEmail;
    @JsonProperty("score")
    private Double score;
    @JsonProperty("maxScore")
    private Double maxScore;
    @JsonProperty("feedback")
    private String feedback;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public ExamGradedEvent() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
