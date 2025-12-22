@Entity
public class Question {
@Id
@GeneratedValue
private Long id;


private String text;
private String correctAnswer;
}