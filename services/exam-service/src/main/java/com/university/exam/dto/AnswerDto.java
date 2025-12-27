package com.university.exam.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class AnswerDto {

    @Positive(message = "Question ID must be a positive number")
    private Long questionId;

    @NotNull(message = "Answer cannot be null")
    private String answer;
}