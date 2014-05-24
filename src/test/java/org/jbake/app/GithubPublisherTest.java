package org.jbake.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.MapConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.jbake.launcher.LaunchOptions;
import org.jbake.publisher.GithubPublisher;
import org.jbake.spi.Publisher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class GithubPublisherTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void publishContentToGhPagesBranch() throws IOException, GitAPIException {

        File currentDirectory = temporaryFolder.newFolder();
        File contentDirectory = new File(currentDirectory, "content");
        contentDirectory.mkdir();
        File contentFile = new File(contentDirectory, "a.html");

        Files.write(contentFile.toPath(), "<p>Hello World</p>".getBytes());

        Git git = GitUtil.initGit(currentDirectory);
        GitUtil.addAndCommit(git, ".", "First commit");

        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("git.user", "Alex");
        configuration.put("git.email", "alex@alex.com");

        compositeConfiguration.append(new MapConfiguration(configuration));

        LaunchOptions launchOptions = parseArguments(new String[] {currentDirectory.getAbsolutePath(), contentDirectory.getAbsolutePath()});
        Publisher githubPublisher = new GithubPublisher();
        githubPublisher.publish(compositeConfiguration, launchOptions);

        GitUtil.checkoutBranch(git, "gh-pages");
        assertThat(new File(currentDirectory, "a.html")).exists();

    }

    @Test
    public void publishContentToAlreadyCreatedGhPages() throws IOException, GitAPIException {
        
        File currentDirectory = temporaryFolder.newFolder();
        File contentDirectory = new File(currentDirectory, "content");
        contentDirectory.mkdir();
        File contentFile = new File(contentDirectory, "a.html");
        
        Files.write(contentFile.toPath(), "<p>Hello World</p>".getBytes());
        
        Git git = GitUtil.initGit(currentDirectory);
        GitUtil.addAndCommit(git, ".", "First commit");
        
        GitUtil.createOrphanBranch(git, "gh-pages", new PersonIdent("Alex", "Alex@alex.com"));
        GitUtil.checkoutBranch(git, "gh-pages");
        Files.write(new File(currentDirectory, "b.html").toPath(), "<p>Hello World</p>".getBytes());
        GitUtil.addAndCommit(git, ".", "First Commit");
        
        
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("git.user", "Alex");
        configuration.put("git.email", "alex@alex.com");
        
        compositeConfiguration.append(new MapConfiguration(configuration));
        
                
        Publisher githubPublisher = new GithubPublisher();
        LaunchOptions launchOptions = parseArguments(new String[] {currentDirectory.getAbsolutePath(), contentDirectory.getAbsolutePath()});
        githubPublisher.publish(compositeConfiguration, launchOptions);
     
        GitUtil.checkoutBranch(git, "gh-pages");
        
        assertThat(new File(currentDirectory, "a.html")).exists();
    }

    @Test
    public void publishContentToGhPagesBranchWithDeepContent() throws IOException, GitAPIException {

        File currentDirectory = temporaryFolder.newFolder();
        File projectDirectory = new File(currentDirectory, "jbake");
        File contentDirectory = new File(projectDirectory, "content");
        contentDirectory.mkdirs();
        File contentFile = new File(contentDirectory, "a.html");

        Files.write(contentFile.toPath(), "<p>Hello World</p>".getBytes());

        Git git = GitUtil.initGit(currentDirectory);
        GitUtil.addAndCommit(git, ".", "First commit");

        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("git.user", "Alex");
        configuration.put("git.email", "alex@alex.com");

        compositeConfiguration.append(new MapConfiguration(configuration));

        Publisher githubPublisher = new GithubPublisher();
        LaunchOptions launchOptions = parseArguments(new String[] {currentDirectory.getAbsolutePath(), contentDirectory.getAbsolutePath()});
        githubPublisher.publish(compositeConfiguration, launchOptions);
        

        GitUtil.checkoutBranch(git, "gh-pages");
        assertThat(new File(currentDirectory, "a.html")).exists();

    }

    private LaunchOptions parseArguments(String[] args) {
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
        }

        return res;
    }
}
