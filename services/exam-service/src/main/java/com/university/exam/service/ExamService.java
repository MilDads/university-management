@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final SubmissionRepository submissionRepository;

    public ExamService(ExamRepository examRepository,
                       SubmissionRepository submissionRepository) {
        this.examRepository = examRepository;
        this.submissionRepository = submissionRepository;
    }

    public Exam createExam(Exam exam) {
        if (examRepository.existsById(exam.getId())) {
            throw new IllegalArgumentException("Exam with this ID already exists.");
        }
        return examRepository.save(exam);
    }

    public ExamSubmission submit(Long examId, String studentId, Map<Long, String> answers) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        int score = calculateScore(answers);

        ExamSubmission submission = new ExamSubmission();
        submission.setExamId(examId);
        submission.setStudentId(studentId);
        submission.setScore(score);

        return submissionRepository.save(submission);
    }

    private int calculateScore(Map<Long, String> answers) {
        return 10;
    }
}
