/*
Copyright 2016 timbq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ch.timbq.intellijplugins.currentgitbranchdisplay;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.HintHint;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.popup.BalloonPopupBuilderImpl;
import com.intellij.util.containers.WeakHashMap;
import com.intellij.util.ui.UIUtil;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CurrentGitBranchDisplay extends AnAction {

    public static final String HTML_LINEBREAK = "<br>";
    public static final String HTML_BOLD_TAG = "<b>";
    public static final String HTML_BOLD_TAG_END = "</b>";

    @Override
    public void actionPerformed(AnActionEvent e) {

        if (e.getProject() == null) {
            return;
        }

        try {
            Collection<GitRepository> repos = getListOfRepositoriesWithActiveOnFirstPosition(e);
            String balloonMessage = constructBalloonMessage(repos);
            displayBalloonMessageIfNotEmpty(e.getProject(), balloonMessage);
        } catch (Exception ex) {
            Logger.getInstance(this.getClass()).error("Could not display current git branch.", ex);
        }
    }

    private Collection<GitRepository> getListOfRepositoriesWithActiveOnFirstPosition(AnActionEvent e) {
        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(e.getProject());
        Collection<GitRepository> repos = repositoryManager.getRepositories();

        VirtualFile activeFile = (VirtualFile) e.getDataContext().getData(DataKeys.VIRTUAL_FILE.getName());
        if (activeFile != null) {
            List<GitRepository> reposWithActiveFirst = new ArrayList<>(repos.size());
            GitRepository repoOfActiveFile = repositoryManager.getRepositoryForFile(activeFile);
            if(repoOfActiveFile != null) {
                reposWithActiveFirst.add(repoOfActiveFile);
                for (GitRepository repo : repos) {
                    if (repo.equals(repoOfActiveFile)) {
                        continue;
                    }
                    reposWithActiveFirst.add(repo);
                }
                repos = reposWithActiveFirst;
            }
        }
        return repos;
    }

    @VisibleForTesting
    String constructBalloonMessage(Collection<GitRepository> repos) {
        List<String> balloonMessageLines = new ArrayList<>(repos.size());

        for (GitRepository repo : repos) {
            StringBuilder balloonMessageLine = new StringBuilder("");
            if (repos.size() > 1) {
                // if there is more than one repo, also add the repo name
                balloonMessageLine.append(repo.getRoot().getName()).append(": ");
            }

            balloonMessageLine.append(HTML_BOLD_TAG).append(repo.getCurrentBranchName()).append(HTML_BOLD_TAG_END);

            // if there is more than one repo, separate the first line with an additional linebreak
            if (repos.size() > 1 && balloonMessageLines.size() < 1) {
                balloonMessageLine.append(HTML_LINEBREAK);
            }
            balloonMessageLines.add(balloonMessageLine.toString());
        }
        return Joiner.on(HTML_LINEBREAK).join(balloonMessageLines);
    }

    private void displayBalloonMessageIfNotEmpty(Project project, String balloonMessage) {
        if (!Strings.isNullOrEmpty(balloonMessage)) {
            BalloonBuilder htmlTextBalloonBuilder = createMyBalloonBuilder(balloonMessage, MessageType.INFO);
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            htmlTextBalloonBuilder.createBalloon().show(RelativePoint.getNorthEastOf(statusBar.getComponent()),
                    Balloon.Position.atRight);
        }
    }

    // use a special BalloonBuilder, because the standard one from JBPopupFactory doesn't allow to change the font size
    private BalloonBuilder createMyBalloonBuilder(String htmlContent, MessageType messageType) {

        // pick a bigger font
        HintHint fontHint = new HintHint().setFont(UIUtil.getToolTipFont().deriveFont(18F));

        JEditorPane text = IdeTooltipManager.initPane(htmlContent, fontHint, null);

        text.setEditable(false);
        NonOpaquePanel.setTransparent(text);
        text.setBorder(null);

        JLabel label = new JLabel();
        final JPanel content = new NonOpaquePanel(new BorderLayout((int) (label.getIconTextGap() * 1.5),
                (int) (label.getIconTextGap() * 1.5)));

        final NonOpaquePanel textWrapper = new NonOpaquePanel(new GridBagLayout());
        JBScrollPane scrolledText = new JBScrollPane(text);
        scrolledText.setBackground(messageType.getPopupBackground());
        scrolledText.getViewport().setBackground(messageType.getPopupBackground());
        scrolledText.getViewport().setBorder(null);
        scrolledText.setBorder(null);
        textWrapper.add(scrolledText);
        content.add(textWrapper, BorderLayout.CENTER);

        final NonOpaquePanel north = new NonOpaquePanel(new BorderLayout());
        north.add(new JLabel(messageType.getDefaultIcon()), BorderLayout.NORTH);
        content.add(north, BorderLayout.WEST);

        content.setBorder(new EmptyBorder(2, 4, 2, 4));

        final BalloonBuilder builder = new BalloonPopupBuilderImpl(new WeakHashMap<Disposable, List<Balloon>>(), content);

        builder.setFillColor(messageType.getPopupBackground());

        return builder;
    }
}
