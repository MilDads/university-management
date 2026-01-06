package io.github.bardiakz.exam_service.dto;

import io.github.bardiakz.exam_service.entity.Question;
import jakarta.validation.constraints.*;
import java.util.List;

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

    public QuestionDto() {}

    public QuestionDto(Long id, String text, Question.QuestionType type, List<String> options,
                       String correctAnswer, Integer marks, Integer orderNumber) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.orderNumber = orderNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Question.QuestionType getType() { return type; }
    public void setType(Question.QuestionType type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }
}
