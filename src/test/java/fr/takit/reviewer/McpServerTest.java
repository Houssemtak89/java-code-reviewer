package fr.takit.reviewer;

import fr.takit.reviewer.application.CodeReviewer;
import fr.takit.reviewer.domain.Finding;
import fr.takit.reviewer.domain.Review;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Starts the whole application and talks to the MCP endpoint the way an MCP client does. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.ai.openai.api-key=test", "reviewer.reviews-per-caller-per-day=50"})
class McpServerTest {

    @LocalServerPort
    int port;

    @MockitoBean
    CodeReviewer reviewer;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void listsAndCallsTheReviewTool() throws Exception {
        when(reviewer.review(any(), any())).thenReturn(new Review("Looks risky.", List.of(
                new Finding(Finding.Severity.MAJOR, Finding.Category.CONCURRENCY, 3, "Shared HashMap.", "Use ConcurrentHashMap."))));

        HttpResponse<String> init = post(null, """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18",
                 "capabilities":{},"clientInfo":{"name":"test","version":"1"}}}""");
        assertThat(init.statusCode()).isEqualTo(200);
        assertThat(init.body()).contains("java-code-reviewer");
        String session = init.headers().firstValue("Mcp-Session-Id").orElse(null);

        post(session, """
                {"jsonrpc":"2.0","method":"notifications/initialized"}""");

        HttpResponse<String> tools = post(session, """
                {"jsonrpc":"2.0","id":2,"method":"tools/list"}""");
        assertThat(tools.body()).contains("review_java_code");

        HttpResponse<String> call = post(session, """
                {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"review_java_code",
                 "arguments":{"code":"class A { Map<String,Integer> m = new HashMap<>(); }"}}}""");
        assertThat(call.body()).contains("Shared HashMap").contains("CONCURRENCY");
    }

    private HttpResponse<String> post(String session, String json) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/mcp"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (session != null) {
            request.header("Mcp-Session-Id", session);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
