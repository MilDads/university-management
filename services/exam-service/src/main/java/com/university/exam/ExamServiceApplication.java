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


public Exam createExam(Exam exam) {
Exam saved = examRepository.save(exam);
notificationClient.sendExamNotification(saved.getTitle());
return saved;
}


public ExamSubmission submitExam(Long examId, Map<Long, String> answers, String studentId) {
Exam exam = examRepository.findById(examId)
.orElseThrow(() -> new RuntimeException("Exam not found"));


int score = 0;
for (Question q : exam.getQuestions()) {
if (q.getCorrectAnswer().equals(answers.get(q.getId()))) {
score++;
}
}


ExamSubmission submission = new ExamSubmission();
submission.setExamId(examId);
submission.setStudentId(studentId);
submission.setScore(score);


return submissionRepository.save(submission);
}
}