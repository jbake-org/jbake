package org.jbake;

import org.apache.commons.vfs2.util.Os;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class BuiltInProjectsTest {

    @Parameters(name = " {0} ")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "thymeleaf",  "thyme" },
                { "freemarker", "ftl" },
                { "jade",       "jade" },
                { "groovy",     "gsp" },
                { "groovy-mte", "tpl" }
        });
    }

    @Parameter
    public String projectName;

    @Parameter(1)
    public String extension;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File projectFolder;
    private File templateFolder;
    private File outputFolder;
    private String jbakeExecutable;
    private BinaryRunner runner;

    @Before
    public void setup() throws IOException {
        if ( Os.isFamily(Os.OS_FAMILY_WINDOWS) ) {
            jbakeExecutable = new File("build\\install\\jbake\\bin\\jbake.bat").getAbsolutePath();
        }
        else {
            jbakeExecutable = new File("build/install/jbake/bin/jbake").getAbsolutePath();
        }
        projectFolder = folder.newFolder("project");
        templateFolder = new File(projectFolder, "templates");
        outputFolder = new File(projectFolder, "output");
        runner = new BinaryRunner(projectFolder);
    }

    @Test
    public void should_bake_with_project() throws Exception {
        shouldInitProject(projectName, extension);
        shouldBakeProject();
    }

    private void shouldInitProject(String projectName, String extension) throws IOException, InterruptedException {
        Process process = runner.runWithArguments(jbakeExecutable,"-i", "-t", projectName);
        assertThat(process.exitValue()).isEqualTo(0);
        assertThat(new File(projectFolder,"jbake.properties")).exists();
        assertThat(new File(templateFolder, String.format("index.%s", extension))).exists();
        process.destroy();
    }

    private void shouldBakeProject() throws IOException, InterruptedException {
        Process process = runner.runWithArguments(jbakeExecutable,"-b");
        assertThat(process.exitValue()).isEqualTo(0);
        assertThat(new File(outputFolder, "index.html")).exists();
        process.destroy();
    }

}
