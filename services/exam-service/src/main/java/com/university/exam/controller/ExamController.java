package com.university.exam.controller;

import com.university.exam.model.Exam;
import com.university.exam.model.ExamSubmission;
import com.university.exam.service.ExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public Exam createExam(@RequestBody Exam exam) {
        return examService.createExam(exam);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ExamSubmission submitExam(
            @PathVariable Long id,
            @RequestBody Map<Long, String> answers,
            @RequestHeader("X-User-Id") String studentId) {

        return examService.submitExam(id, answers, studentId);
    }
}
