package org.jenkinsci.plugins;

import java.io.IOException;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.buildnamesetter.Executor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class BuildNameStep extends Builder implements SimpleBuildStep {

    private final String buildName;

    @DataBoundConstructor
    public BuildNameStep(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildName() {
        return buildName;
    }

    @Override
    public void perform(Run run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws IOException {
        Executor executor = new Executor(run, listener);
        executor.setName(buildName);
    }

    @Symbol("setBuildName")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Changes build name";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }
}