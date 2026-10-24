package dev.prashant.ailab.eval;

import java.util.List;

public record EvalReport(int passed, int total, List<String> failures) {

	public boolean perfect() {
		return failures.isEmpty() && total > 0 && passed == total;
	}

}
