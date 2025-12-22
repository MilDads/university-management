@Entity
public class Exam {
@Id
@GeneratedValue
private Long id;


private String title;
private String instructorId;
private LocalDateTime startTime;
private LocalDateTime endTime;


@OneToMany(cascade = CascadeType.ALL)
private List<Question> questions;
}