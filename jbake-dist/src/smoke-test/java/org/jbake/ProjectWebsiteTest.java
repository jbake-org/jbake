package org.jbake;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectWebsiteTest {

    private static final String WEBSITE_REPO_URL = "https://github.com/jbake-org/jbake.org.git";

    @TempDir
    private Path folder;
    private File projectFolder;
    private File outputFolder;
    private String jbakeExecutable;
    private BinaryRunner runner;

    @BeforeEach
    void setup() throws IOException, GitAPIException {
        if (OS.current() == OS.WINDOWS) {
            jbakeExecutable = new File("build\\install\\jbake\\bin\\jbake.bat").getAbsolutePath();
        } else {
            jbakeExecutable = new File("build/install/jbake/bin/jbake").getAbsolutePath();
        }
        projectFolder = folder.resolve("project").toFile();
        new File(projectFolder, "templates");
        outputFolder = new File(projectFolder, "output");

        runner = new BinaryRunner(projectFolder);
        cloneJbakeWebsite();

    }

    @Test
    void shouldBakeWebsite() throws IOException, InterruptedException {
        Process process = runner.runWithArguments(jbakeExecutable, "-b");
        assertThat(process.exitValue()).isEqualTo(0);
        assertThat(new File(outputFolder, "index.html")).exists();
        process.destroy();
    }

    private void cloneJbakeWebsite() throws GitAPIException {
        CloneCommand cmd = Git.cloneRepository();
        cmd.setBare(false);
        cmd.setBranch("master");
        cmd.setRemote("origin");
        cmd.setURI(WEBSITE_REPO_URL);
        cmd.setDirectory(projectFolder);

        cmd.call();

        assertThat(new File(projectFolder, "README.md").exists()).isTrue();
    }

}
