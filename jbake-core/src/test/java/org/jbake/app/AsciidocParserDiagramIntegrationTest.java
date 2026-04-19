package org.jbake.app;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that {@code asciidoctor-diagram} is loaded correctly
 * when configured via {@code asciidoctor.option.requires} and that a PlantUML
 * sequence diagram in an AsciiDoc file is rendered to an image.
 *
 * <p>This test exercises the full path from configuration through
 * {@link AsciidoctorEngine#parseRequires} to Asciidoctor's {@code requireLibrary}
 * call. PlantUML sequence diagrams are used because they do not require an
 * external Graphviz installation.
 */
public class AsciidocParserDiagramIntegrationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private DefaultJBakeConfiguration config;
    private Parser parser;
    private File asciidocWithDiagram;

    private final String validHeader = "title=Diagram Test\nstatus=draft\ntype=post\ndate=2024-01-15\n~~~~~~";

    @Before
    public void setUp() throws Exception {
        File rootPath = TestUtils.getTestResourcesAsSourceFolder();
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(rootPath);
        config.setProperty("asciidoctor.option.requires", "asciidoctor-diagram");
        parser = new Parser(config);

        asciidocWithDiagram = folder.newFile("diagram-test.ad");
        try (PrintWriter out = new PrintWriter(asciidocWithDiagram)) {
            out.println(validHeader);
            out.println("= Diagram Test");
            out.println("");
            out.println("[plantuml,test-diagram,svg]");
            out.println("----");
            out.println("@startuml");
            out.println("Alice -> Bob: Hello");
            out.println("Bob --> Alice: Hi there");
            out.println("@enduml");
            out.println("----");
        }
    }

    @Test
    public void parsesAsciidocFileWithPlantUmlDiagram() {
        DocumentModel map = parser.processFile(asciidocWithDiagram);

        assertThat(map).isNotNull();
        assertThat(map.getStatus()).isEqualTo("draft");
        assertThat(map.getType()).isEqualTo("post");
        assertThat(map.getBody())
                .as("rendered body should contain an image reference for the diagram")
                .containsPattern("(?i)<img[^>]+test-diagram[^>]*>");
    }
}
