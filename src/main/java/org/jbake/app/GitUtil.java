package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;

/**
 * Util class for executing git operations.
 * 
 * @author Alex Soto
 *
 */
public class GitUtil {

    /**
     * Inits a folder as git directory.
     * 
     * @param folder which is initialized as git repository.
     * @return Git instance.
     * @throws GitAPIException
     */
    public static Git initGit(File folder) throws GitAPIException {
        return Git.init().setDirectory(folder).call();
    }
    
    /**
     * Opens a git repo. 
     * 
     * @param folder git repository.
     * @return Git instance.
     * @throws IOException
     */
    public static Git openGitRepository(File folder) throws IOException {
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder().readEnvironment().findGitDir(folder);
        File gitDir = repositoryBuilder.getGitDir();
        return Git.open(gitDir);
    }

    /**
     * Adds remote configuration to git configuration file if it is not already set.
     * 
     * @param git instance.
     * @param remote name of remote upstream (origin is the most used).
     * @param remoteUrl of remote repo.
     */
    public static void addRemoteConfig(Git git, String remote, String remoteUrl) {
        StoredConfig storedConfig = git.getRepository().getConfig();
        
        if(storedConfig.getString("remote", remote, "url") == null) {
            storedConfig.setString("remote", remote, "url",
                    remoteUrl);
        }
    }
    
    /**
     * Checks if given directory is a git repo.
     * 
     * @param folder to check.
     * @return True if it is a git repo, false otherwise.
     * @throws IOException
     */
    public static boolean isGitDirectory(File folder) throws IOException {
        RepositoryBuilder repositoryBuilder = new RepositoryBuilder().readEnvironment().findGitDir(folder);
        return repositoryBuilder.getGitDir() != null;
    }

    /**
     * Adds element to stage area.
     * 
     * @param git instance.
     * @param pattern of elements to be added.
     * @throws GitAPIException
     */
    public static void add(Git git, String pattern) throws GitAPIException {
        git.add().addFilepattern(pattern).call();
    }

    /**
     * Commits elements previously added.
     * 
     * @param git instance.
     * @param message to be used as commit message.
     * @throws GitAPIException
     */
    public static void commit(Git git, String message) throws GitAPIException {
        git.commit().setMessage(message).call();
    }

    /**
     * Remove element from stage area.
     * 
     * @param git instance.
     * @param pattern of files to be removed.
     * @throws GitAPIException
     */
    public static void rm(Git git, String pattern) throws GitAPIException {
        git.rm().addFilepattern(pattern).call();
    }
    
    public static void addAndCommit(Git git, String pattern, String message) throws GitAPIException {
        add(git, pattern);
        commit(git, message);
    }
    
    public static void addAndRemove(Git git, String pattern, String message) throws GitAPIException {
        add(git, pattern);
        rm(git, message);
    }

    /**
     * Creates an orphan branch.
     * 
     * @param git instance.
     * @param branchName to be created.
     * @param personIdent information.
     * @return Ref created.
     * @throws IOException
     * @throws GitAPIException 
     */
    public static Ref createOrphanBranch(Git git, String branchName, PersonIdent personIdent) throws IOException, GitAPIException {

        Repository repository = git.getRepository();
        boolean success = false;

        String message = "Created branch " + branchName;
        ObjectInserter odi = repository.newObjectInserter();
        try {
            // Create a blob object to insert into a tree
            ObjectId blobId = odi.insert(Constants.OBJ_BLOB, message.getBytes(Constants.CHARACTER_ENCODING));

            // Create a tree object to reference from a commit
            TreeFormatter tree = new TreeFormatter();
            tree.append(".branch", FileMode.REGULAR_FILE, blobId);
            ObjectId treeId = odi.insert(tree);

            // Create a commit object
            CommitBuilder commit = new CommitBuilder();
            commit.setAuthor(personIdent);
            commit.setCommitter(personIdent);
            commit.setEncoding(Constants.CHARACTER_ENCODING);
            commit.setMessage(message);
            commit.setTreeId(treeId);

            // Insert the commit into the repository
            ObjectId commitId = odi.insert(commit);
            odi.flush();

            RevWalk revWalk = new RevWalk(repository);
            try {
                RevCommit revCommit = revWalk.parseCommit(commitId);
                if (!branchName.startsWith("refs/")) {
                    branchName = "refs/heads/" + branchName;
                }
                RefUpdate ru = repository.updateRef(branchName);
                ru.setNewObjectId(commitId);
                ru.setRefLogMessage("commit: " + revCommit.getShortMessage(), false);
                Result rc = ru.forceUpdate();
                switch (rc) {
                case NEW:
                case FORCED:
                case FAST_FORWARD:
                    success = true;
                    break;
                default:
                    success = false;
                }
                
                if(success) {
                    return ru.getRef();
                }
                
            } finally {
                revWalk.release();
            }
        } finally {
            odi.release();
        }

        return null;
    }

    /**
     * Checks if branch is created.
     * 
     * @param git instance.
     * @param branchName 
     * @return True if branch is created, fañse otherwise.
     * @throws GitAPIException
     */
    public static boolean isBranchCreated(Git git, String branchName) throws GitAPIException {
        List<Ref> refList = git.branchList().call();
        
        for (Ref ref : refList) {
            if(ref.getName().equals(branchName)) {
                return true;
            }
        }
        
        return false;
        
    }

    /**
     * Checkouts branch.
     * 
     * @param git instance.
     * @param branchName to checkout.
     * @throws GitAPIException
     */
    public static void checkoutBranch(Git git, String branchName) throws GitAPIException {
        git.checkout().setName(branchName).call();
    }

    /**
     * Forces a remove of given branch.
     * 
     * @param git instance.
     * @param branchName to be removed.
     * @throws GitAPIException
     */
    public static void forceRemoveBranch(Git git, String branchName) throws GitAPIException {
        git.branchDelete().setForce(true).call();
    }

    public static void checkoutAndRemove(Git git, String branchName) throws GitAPIException {
        checkoutBranch(git, branchName);
        forceRemoveBranch(git, branchName);
    }

    /**
     * Forces a push to given ref.
     * @param git instance.
     * @param credentialsProvider to authenticate against remote server.
     * @param ref to push.
     * @param remote name of remote upstream (origin is the most used).
     * @throws GitAPIException
     */
    public static void forcePush(Git git, CredentialsProvider credentialsProvider, Ref ref, String remote)
            throws GitAPIException {
        git.push().setCredentialsProvider(credentialsProvider).add(ref).setRemote(remote).setForce(true).call();
    }

    /**
     * Closes git repo.
     * @param git instance.
     */
    public static void closeRepo(Git git) {
        git.close();
    }

}
