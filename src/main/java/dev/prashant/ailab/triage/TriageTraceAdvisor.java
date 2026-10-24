package dev.prashant.ailab.triage;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

/**
 * Wraps the whole chat call, including the tool loop, and keeps a short trace.
 * Order is outside {@code ToolCallingAdvisor} so one user request is one trace.
 */
@Component
public class TriageTraceAdvisor implements BaseAdvisor {

	private final TriageTraceLog traces;

	public TriageTraceAdvisor(TriageTraceLog traces) {
		this.traces = traces;
	}

	@Override
	public String getName() {
		return "triage-trace";
	}

	@Override
	public int getOrder() {
		return DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER - 100;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		return chatClientRequest;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		return chatClientResponse;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		long started = System.nanoTime();
		ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);
		long durationMillis = (System.nanoTime() - started) / 1_000_000;
		traces.add(new TriageTrace(preview(userText(chatClientRequest)), durationMillis, preview(responseText(response))));
		return response;
	}

	private static String userText(ChatClientRequest request) {
		StringBuilder text = new StringBuilder();
		for (Message message : request.prompt().getInstructions()) {
			if (message instanceof UserMessage user && user.getText() != null) {
				text.append(user.getText()).append(' ');
			}
		}
		return text.toString();
	}

	private static String responseText(ChatClientResponse response) {
		ChatResponse chatResponse = response.chatResponse();
		if (chatResponse == null || chatResponse.getResult() == null || chatResponse.getResult().getOutput() == null) {
			return "";
		}
		String text = chatResponse.getResult().getOutput().getText();
		return text == null ? "" : text;
	}

	private static String preview(String value) {
		String flat = value.replace('\n', ' ').trim();
		if (flat.length() <= 180) {
			return flat;
		}
		return flat.substring(0, 180);
	}

}
