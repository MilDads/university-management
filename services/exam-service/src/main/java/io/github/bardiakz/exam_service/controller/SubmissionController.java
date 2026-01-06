package io.github.bardiakz.exam_service.controller;

import io.github.bardiakz.exam_service.dto.SubmissionRequest;
import io.github.bardiakz.exam_service.dto.SubmissionResponse;
import io.github.bardiakz.exam_service.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> submitExam(
            @Valid @RequestBody SubmissionRequest request,
            Authentication authentication) {

        String studentId = authentication.getName();
        log.info("Student {} submitting exam {}", studentId, request.getExamId());

        SubmissionResponse response = submissionService.submitExam(request, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> getSubmissionById(
            @PathVariable Long submissionId,
            Authentication authentication) {

        String studentId = authentication.getName();
        log.info("Student {} fetching submission {}", studentId, submissionId);

        SubmissionResponse response = submissionService.getSubmissionById(submissionId, studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(Authentication authentication) {
        String studentId = authentication.getName();
        log.info("Fetching all submissions for student {}", studentId);

        List<SubmissionResponse> submissions = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/exam/{examId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<SubmissionResponse>> getExamSubmissions(@PathVariable Long examId) {
        log.info("Fetching all submissions for exam {}", examId);

        List<SubmissionResponse> submissions = submissionService.getSubmissionsByExam(examId);
        return ResponseEntity.ok(submissions);
    }
}