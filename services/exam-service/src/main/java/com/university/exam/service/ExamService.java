package com.university.exam.service;

import com.university.exam.client.NotificationClient;
import com.university.exam.dto.AnswerDto;
import com.university.exam.dto.ExamCreateRequest;
import com.university.exam.dto.ExamSubmitRequest;
import com.university.exam.model.Exam;
import com.university.exam.model.ExamSubmission;
import com.university.exam.model.Question;
import com.university.exam.repository.ExamRepository;
import com.university.exam.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final SubmissionRepository submissionRepository;
    private final NotificationClient notificationClient;

    public ExamService(ExamRepository examRepository,
                       SubmissionRepository submissionRepository,
                       NotificationClient notificationClient) {
        this.examRepository = examRepository;
        this.submissionRepository = submissionRepository;
        this.notificationClient = notificationClient;
    }

    public Exam createExam(ExamCreateRequest request) {
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setInstructorId("instructor-1");

        List<Question> questions = request.getQuestions().stream()
                .map(dto -> {
                    Question q = new Question();
                    q.setText(dto.getText());
                    q.setCorrectAnswer(dto.getCorrectAnswer());
                    q.setExam(exam);
                    return q;
                }).collect(Collectors.toList());

        exam.setQuestions(questions);
        Exam savedExam = examRepository.save(exam);

        notificationClient.sendExamNotification(
                "آزمون جدید ایجاد شد: " + savedExam.getTitle() +
                " - شروع: " + savedExam.getStartTime()
        );

        return savedExam;
    }

    public ExamSubmission submitExam(Long examId, String studentId, ExamSubmitRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        Map<Long, String> answers = new HashMap<>();
        for (AnswerDto dto : request.getAnswers()) {
            answers.put(dto.getQuestionId(), dto.getAnswer());
        }

        int score = calculateScore(exam.getQuestions(), answers);

        ExamSubmission submission = new ExamSubmission();
        submission.setExam(exam);
        submission.setStudentId(studentId);
        submission.setScore(score);

        ExamSubmission savedSubmission = submissionRepository.save(submission);

        notificationClient.sendExamNotification(
                "دانشجو " + studentId + " آزمون \"" + exam.getTitle() + "\" را ارسال کرد. نمره: " + score
        );

        return savedSubmission;
    }

    private int calculateScore(List<Question> questions, Map<Long, String> answers) {
        int score = 0;
        for (Question question : questions) {
            String studentAnswer = answers.get(question.getId());
            if (studentAnswer != null && studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                score += 10;
            }
        }
        return score;
    }
}