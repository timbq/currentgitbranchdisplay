package ch.timbq.intellijplugins.currentgitbranchdisplay;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import git4idea.GitLocalBranch;
import git4idea.branch.GitBranchesCollection;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepoInfo;
import git4idea.repo.GitRepository;
import git4idea.repo.GitUntrackedFilesHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;


public class CurrentGitBranchDisplayTest {

    @Test
    public void constructBalloonMessageReposEmpty() {
        String expected = "";
        Collection<GitRepository> testrepos = new ArrayList<>();

        String result = new CurrentGitBranchDisplay().constructBalloonMessage(testrepos);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void constructBalloonMessageOneRoot() {
        String expected = "<b>currentBranchWithPrettyLongName</b>";
        Collection<GitRepository> testrepos = new ArrayList<>();
        testrepos.add(new MockRepo());

        String result = new CurrentGitBranchDisplay().constructBalloonMessage(testrepos);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void constructBalloonMessageMultipleRoots() {
        String expected = "gitrootfoldername: <b>currentBranchWithPrettyLongName</b><br><br>gitrootfoldername: <b>currentBranchWithPrettyLongName</b>";
        Collection<GitRepository> testrepos = new ArrayList<>();
        testrepos.add(new MockRepo());
        testrepos.add(new MockRepo());

        String result = new CurrentGitBranchDisplay().constructBalloonMessage(testrepos);
        Assert.assertEquals(expected, result);
    }

    class MockRepo implements GitRepository {

        @NotNull
        @Override
        public VirtualFile getGitDir() {
            return null;
        }

        @NotNull
        @Override
        public GitUntrackedFilesHolder getUntrackedFilesHolder() {
            return null;
        }

        @NotNull
        @Override
        public GitRepoInfo getInfo() {
            return null;
        }

        @Nullable
        @Override
        public GitLocalBranch getCurrentBranch() {
            return null;
        }

        @NotNull
        @Override
        public GitBranchesCollection getBranches() {
            return null;
        }

        @NotNull
        @Override
        public Collection<GitRemote> getRemotes() {
            return null;
        }

        @NotNull
        @Override
        public Collection<GitBranchTrackInfo> getBranchTrackInfos() {
            return null;
        }

        @Override
        public boolean isRebaseInProgress() {
            return false;
        }

        @Override
        public boolean isOnBranch() {
            return false;
        }

        @NotNull
        @Override
        public VirtualFile getRoot() {
            return new LightVirtualFile("gitrootfoldername");
        }

        @NotNull
        @Override
        public String getPresentableUrl() {
            return null;
        }

        @NotNull
        @Override
        public Project getProject() {
            return null;
        }

        @NotNull
        @Override
        public State getState() {
            return null;
        }

        @Nullable
        @Override
        public String getCurrentBranchName() {
            return "currentBranchWithPrettyLongName";
        }

        @Nullable
        @Override
        public AbstractVcs getVcs() {
            return null;
        }

        @Nullable
        @Override
        public String getCurrentRevision() {
            return null;
        }

        @Override
        public boolean isFresh() {
            return false;
        }

        @Override
        public void update() {

        }

        @NotNull
        @Override
        public String toLogString() {
            return null;
        }

        @Override
        public void dispose() {

        }
    }
}