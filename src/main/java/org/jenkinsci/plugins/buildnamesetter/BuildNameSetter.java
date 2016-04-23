package org.jenkinsci.plugins.buildnamesetter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.EnvironmentContributingAction;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.jenkinsci.plugins.EnvironmentVarSetter;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

import static org.apache.commons.lang.BooleanUtils.toBooleanDefaultIfNull;

/**
 * Sets the build name at two configurable points during the build.
 *
 * Once early on in the build, and another time later on.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildNameSetter extends BuildWrapper implements MatrixAggregatable {
    public final String template;
    public boolean runAtStart = true;
    public boolean runAtEnd = true;

    @DataBoundConstructor
    public BuildNameSetter(String template, Boolean runAtStart, Boolean runAtEnd) {
        this.template = template;
        this.runAtStart = toBooleanDefaultIfNull(runAtStart, true);
        this.runAtEnd = toBooleanDefaultIfNull(runAtEnd, true);
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        if (runAtStart)
        {
            setDisplayName(build, listener);
        }

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                if (runAtEnd)
                {
                    setDisplayName(build, listener);
                }
                return true;
            }
        };
    }

    private void setDisplayName(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        listener.getLogger().println("Set build name.");
        try {
            final String name = TokenMacro.expandAll(build, listener, template);
            listener.getLogger().println("New build name is '" + name + "'");
            build.setDisplayName(name);
            EnvironmentVarSetter.setVar(build, EnvironmentVarSetter.buildDisplayNameVar, name, listener.getLogger());
        } catch (MacroEvaluationException e) {
            listener.getLogger().println(e.getMessage());
        }
    }

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build,launcher,listener) {
            @Override
            public boolean startBuild() throws InterruptedException, IOException {
                setDisplayName(build,listener);
                return super.startBuild();
            }

            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                setDisplayName(build,listener);
                return super.endBuild();
            }
        };
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
