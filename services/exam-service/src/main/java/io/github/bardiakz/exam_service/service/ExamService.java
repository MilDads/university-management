package io.github.bardiakz.exam_service.service;

import io.github.bardiakz.exam_service.dto.*;
import io.github.bardiakz.exam_service.entity.Exam;
import io.github.bardiakz.exam_service.entity.Question;
import io.github.bardiakz.exam_service.event.ExamCreatedEvent;
import io.github.bardiakz.exam_service.exception.ExamNotFoundException;
import io.github.bardiakz.exam_service.exception.UnauthorizedException;
import io.github.bardiakz.exam_service.repository.ExamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {
    private static final Logger log = LoggerFactory.getLogger(ExamService.class);

    private final ExamRepository examRepository;
    private final NotificationService notificationService;

    public ExamService(ExamRepository examRepository, NotificationService notificationService) {
        this.examRepository = examRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ExamResponse createExam(ExamRequest request, String instructorId) {

        log.info("Creating exam '{}' by instructor {}", request.getTitle(), instructorId);

        validateExamDates(request.getStartTime(), request.getEndTime());

        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setInstructorId(instructorId);
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setTotalMarks(request.getTotalMarks());
        exam.setStatus(Exam.ExamStatus.DRAFT);

        List<Question> questions = request.getQuestions().stream()
                .map(dto -> mapToQuestionEntity(dto, exam))
                .collect(Collectors.toList());
        exam.setQuestions(questions);

        Exam saved = examRepository.save(exam);
        log.info("Exam created successfully with ID: {}", saved.getId());

        return mapToExamResponse(saved);
    }

    @Transactional
    public ExamResponse publishExam(Long examId, String instructorId) {
        log.info("Publishing exam ID: {} by instructor {}", examId, instructorId);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with ID: " + examId));

        if (!exam.getInstructorId().equals(instructorId)) {
            throw new UnauthorizedException("You are not authorized to publish this exam");
        }

        if (exam.getStatus() != Exam.ExamStatus.DRAFT) {
            throw new IllegalStateException("Only draft exams can be published");
        }

        exam.setStatus(Exam.ExamStatus.SCHEDULED);
        Exam saved = examRepository.save(exam);

        // Send notification via Circuit Breaker protected method
        ExamCreatedEvent event = new ExamCreatedEvent(
                "ExamCreated",
                saved.getId(),
                saved.getTitle(),
                saved.getStartTime(),
                saved.getDurationMinutes(),
                Collections.emptyList(),
                LocalDateTime.now()
        );

        notificationService.notifyExamCreated(event);

        log.info("Exam published successfully with ID: {}", saved.getId());
        return mapToExamResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExamResponse getExamById(Long examId, String userId, String role) {
        log.info("Fetching exam ID: {} for user {} with role {}", examId, userId, role);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with ID: " + examId));

        // Instructors can see their own exams, students can see published exams
        if (role.equals("INSTRUCTOR") && !exam.getInstructorId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this exam");
        }

        if (role.equals("STUDENT") && exam.getStatus() == Exam.ExamStatus.DRAFT) {
            throw new UnauthorizedException("This exam is not yet published");
        }

        return mapToExamResponse(exam);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getExamsByInstructor(String instructorId) {
        log.info("Fetching all exams for instructor {}", instructorId);
        return examRepository.findByInstructorId(instructorId)
                .stream()
                .map(this::mapToExamResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getActiveExams() {
        log.info("Fetching all active exams");
        return examRepository.findActiveExams(LocalDateTime.now())
                .stream()
                .map(this::mapToExamResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getUpcomingExams() {
        log.info("Fetching all upcoming exams");
        return examRepository.findUpcomingExams(LocalDateTime.now())
                .stream()
                .map(this::mapToExamResponse)
                .collect(Collectors.toList());
    }

    private void validateExamDates(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
    }

    private Question mapToQuestionEntity(QuestionDto dto, Exam exam) {
        Question question = new Question();
        question.setExam(exam);
        question.setText(dto.getText());
        question.setType(dto.getType());
        question.setOptions(dto.getOptions());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setMarks(dto.getMarks());
        question.setOrderNumber(dto.getOrderNumber());
        return question;
    }

    private ExamResponse mapToExamResponse(Exam exam) {
        List<QuestionDto> questionDtos = exam.getQuestions().stream()
                .map(this::mapToQuestionDto)
                .collect(Collectors.toList());

        return new ExamResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getInstructorId(),
                exam.getStartTime(),
                exam.getEndTime(),
                exam.getDurationMinutes(),
                exam.getTotalMarks(),
                exam.getStatus(),
                questionDtos,
                exam.getCreatedAt(),
                exam.getUpdatedAt()
        );
    }

    private QuestionDto mapToQuestionDto(Question question) {
        return new QuestionDto(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getOptions(),
                question.getCorrectAnswer(),
                question.getMarks(),
                question.getOrderNumber()
        );
    }
}