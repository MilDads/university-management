package io.github.bardiakz.exam_service.dto;

import io.github.bardiakz.exam_service.entity.Exam;
import io.github.bardiakz.exam_service.entity.Question;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}