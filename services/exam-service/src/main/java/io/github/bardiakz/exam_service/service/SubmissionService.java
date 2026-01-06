package io.github.bardiakz.exam_service.service;

import io.github.bardiakz.exam_service.dto.*;
import io.github.bardiakz.exam_service.entity.*;
import io.github.bardiakz.exam_service.exception.ExamNotFoundException;
import io.github.bardiakz.exam_service.repository.ExamRepository;
import io.github.bardiakz.exam_service.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ExamRepository examRepository;

    public SubmissionService(SubmissionRepository submissionRepository, ExamRepository examRepository) {
        this.submissionRepository = submissionRepository;
        this.examRepository = examRepository;
    }

    @Transactional
    public SubmissionResponse submitExam(SubmissionRequest request, String studentId) {
        log.info("Student {} submitting exam {}", studentId, request.getExamId());

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with ID: " + request.getExamId()));

        validateExamSubmission(exam);

        if (submissionRepository.existsByExamIdAndStudentId(request.getExamId(), studentId)) {
            throw new IllegalStateException("You have already submitted this exam");
        }

        Submission submission = new Submission();
        submission.setExamId(request.getExamId());
        submission.setStudentId(studentId);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission.setTotalScore(exam.getTotalMarks());

        List<Answer> answers = request.getAnswers().stream()
                .map(dto -> mapToAnswerEntity(dto, submission, exam))
                .collect(Collectors.toList());
        submission.setAnswers(answers);

        // Auto-grade objective questions
        int obtainedScore = autoGradeSubmission(answers, exam);
        submission.setObtainedScore(obtainedScore);
        submission.setStatus(Submission.SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        log.info("Submission saved with ID: {} for student {}", saved.getId(), studentId);

        return mapToSubmissionResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long submissionId, String studentId) {
        log.info("Fetching submission {} for student {}", submissionId, studentId);

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ExamNotFoundException("Submission not found with ID: " + submissionId));

        if (!submission.getStudentId().equals(studentId)) {
            throw new IllegalStateException("You are not authorized to view this submission");
        }

        return mapToSubmissionResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByStudent(String studentId) {
        log.info("Fetching all submissions for student {}", studentId);
        return submissionRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByExam(Long examId) {
        log.info("Fetching all submissions for exam {}", examId);
        return submissionRepository.findByExamId(examId)
                .stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    private void validateExamSubmission(Exam exam) {
        LocalDateTime now = LocalDateTime.now();

        if (exam.getStatus() != Exam.ExamStatus.ACTIVE && exam.getStatus() != Exam.ExamStatus.SCHEDULED) {
            throw new IllegalStateException("This exam is not accepting submissions");
        }

        if (now.isBefore(exam.getStartTime())) {
            throw new IllegalStateException("Exam has not started yet");
        }

        if (now.isAfter(exam.getEndTime())) {
            throw new IllegalStateException("Exam submission deadline has passed");
        }
    }

    private Answer mapToAnswerEntity(AnswerDto dto, Submission submission, Exam exam) {
        Answer answer = new Answer();
        answer.setSubmission(submission);
        answer.setQuestionId(dto.getQuestionId());
        answer.setAnswerText(dto.getAnswerText());

        // Find the question to check answer
        Question question = exam.getQuestions().stream()
                .filter(q -> q.getId().equals(dto.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + dto.getQuestionId()));

        // Auto-grade objective questions
        if (question.getType() == Question.QuestionType.MULTIPLE_CHOICE ||
                question.getType() == Question.QuestionType.TRUE_FALSE) {
            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(dto.getAnswerText().trim());
            answer.setIsCorrect(isCorrect);
            answer.setMarksAwarded(isCorrect ? question.getMarks() : 0);
        }

        return answer;
    }

    private int autoGradeSubmission(List<Answer> answers, Exam exam) {
        return answers.stream()
                .filter(answer -> answer.getIsCorrect() != null && answer.getIsCorrect())
                .mapToInt(Answer::getMarksAwarded)
                .sum();
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        List<AnswerDto> answerDtos = submission.getAnswers().stream()
                .map(this::mapToAnswerDto)
                .collect(Collectors.toList());

        return new SubmissionResponse(
                submission.getId(),
                submission.getExamId(),
                submission.getStudentId(),
                answerDtos,
                submission.getSubmittedAt(),
                submission.getStatus().toString(),
                submission.getTotalScore(),
                submission.getObtainedScore(),
                submission.getGradedAt(),
                submission.getFeedback()
        );
    }

    private AnswerDto mapToAnswerDto(Answer answer) {
        return new AnswerDto(
                answer.getId(),
                answer.getQuestionId(),
                answer.getAnswerText(),
                answer.getIsCorrect(),
                answer.getMarksAwarded()
        );
    }
}