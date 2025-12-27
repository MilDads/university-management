package com.university.exam.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionDto {
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String text;

    @NotNull
    @Size(min = 1, max = 255)
    private String correctAnswer;
}