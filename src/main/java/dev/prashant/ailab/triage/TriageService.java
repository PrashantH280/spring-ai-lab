package dev.prashant.ailab.triage;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TriageService {

	private final ChatClient chatClient;

	public TriageService(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	public TriageResult triage(String exceptionText) {
		TriageResult result = chatClient.prompt().user(exceptionText).call().entity(TriageResult.class);
		if (result == null) {
			throw new IllegalStateException("Model returned an empty triage");
		}
		return result;
	}

}
