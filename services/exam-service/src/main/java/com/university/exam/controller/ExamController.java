@RestController
@RequestMapping("/exams")
public class ExamController {


private final ExamService examService;


public ExamController(ExamService examService) {
this.examService = examService;
}


@PostMapping
@PreAuthorize("hasRole('INSTRUCTOR')")
public Exam createExam(@RequestBody Exam exam) {
return examService.createExam(exam);
}


@PostMapping("/{id}/submit")
@PreAuthorize("hasRole('STUDENT')")
public ExamSubmission submitExam(
@PathVariable Long id,
@RequestBody Map<Long, String> answers,
@RequestHeader("X-User-Id") String studentId) {
return examService.submitExam(id, answers, studentId);
}
}