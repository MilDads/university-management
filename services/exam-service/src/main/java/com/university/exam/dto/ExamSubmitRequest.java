import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class ExamSubmitRequest {

    @NotNull(message = "Answers cannot be null")
    private Map<Long, String> answers;
}
