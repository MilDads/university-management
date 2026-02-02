package io.github.bardiakz.notification_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ExamSubmittedEvent {
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("submissionId")
    private Long submissionId;
    @JsonProperty("examTitle")
    private String examTitle;
    @JsonProperty("studentEmail")
    private String studentEmail;
    @JsonProperty("submittedAt")
    private LocalDateTime submittedAt;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public ExamSubmittedEvent() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
