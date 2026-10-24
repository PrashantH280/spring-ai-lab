package dev.prashant.ailab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import dev.prashant.ailab.eval.EvalCase;
import dev.prashant.ailab.eval.EvalReport;
import dev.prashant.ailab.eval.TriageEvalScorer;
import dev.prashant.ailab.triage.KnownFailureCatalog;
import dev.prashant.ailab.triage.TriageService;
import dev.prashant.ailab.triage.TriageTraceLog;

@SpringBootTest
@AutoConfigureMockMvc
class TriageEvalTest {

	@Autowired
	private TriageService triageService;

	@Autowired
	private KnownFailureCatalog catalog;

	@Autowired
	private TriageTraceLog traces;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void catalogModelPassesTheEvalSuite() throws Exception {
		catalog.resetLookups();
		List<EvalCase> cases = jsonMapper.readValue(new ClassPathResource("evals/triage-cases.json").getContentAsByteArray(),
				new TypeReference<List<EvalCase>>() {
				});

		EvalReport report = new TriageEvalScorer().score(cases, triageService::triage);

		assertThat(report.failures()).as(report.toString()).isEmpty();
		assertThat(report.passed()).isEqualTo(cases.size());
		assertThat(catalog.lookups()).isPositive();
		assertThat(traces.recent()).isNotEmpty();
	}

	@Test
	void httpTriageReturnsStructuredJson() throws Exception {
		mockMvc.perform(post("/api/triage").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"exceptionText":"org.springframework.dao.DataAccessResourceFailureException: Connection refused"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.category").value("DEPENDENCY"))
			.andExpect(jsonPath("$.knownMatch").value("DataAccessResourceFailureException"));
	}

	@Test
	void blankExceptionIsRejected() throws Exception {
		mockMvc.perform(post("/api/triage").contentType(MediaType.APPLICATION_JSON).content("""
				{"exceptionText":"  "}
				""")).andExpect(status().isBadRequest());
	}

}
