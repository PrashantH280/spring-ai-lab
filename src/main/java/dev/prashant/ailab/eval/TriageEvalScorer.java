package dev.prashant.ailab.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import dev.prashant.ailab.triage.TriageResult;

public class TriageEvalScorer {

	public EvalReport score(List<EvalCase> cases, Function<String, TriageResult> triage) {
		List<String> failures = new ArrayList<>();
		int passed = 0;
		for (EvalCase evalCase : cases) {
			TriageResult actual = triage.apply(evalCase.exceptionText());
			String reason = mismatch(evalCase, actual);
			if (reason == null) {
				passed++;
			}
			else {
				failures.add(evalCase.id() + ": " + reason);
			}
		}
		return new EvalReport(passed, cases.size(), List.copyOf(failures));
	}

	private static String mismatch(EvalCase evalCase, TriageResult actual) {
		if (!evalCase.expectedCategory().equals(actual.category())) {
			return "category was " + actual.category() + ", expected " + evalCase.expectedCategory();
		}
		String knownMatch = actual.knownMatch() == null ? "" : actual.knownMatch();
		if (!evalCase.expectedKnownMatch().equals(knownMatch)) {
			return "knownMatch was '" + knownMatch + "', expected '" + evalCase.expectedKnownMatch() + "'";
		}
		String required = evalCase.requiredCheck();
		if (required != null && !required.isBlank()) {
			String needle = required.toLowerCase(Locale.ROOT);
			boolean found = actual.nextChecks() != null && actual.nextChecks()
				.stream()
				.anyMatch(check -> check != null && check.toLowerCase(Locale.ROOT).contains(needle));
			if (!found) {
				return "nextChecks " + actual.nextChecks() + " missed '" + required + "'";
			}
		}
		return null;
	}

}
