package io.github.bardiakz.exam_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, length = 5000)
    private String answerText;

    @Column
    private Boolean isCorrect;

    @Column
    private Integer marksAwarded;
}