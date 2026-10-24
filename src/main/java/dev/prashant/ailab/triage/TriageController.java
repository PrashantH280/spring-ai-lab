package dev.prashant.ailab.triage;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TriageController {

	private final TriageService triageService;

	private final TriageTraceLog traces;

	public TriageController(TriageService triageService, TriageTraceLog traces) {
		this.triageService = triageService;
		this.traces = traces;
	}

	@PostMapping("/triage")
	public TriageResult triage(@Valid @RequestBody TriageRequest request) {
		return triageService.triage(request.exceptionText());
	}

	@GetMapping("/traces")
	public List<TriageTrace> traces() {
		return traces.recent();
	}

}
