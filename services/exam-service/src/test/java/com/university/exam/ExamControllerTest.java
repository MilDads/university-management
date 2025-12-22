@SpringBootTest
@AutoConfigureMockMvc
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheckUnauthorized() throws Exception {
        mockMvc.perform(get("/api/exams"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthCheckAuthorized() throws Exception {
        mockMvc.perform(get("/api/exams")
                .header("Authorization", "Bearer <valid_token>"))
                .andExpect(status().isOk());
    }
}
