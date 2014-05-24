package org.jbake.publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;

import org.apache.commons.configuration.Configuration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jbake.app.FileUtil;
import org.jbake.app.GitUtil;
import org.jbake.launcher.LaunchOptions;
import org.jbake.spi.Publisher;

public class GithubPublisher implements Publisher {

    private static final String GH_PAGES = "gh-pages";
    private static final String GIT_MASTER = "git.master";
    private static final String GIT_USER = "git.user";
    private static final String GIT_EMAIL = "git.email";
    private static final String GIT_PASSWORD = "git.password";
    private static final String GITHUB_URL = "github.url";
    
    private static final String PUBLISHER_NAME = "github";
    
    private Configuration configuration;
    private File sourceDirectory;
    private File outputDirectory;
    private LaunchOptions launchOptions;
    
    @Override
    public String publisherName() {
        return PUBLISHER_NAME;
    }
    
    @Override
    public void publish(Configuration configuration, LaunchOptions options) {
        
        initializeAttributes(configuration, options);
        
        try {
            ensureDestination();
            publishToGhPagesBranch();
        } catch (IOException e) {
            throw new PublisherException(e);
        }
        
    }

    private void initializeAttributes(Configuration configuration, LaunchOptions options) {
        this.configuration = configuration;
        this.launchOptions = options;
        this.sourceDirectory = options.getSource();
        this.outputDirectory = options.getDestination();
    }

    private void ensureDestination() {
        if(this.outputDirectory == null) {
            outputDirectory = new File(configuration.getString("destination.folder"));
        }
    }
    
    private void publishToGhPagesBranch() throws IOException {
        
        if(GitUtil.isGitDirectory(sourceDirectory)) {
            Git git = GitUtil.openGitRepository(sourceDirectory);
            
            try {
                
                String username = this.configuration.getString(GIT_USER);
                String email = this.configuration.getString(GIT_EMAIL);
                
                if(GitUtil.isBranchCreated(git, GH_PAGES)) {
                    GitUtil.forceRemoveBranch(git, "refs/heads/"+GH_PAGES);
                }

                Ref ref = GitUtil.createOrphanBranch(git, GH_PAGES, new PersonIdent(username, email));
                
                String workingBranch = this.configuration.getString(GIT_MASTER, Constants.MASTER);
                GitUtil.checkoutBranch(git, workingBranch);
                
                Path tempDirectory = FileUtil.copyRecursiveToTempDirectory(this.outputDirectory.toPath());
                
                GitUtil.checkoutBranch(git, GH_PAGES);
                FileUtil.copyRecursive(tempDirectory, git.getRepository().getDirectory().getParentFile().toPath());
                
                GitUtil.addAndCommit(git, ".", "JBake " + this.configuration.getString("version") + " (" + this.configuration.getString("build.timestamp") + ") [http://jbake.org]");
                
                String githubUrl = this.configuration.getString(GITHUB_URL, "");
                
                if(!"".equals(githubUrl)) {
                    sendToRemote(git, ref, githubUrl);
                }
                
                GitUtil.checkoutBranch(git, workingBranch);
                
            } catch (GitAPIException e) {
                throw new PublisherException(e);
            } finally {
                git.close();
            }
            
        } else {
            throw new PublisherException(MessageFormat.format("Source directory {0} must be a git repository in order to publish to github.", sourceDirectory.getAbsolutePath()));
        }
    }
    
    private void sendToRemote(Git git, Ref ref, String remoteUrl) throws GitAPIException {
        GitUtil.addRemoteConfig(git, Constants.DEFAULT_REMOTE_NAME, remoteUrl);
        GitUtil.forcePush(git, getCredentialsProvider(), ref, Constants.DEFAULT_REMOTE_NAME);
    }
    
    private CredentialsProvider getCredentialsProvider() {
        
        String username = this.configuration.getString(GIT_USER, "");
        
        String password = "";
        if(this.launchOptions.isPublisherParameters() && this.launchOptions.getPublisherParams().containsKey(GIT_PASSWORD)) {    
            password = this.launchOptions.getPublisherParams().get(GIT_PASSWORD);
        } else {
            password = this.configuration.getString(GIT_PASSWORD, "");
        }
        
        return new UsernamePasswordCredentialsProvider(username, password);
        
    }
    
}
