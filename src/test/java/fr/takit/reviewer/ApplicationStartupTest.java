package fr.takit.reviewer;

import fr.takit.reviewer.adapter.mcp.ReviewTools;
import fr.takit.reviewer.application.CodeReviewer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the real wiring, with no bean replaced: the Spring AI chat model, the tool calling
 * manager and the MCP tools together. A mocked reviewer once hid a dependency cycle between them.
 */
@SpringBootTest(properties = "spring.ai.openai.api-key=test")
class ApplicationStartupTest {

    @Autowired
    CodeReviewer reviewer;

    @Autowired
    ReviewTools tools;

    @Test
    void startsWithTheRealBeans() {
        assertThat(reviewer.getClass().getSimpleName()).isEqualTo("LlmCodeReviewer");
        assertThat(tools).isNotNull();
    }
}
