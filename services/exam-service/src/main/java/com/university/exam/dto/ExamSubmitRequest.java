package com.university.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitRequest {

    @NotNull(message = "Answers cannot be null")
    private List<AnswerDto> answers;
}