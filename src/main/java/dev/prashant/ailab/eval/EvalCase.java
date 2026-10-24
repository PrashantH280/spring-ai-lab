package dev.prashant.ailab.eval;

public record EvalCase(String id, String exceptionText, String expectedCategory, String expectedKnownMatch,
		String requiredCheck) {
}
