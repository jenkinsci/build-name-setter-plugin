package org.jenkinsci.plugins.buildnamesetter;

import static org.apache.commons.lang.BooleanUtils.toBooleanDefaultIfNull;

import java.io.IOException;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.EnvironmentVarSetter;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Sets the build name at two configurable points during the build.
 * <p>
 * Once early on in the build, and another time later on.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildNameSetter extends BuildWrapper implements MatrixAggregatable {

    private String template;
    private String descriptionTemplate;
    private Boolean runAtStart = true;
    private Boolean runAtEnd = true;

    @DataBoundConstructor
    public BuildNameSetter(String template, Boolean runAtStart, Boolean runAtEnd) {
        // attribute is named differently than parameter that must be backwards compatible
        this.template = template;
        this.runAtStart = toBooleanDefaultIfNull(runAtStart, true);
        this.runAtEnd = toBooleanDefaultIfNull(runAtEnd, true);
    }

    @DataBoundSetter
    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }

    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    @DataBoundSetter
    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    public Boolean getRunAtStart() {
        return runAtStart;
    }

    public Boolean getRunAtEnd() {
        return runAtEnd;
    }

    protected Object readResolve() {
        if (runAtStart == null) {
            runAtStart = true;
        }
        if (runAtEnd == null) {
            runAtEnd = true;
        }
        return this;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        if (runAtStart) {
            setName(build, listener);
            setDescription(build, listener);
        }

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                if (runAtEnd) {
                    setName(build, listener);
                    setDescription(build, listener);
                }
                return true;
            }
        };
    }

    private void setName(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        try {
            final String name = TokenMacro.expandAll(build, listener, template);
            listener.getLogger().println("New build name is '" + name + "'");
            build.setDisplayName(name);
            EnvironmentVarSetter.setVar(build, EnvironmentVarSetter.buildDisplayNameVar, name, listener.getLogger());
        } catch (MacroEvaluationException e) {
            listener.getLogger().println(e.getMessage());
        }
    }

    private void setDescription(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        // when the description is not provided (because plugin was updated but configuration not)
        if (StringUtils.isEmpty(descriptionTemplate)) {
            return;
        }

        try {
            final String description = TokenMacro.expandAll(build, listener, descriptionTemplate);
            listener.getLogger().println("New build description is '" + description + "'");
                build.setDescription(description);
        } catch (MacroEvaluationException e) {
            listener.getLogger().println(e.getMessage());
        }
    }

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build, launcher, listener) {
            @Override
            public boolean startBuild() throws InterruptedException, IOException {
                setName(build, listener);
                setDescription(build, listener);
                return super.startBuild();
            }

            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                setName(build, listener);
                setDescription(build, listener);
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
