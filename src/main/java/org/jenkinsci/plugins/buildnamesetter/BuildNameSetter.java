package org.jenkinsci.plugins.buildnamesetter;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Set the name twice.
 *
 * Once early on in the build, and another time later on.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildNameSetter extends BuildWrapper {

    public String template;

    @DataBoundConstructor
    public BuildNameSetter(String template) {
        this.template = template;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        setDisplayName(build, listener);

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                setDisplayName(build, listener);
                return true;
            }
        };
    }

    private void setDisplayName(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        try {
            template = build.getEnvironment(listener).expand(template);
            build.setDisplayName(TokenMacro.expand(build, listener, template));
        } catch (MacroEvaluationException e) {
            listener.getLogger().println(e.getMessage());
        }
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Set Build Name";
        }
    }
}
