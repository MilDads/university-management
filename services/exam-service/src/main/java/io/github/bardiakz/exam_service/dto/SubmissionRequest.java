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
public class SubmissionRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotNull(message = "Answers are required")
    @Size(min = 1, message = "Submission must have at least one answer")
    private List<AnswerDto> answers;
}