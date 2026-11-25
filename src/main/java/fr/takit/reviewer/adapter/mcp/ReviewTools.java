package fr.takit.reviewer.adapter.mcp;

import fr.takit.reviewer.application.ReviewService;
import fr.takit.reviewer.domain.Review;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The same review, exposed as an MCP tool, so that a coding agent (Claude Code, for example the
 * Spring agentic workflow at its review step) can ask an open source model for a second opinion.
 *
 * <p>The service is looked up at call time: Spring AI also offers every tool to the chat model,
 * so a direct dependency would make the chat model depend on the service that uses it.
 */
@Component
public class ReviewTools {

    static final String MCP_CALLER = "mcp";

    private final ObjectProvider<ReviewService> service;

    public ReviewTools(ObjectProvider<ReviewService> service) {
        this.service = service;
    }

    @Tool(name = "review_java_code",
          description = "Reviews a piece of Java code (200 lines at most) with an open source code model and returns "
                  + "a summary and findings with severity, category, line, message and suggestion.")
    public Review reviewJavaCode(@ToolParam(description = "The Java code to review") String code) {
        return service.getObject().review(code, MCP_CALLER, null);
    }
}
