package dev.prashant.ailab.triage;

import java.util.List;

public record KnownFailure(String exceptionName, String category, String summary, List<String> nextChecks,
		String knownMatch) {
}
