package org.jbake;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class BuiltInProjectsTest {

    @TempDir
    private Path folder;
    private File projectFolder;
    private File templateFolder;
    private File outputFolder;
    private String jbakeExecutable;
    private BinaryRunner runner;

    @BeforeEach
    void setup() throws IOException {
        if (OS.current() == OS.WINDOWS) {
            jbakeExecutable = new File("build\\install\\jbake\\bin\\jbake.bat").getAbsolutePath();
        } else {
            jbakeExecutable = new File("build/install/jbake/bin/jbake").getAbsolutePath();
        }
        projectFolder = folder.resolve("project").toFile();
        Files.createDirectory(projectFolder.toPath());
        templateFolder = new File(projectFolder, "templates");
        outputFolder = new File(projectFolder, "output");
        runner = new BinaryRunner(projectFolder);
    }

    @ParameterizedTest
    @CsvSource({
        "thymeleaf,thyme",
        "freemarker,ftl",
        "jade,jade",
        "groovy,gsp",
        "groovy-mte,tpl"
    })
    void shouldBakeWithProject(String projectName, String extension) throws Exception {
        shouldInitProject(projectName, extension);
        shouldBakeProject();
    }

    private void shouldInitProject(String projectName, String extension) throws IOException, InterruptedException {
        Process process = runner.runWithArguments(jbakeExecutable, "-i", "-t", projectName);
        assertThat(process.exitValue()).isEqualTo(0);
        assertThat(new File(projectFolder, "jbake.properties")).exists();
        assertThat(new File(templateFolder, String.format("index.%s", extension))).exists();
        process.destroy();
    }

    private void shouldBakeProject() throws IOException, InterruptedException {
        Process process = runner.runWithArguments(jbakeExecutable, "-b");
        assertThat(process.exitValue()).isEqualTo(0);
        assertThat(new File(outputFolder, "index.html")).exists();
        process.destroy();
    }

}
