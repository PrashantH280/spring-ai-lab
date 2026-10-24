# spring-ai-lab

Incident triage for a Spring Boot on-call developer. Paste a stack trace, get a structured answer: category, summary, next checks, and the catalog entry that matched.

The default model is local and deterministic. It still runs through Spring AI `ChatClient`: a known exception becomes a real tool call, the tool result comes back as JSON, and `.entity(TriageResult.class)` parses the final answer. `mvn test` scores that path against `src/main/resources/evals/triage-cases.json`.

## Run

```powershell
mvn test
mvn spring-boot:run
```

```powershell
curl -s http://localhost:8080/api/triage -H "Content-Type: application/json" -d "{\"exceptionText\":\"org.springframework.dao.DataAccessResourceFailureException: Connection refused\"}"
curl -s http://localhost:8080/api/traces
```

Categories are `CONFIG`, `DATA`, `CODE`, `DEPENDENCY`, and `UNKNOWN`.

## Practice loop

1. Add a failing stack trace to `triage-cases.json` with the category and check you expect.
2. Run `mvn test` and read the assertion. The failure names the case and the field that missed.
3. Teach `KnownFailureCatalog` the new exception, or change the system prompt in `TriageChatConfig` when you are on a live model.
4. Re-run the suite. Keep the change only when the score is perfect.

`GET /api/traces` is the trace log from `TriageTraceAdvisor`. One user request is one entry, including the time spent inside the tool loop.

## Live model

```powershell
$env:OPENAI_API_KEY = "sk-..."
mvn spring-boot:run -Dspring-boot.run.profiles=live
```

`application-live.yml` points Spring AI at an OpenAI-compatible chat API. For a local server, set `OPENAI_BASE_URL` and `OPENAI_MODEL` as well. The controller, catalog tool, advisor, and eval file stay the same. The live model will miss cases the catalog model passes. That gap is the thing to study.
