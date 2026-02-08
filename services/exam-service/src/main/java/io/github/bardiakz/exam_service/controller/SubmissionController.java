package io.github.bardiakz.exam_service.controller;

import io.github.bardiakz.exam_service.dto.SubmissionRequest;
import io.github.bardiakz.exam_service.dto.SubmissionResponse;
import io.github.bardiakz.exam_service.service.SubmissionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private static final Logger log = LoggerFactory.getLogger(SubmissionController.class);

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> submitExam(
            @Valid @RequestBody SubmissionRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {

        String studentId = authentication.getName();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = studentId + "@university.edu";
        }
        
        log.info("Student {} ({}) submitting exam {}", studentId, userEmail, request.getExamId());

        SubmissionResponse response = submissionService.submitExam(request, studentId, userEmail);
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