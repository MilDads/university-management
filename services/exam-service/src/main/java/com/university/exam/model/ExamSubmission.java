package com.university.exam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class ExamSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String studentId;

    @Min(0)
    @Max(100)
    private int score;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }
}