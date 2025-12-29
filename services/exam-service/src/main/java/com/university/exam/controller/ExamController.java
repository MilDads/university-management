package com.university.exam.controller;

import com.university.exam.dto.ExamCreateRequest;
import com.university.exam.dto.ExamSubmitRequest;
import com.university.exam.model.Exam;
import com.university.exam.model.ExamSubmission;
import com.university.exam.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Exam> createExam(@Valid @RequestBody ExamCreateRequest request) {
        Exam exam = examService.createExam(request);
        return ResponseEntity.ok(exam);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamSubmission> submitExam(
            @PathVariable Long id,
            @Valid @RequestBody ExamSubmitRequest request,
            @RequestParam String studentId) {

        ExamSubmission submission = examService.submitExam(id, studentId, request);
        return ResponseEntity.ok(submission);
    }
}