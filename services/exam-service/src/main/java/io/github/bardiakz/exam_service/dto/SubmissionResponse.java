package io.github.bardiakz.exam_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SubmissionResponse {
    private Long id;
    private Long examId;
    private String studentId;
    private List<AnswerDto> answers;
    private LocalDateTime submittedAt;
    private String status;
    private Integer totalScore;
    private Integer obtainedScore;
    private LocalDateTime gradedAt;
    private String feedback;

    public SubmissionResponse() {}

    public SubmissionResponse(Long id, Long examId, String studentId, List<AnswerDto> answers,
                              LocalDateTime submittedAt, String status, Integer totalScore,
                              Integer obtainedScore, LocalDateTime gradedAt, String feedback) {
        this.id = id;
        this.examId = examId;
        this.studentId = studentId;
        this.answers = answers;
        this.submittedAt = submittedAt;
        this.status = status;
        this.totalScore = totalScore;
        this.obtainedScore = obtainedScore;
        this.gradedAt = gradedAt;
        this.feedback = feedback;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public List<AnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDto> answers) { this.answers = answers; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getObtainedScore() { return obtainedScore; }
    public void setObtainedScore(Integer obtainedScore) { this.obtainedScore = obtainedScore; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
