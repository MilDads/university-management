import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Entity
public class ExamSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long examId;

    @NotNull
    private String studentId;

    @Min(0)
    @Max(100)
    private int score;
}
