package dev.prashant.ailab.triage;

import java.util.List;

public record TriageResult(String summary, String category, List<String> nextChecks, String knownMatch) {
}
