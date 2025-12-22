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
        return examRepository.save(exam);
    }

    public ExamSubmission submit(Long examId, String studentId) {
        ExamSubmission s = new ExamSubmission();
        s.setExamId(examId);
        s.setStudentId(studentId);
        s.setScore(10);
        return submissionRepository.save(s);
    }
}
