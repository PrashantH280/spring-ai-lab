package dev.prashant.ailab.triage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TriageTraceLog {

	private static final int LIMIT = 20;

	private final Deque<TriageTrace> traces = new ArrayDeque<>();

	public synchronized void add(TriageTrace trace) {
		traces.addFirst(trace);
		while (traces.size() > LIMIT) {
			traces.removeLast();
		}
	}

	public synchronized List<TriageTrace> recent() {
		return List.copyOf(traces);
	}

}
