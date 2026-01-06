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
public class QuestionDto {
    private Long id;

    @NotBlank(message = "Question text is required")
    @Size(min = 5, max = 2000, message = "Question text must be between 5 and 2000 characters")
    private String text;

    @NotNull(message = "Question type is required")
    private Question.QuestionType type;

    private List<String> options;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @NotNull(message = "Marks is required")
    @Min(value = 1, message = "Marks must be at least 1")
    private Integer marks;

    @NotNull(message = "Order number is required")
    private Integer orderNumber;
}