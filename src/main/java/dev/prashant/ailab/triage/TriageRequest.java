package dev.prashant.ailab.triage;

import jakarta.validation.constraints.NotBlank;

public record TriageRequest(@NotBlank String exceptionText) {
}
