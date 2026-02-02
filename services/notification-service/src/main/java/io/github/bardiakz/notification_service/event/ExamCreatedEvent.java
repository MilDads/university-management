package io.github.bardiakz.notification_service.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class ExamCreatedEvent {
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("examId")
    private Long examId;
    @JsonProperty("examTitle")
    private String examTitle;
    @JsonProperty("scheduledAt")
    private LocalDateTime scheduledAt;
    @JsonProperty("duration")
    private Integer duration;
    @JsonProperty("studentEmails")
    private List<String> studentEmails;
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public ExamCreatedEvent() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public List<String> getStudentEmails() { return studentEmails; }
    public void setStudentEmails(List<String> studentEmails) { this.studentEmails = studentEmails; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
