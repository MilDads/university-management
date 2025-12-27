package com.university.exam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;  // این خط رو اضافه کن

@Entity
@Getter     // اضافه کن
@Setter     // اضافه کن (این مهمه برای setText و setExam)
@NoArgsConstructor  // اختیاری اما خوبه برای JPA
@AllArgsConstructor // اختیاری اما خوبه
@ToString   // اختیاری، برای دیباگ بهتر
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String text;

    @NotNull
    @Size(min = 1, max = 255)
    private String correctAnswer;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Long getId() {
        return id;
    }
}