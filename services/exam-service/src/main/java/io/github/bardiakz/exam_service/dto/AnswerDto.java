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
public class AnswerDto {
    private Long id;

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Answer text is required")
    private String answerText;

    private Boolean isCorrect;
    private Integer marksAwarded;
}