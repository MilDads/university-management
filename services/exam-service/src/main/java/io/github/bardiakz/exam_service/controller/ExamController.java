package io.github.bardiakz.exam_service.controller;

import io.github.bardiakz.exam_service.dto.ExamRequest;
import io.github.bardiakz.exam_service.dto.ExamResponse;
import io.github.bardiakz.exam_service.service.ExamService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    private static final Logger log = LoggerFactory.getLogger(ExamController.class);

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'FACULTY')")
    public ResponseEntity<ExamResponse> createExam(
            @Valid @RequestBody ExamRequest request,
            Authentication authentication) {

        String instructorId = authentication.getName();
        log.info("Instructor {} creating new exam", instructorId);

        ExamResponse response = examService.createExam(request, instructorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{examId}/publish")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'FACULTY')")
    public ResponseEntity<ExamResponse> publishExam(
            @PathVariable Long examId,
            Authentication authentication) {

        String instructorId = authentication.getName();
        log.info("Instructor {} publishing exam {}", instructorId, examId);

        ExamResponse response = examService.publishExam(examId, instructorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamResponse> getExamById(
            @PathVariable Long examId,
            Authentication authentication) {

        String userId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .findFirst()
                .map(auth -> auth.replace("ROLE_", ""))
                .orElse("STUDENT");

        log.info("User {} fetching exam {}", userId, examId);

        ExamResponse response = examService.getExamById(examId, userId, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/instructor/my-exams")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'FACULTY')")
    public ResponseEntity<List<ExamResponse>> getMyExams(Authentication authentication) {
        String instructorId = authentication.getName();
        log.info("Fetching all exams for instructor {}", instructorId);

        List<ExamResponse> exams = examService.getExamsByInstructor(instructorId);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ExamResponse>> getActiveExams() {
        log.info("Fetching all active exams");
        List<ExamResponse> exams = examService.getActiveExams();
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ExamResponse>> getUpcomingExams() {
        log.info("Fetching all upcoming exams");
        List<ExamResponse> exams = examService.getUpcomingExams();
        return ResponseEntity.ok(exams);
    }
}