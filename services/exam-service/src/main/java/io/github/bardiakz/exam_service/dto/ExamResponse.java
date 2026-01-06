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
public class ExamResponse {
    private Long id;
    private String title;
    private String description;
    private String instructorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Exam.ExamStatus status;
    private List<QuestionDto> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}