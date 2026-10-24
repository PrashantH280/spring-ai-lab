package dev.prashant.ailab.triage;

import java.util.List;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Deterministic stand-in for a chat model. It still goes through ChatClient, so tool
 * calling and structured output run for real. A known exception becomes a tool call.
 * The follow-up turn copies the tool JSON into {@link TriageResult}. Anything else is
 * UNKNOWN.
 */
@Component
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "catalog", matchIfMissing = true)
public class CatalogChatModel implements ChatModel {

	private final KnownFailureCatalog catalog;

	private final JsonMapper jsonMapper;

	public CatalogChatModel(KnownFailureCatalog catalog, JsonMapper jsonMapper) {
		this.catalog = catalog;
		this.jsonMapper = jsonMapper;
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		String toolJson = latestToolJson(prompt);
		if (toolJson != null) {
			return answer(jsonMapper.readValue(toolJson, KnownFailure.class));
		}

		String userText = userText(prompt);
		String knownName = catalog.matchingName(userText);
		if (knownName != null) {
			AssistantMessage toolCall = AssistantMessage.builder()
				.content("")
				.toolCalls(List.of(new AssistantMessage.ToolCall("call-catalog", "function", "lookupKnownFailure",
						"{\"exceptionName\":\"" + knownName + "\"}")))
				.build();
			return new ChatResponse(List.of(new Generation(toolCall)));
		}

		return answer(new TriageResult("No catalog entry matches this exception.", "UNKNOWN",
				List.of("Capture the full stack trace", "Reproduce the failing request"), ""));
	}

	@Override
	public ChatOptions getOptions() {
		return ToolCallingChatOptions.builder().build();
	}

	private ChatResponse answer(KnownFailure failure) {
		return answer(new TriageResult(failure.summary(), failure.category(), failure.nextChecks(),
				failure.knownMatch()));
	}

	private ChatResponse answer(TriageResult result) {
		AssistantMessage message = new AssistantMessage(jsonMapper.writeValueAsString(result));
		return new ChatResponse(List.of(new Generation(message)));
	}

	private static String latestToolJson(Prompt prompt) {
		List<Message> messages = prompt.getInstructions();
		for (int i = messages.size() - 1; i >= 0; i--) {
			if (messages.get(i) instanceof ToolResponseMessage tool && !tool.getResponses().isEmpty()) {
				return tool.getResponses().get(0).responseData();
			}
		}
		return null;
	}

	private static String userText(Prompt prompt) {
		StringBuilder text = new StringBuilder();
		for (Message message : prompt.getInstructions()) {
			if (message instanceof UserMessage user && user.getText() != null) {
				text.append(user.getText()).append('\n');
			}
		}
		return text.toString();
	}

}
