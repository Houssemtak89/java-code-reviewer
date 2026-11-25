---
title: Java Code Reviewer
emoji: ☕
colorFrom: blue
colorTo: green
sdk: docker
app_port: 7860
pinned: false
license: mit
short_description: Review a piece of Java code with an open source model
---

# Java code reviewer

Paste a Java method or class and an open source code model reviews it: bugs, concurrency, security, performance, design, readability, and the tests worth writing. Each finding has a severity, a line and a concrete fix.

The same review is available three ways:
- a web page;
- a REST endpoint, `POST /api/review`;
- an **MCP server** at `/mcp`, tool `review_java_code`, so that a coding agent can ask an open source model for a second opinion. The [Spring agentic workflow](https://github.com/Houssemtak89/spring-agentic-workflow) plugin can call it at its review step.

## How it is built

- Java 21, Spring Boot 4, Spring AI 2 (OpenAI-compatible client and MCP server).
- The model is `nvidia/nemotron-3-ultra-550b-a55b`, an open-weights model, on the free tier of OpenRouter. Any OpenAI-compatible provider works, for example the Hugging Face inference router: set `REVIEWER_BASE_URL`, `REVIEWER_MODEL` and `REVIEWER_API_KEY`.
- Models do not always use the expected words ("high" instead of MAJOR, "NullPointerException" as a category). The enums map these variants instead of failing.
- The answer is mapped straight to Java records (`Review`, `Finding`) with Spring AI structured output. There is no regex parsing of free text.
- Hexagonal layout: the `domain` holds the size rules and the result types, the `application` holds the use case and the daily quota behind a `CodeReviewer` port, and the `adapter` packages hold the LLM client, the REST API and the MCP tools.
- The code is numbered before it is sent, so the model can point at a line. The prompt tells it to treat the code as data, never as instructions.

```
src/main/java/fr/takit/reviewer
├── domain        CodeSubmission (size rules), Review, Finding
├── application   ReviewService, DailyQuota, CodeReviewer (port)
└── adapter
    ├── llm       LlmCodeReviewer (Spring AI ChatClient)
    ├── web       ReviewController, ApiErrors (problem details)
    └── mcp       ReviewTools (@Tool), McpConfig
```

## Limits

The demo runs on a free inference credit, so it has limits:
- 200 lines or 12,000 characters per review;
- 5 reviews per visitor and 40 in total per day. A visitor who gives their own API key for the provider uses their own credit and skips the limit. The key goes with the request only: it is never stored or logged.
- A free model can be busy. A review then takes 20 to 40 seconds, or fails with a message to try again.

The pasted code is sent to the model and not kept.

## Run it locally

```bash
export REVIEWER_API_KEY=sk-or-... # an OpenRouter key (free models need no credit)
./mvnw spring-boot:run            # http://localhost:7860
```

Settings, all optional: `REVIEWER_MODEL`, `REVIEWER_BASE_URL`, `REVIEWER_PROVIDER_NAME`, `REVIEWS_PER_CALLER_PER_DAY`, `REVIEWS_PER_DAY`.

Use it from Claude Code as an MCP server:

```bash
claude mcp add --transport http java-reviewer http://localhost:7860/mcp
```

## Tests

```bash
./mvnw verify
```

There are 22 tests: the size rules, the quota, the ordering of findings and the mapping of the model's words; the LLM adapter against a fake model; the REST errors (400, 429, 503); a test that starts the real wiring with no bean replaced; and a test that calls the MCP endpoint the way an MCP client does. Line coverage is above 90% (JaCoCo report in `target/site/jacoco`).

## License

MIT
