package fr.takit.reviewer.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodeSubmissionTest {

    @Test
    void refusesEmptyCode() {
        assertThatThrownBy(() -> new CodeSubmission("  \n "))
                .isInstanceOf(InvalidSubmissionException.class)
                .hasMessageContaining("Paste some Java code");
    }

    @Test
    void acceptsExactly200LinesAndRefuses201() {
        String twoHundred = "int a;\n".repeat(199) + "int b;";
        assertThat(new CodeSubmission(twoHundred).lines()).isEqualTo(200);
        assertThatThrownBy(() -> new CodeSubmission(twoHundred + "\nint c;"))
                .hasMessageContaining("200 lines");
    }

    @Test
    void refusesTooManyCharacters() {
        assertThatThrownBy(() -> new CodeSubmission("x".repeat(CodeSubmission.MAX_CHARS + 1)))
                .hasMessageContaining("characters");
    }

    @Test
    void numbersEachLineForTheModel() {
        assertThat(new CodeSubmission("class A {\n}").numbered())
                .contains("  1 | class A {")
                .contains("  2 | }");
    }
}
