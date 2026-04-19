package org.jbake.parser;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AsciidoctorEngine#parseRequires(Object)}.
 *
 * <p>These tests pin the behaviour of the {@code asciidoctor.option.requires} option
 * parsing. The option value is produced by {@code DefaultJBakeConfiguration.getAsciidoctorOption()}
 * as a {@code List<String>}, but the option was historically documented as a
 * comma-separated {@code String}. A prior regression caused {@code String.valueOf(list)}
 * to be passed through unchanged, producing values like {@code "[asciidoctor-diagram]"}
 * that the JRuby {@code require} call could not resolve. Both forms must be supported,
 * and no parsed entry may ever contain the stray brackets introduced by
 * {@code List.toString()}.
 */
public class AsciidoctorEngineTest {

    @Test
    public void parsesSingleElementListFromConfiguration() {
        List<String> result = AsciidoctorEngine.parseRequires(Collections.singletonList("asciidoctor-diagram"));

        assertThat(result).containsExactly("asciidoctor-diagram");
    }

    @Test
    public void parsesMultiElementListFromConfiguration() {
        List<String> result = AsciidoctorEngine.parseRequires(Arrays.asList("asciidoctor-diagram", "asciidoctor-mathematical"));

        assertThat(result).containsExactly("asciidoctor-diagram", "asciidoctor-mathematical");
    }

    @Test
    public void parsesCommaSeparatedString() {
        List<String> result = AsciidoctorEngine.parseRequires("asciidoctor-diagram,asciidoctor-mathematical");

        assertThat(result).containsExactly("asciidoctor-diagram", "asciidoctor-mathematical");
    }

    @Test
    public void trimsWhitespaceAroundEntries() {
        List<String> result = AsciidoctorEngine.parseRequires("  asciidoctor-diagram ,  asciidoctor-mathematical  ");

        assertThat(result).containsExactly("asciidoctor-diagram", "asciidoctor-mathematical");
    }

    @Test
    public void skipsEmptyAndNullEntries() {
        List<String> result = AsciidoctorEngine.parseRequires(Arrays.asList("asciidoctor-diagram", "", null, "  "));

        assertThat(result).containsExactly("asciidoctor-diagram");
    }

    @Test
    public void returnsEmptyListForNullInput() {
        assertThat(AsciidoctorEngine.parseRequires(null)).isEmpty();
    }

    @Test
    public void returnsEmptyListForEmptyString() {
        assertThat(AsciidoctorEngine.parseRequires("")).isEmpty();
    }

    /**
     * Regression guard for the original bug: when the option value is a {@code List},
     * no parsed entry may contain the literal {@code [} or {@code ]} characters that
     * {@code List.toString()} would introduce. Had the previous implementation been
     * covered by this test, the regression would have been caught immediately.
     */
    @Test
    public void listInputNeverLeaksToStringBrackets() {
        List<String> result = AsciidoctorEngine.parseRequires(Collections.singletonList("asciidoctor-diagram"));

        assertThat(result).isNotEmpty();
        for (String require : result) {
            assertThat(require)
                    .as("parsed require entry must not contain List.toString() brackets")
                    .doesNotContain("[")
                    .doesNotContain("]");
        }
    }
}
