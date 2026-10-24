package dev.prashant.ailab.triage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class KnownFailureCatalog {

	private final Map<String, KnownFailure> failures = new LinkedHashMap<>();

	private final AtomicInteger lookups = new AtomicInteger();

	public KnownFailureCatalog() {
		add(new KnownFailure("DataAccessResourceFailureException", "DEPENDENCY",
				"The database connection failed before a query ran.",
				List.of("Confirm the database process is up", "Check spring.datasource.url and the network path"),
				"DataAccessResourceFailureException"));
		add(new KnownFailure("BindException", "CONFIG",
				"A configuration value could not be bound to its target type.",
				List.of("Open application.yml and fix the property named in the exception",
						"Compare the value with the field type on the @ConfigurationProperties class"),
				"BindException"));
		add(new KnownFailure("UnsatisfiedDependencyException", "CODE",
				"Spring could not wire a required collaborator.",
				List.of("Find the missing bean type named in the stack",
						"Check that the bean's configuration class is component-scanned"),
				"UnsatisfiedDependencyException"));
		add(new KnownFailure("HttpMessageNotReadableException", "DATA",
				"The request body was not valid JSON for the controller argument.",
				List.of("Log the raw request body", "Compare the payload with the request record"),
				"HttpMessageNotReadableException"));
	}

	@Tool(description = "Look up a known Spring failure by its simple exception class name.")
	public KnownFailure lookupKnownFailure(
			@ToolParam(description = "Simple exception class name, such as BindException") String exceptionName) {
		lookups.incrementAndGet();
		KnownFailure found = failures.get(exceptionName);
		if (found != null) {
			return found;
		}
		return new KnownFailure(exceptionName, "UNKNOWN", "No catalog entry for " + exceptionName + ".",
				List.of("Capture the full stack trace and the request that triggered it"), "");
	}

	public String matchingName(String text) {
		String match = null;
		for (String name : failures.keySet()) {
			if (text.contains(name) && (match == null || name.length() > match.length())) {
				match = name;
			}
		}
		return match;
	}

	public int lookups() {
		return lookups.get();
	}

	public void resetLookups() {
		lookups.set(0);
	}

	private void add(KnownFailure failure) {
		failures.put(failure.exceptionName(), failure);
	}

}
