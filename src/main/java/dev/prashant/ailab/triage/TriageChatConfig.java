package dev.prashant.ailab.triage;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TriageChatConfig {

	static final String SYSTEM = """
			You triage a Java stack trace for an on-call Spring Boot developer.
			When the trace names a known exception, call lookupKnownFailure with the simple class name before you answer.
			category must be one of CONFIG, DATA, CODE, DEPENDENCY, UNKNOWN.
			knownMatch is the catalog exception name, or an empty string when nothing matched.
			nextChecks are concrete files, properties, or commands to inspect.
			""";

	@Bean
	ChatClient triageChatClient(ChatClient.Builder builder, KnownFailureCatalog catalog, TriageTraceAdvisor advisor) {
		return builder.defaultSystem(SYSTEM).defaultTools(catalog).defaultAdvisors(advisor).build();
	}

}
