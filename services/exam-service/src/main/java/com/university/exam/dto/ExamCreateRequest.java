@Data
public class ExamCreateRequest {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<QuestionDto> questions;
}
