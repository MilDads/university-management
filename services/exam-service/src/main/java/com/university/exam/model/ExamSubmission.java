@Entity
public class ExamSubmission {
@Id
@GeneratedValue
private Long id;


private Long examId;
private String studentId;
private int score;
}