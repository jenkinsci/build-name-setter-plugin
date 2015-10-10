package org.jenkinsci.plugins.buildnameupdater;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * This plugin replace the build name with the first line from a file on a slave.
 *
 * @author Lev Mishin
 */
public class BuildNameUpdater extends Builder {

    private final String buildName;
    private final String macroTemplate;
    private final boolean fromFile;
    private final boolean fromMacro;
    private final boolean macroFirst;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public BuildNameUpdater(BuildNameFromFile nameFromFile, BuildNameFromMacro nameFromMacro) {
        if (nameFromFile != null) {
            this.buildName = nameFromFile.buildName;
            this.fromFile = true;
        }
        else {
            this.buildName = "";
            this.fromFile = false;
        }

        if (nameFromMacro != null) {
            this.macroTemplate = nameFromMacro.macroTemplate;
            this.fromMacro = true;
            this.macroFirst = nameFromMacro.macroFirst;
        }
        else {
            this.macroTemplate = "";
            this.fromMacro = false;
            this.macroFirst = false;
        }
    }

    @SuppressWarnings("unused")
    public boolean getFromFile() {
        return fromFile;
    }

    @SuppressWarnings("unused")
    public boolean getMacroFirst() {
        return macroFirst;
    }

    @SuppressWarnings("unused")
    public boolean getFromMacro() {
        return fromMacro;
    }

    @SuppressWarnings("unused")
    public String getBuildName() {
        return buildName;
    }

    @SuppressWarnings("unused")
    public String getMacroTemplate() {
        return macroTemplate;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        String buildNameToSet = "";

        if (fromFile){
            buildNameToSet = readFromFile(build, listener, buildName);
        }

        if(fromMacro){
            String evaluatedMacro = getFromMacro(build, listener, macroTemplate);

            listener.getLogger().println("Evaluated macro: '" + evaluatedMacro + "'");

            buildNameToSet = macroFirst ? evaluatedMacro + buildNameToSet : buildNameToSet + evaluatedMacro;
        }

        if (buildNameToSet != null && !buildNameToSet.isEmpty()) {
            setDisplayName(build, listener, buildNameToSet);
        }

        return true;
    }

    private void setDisplayName(AbstractBuild build, BuildListener listener, String result) {
        listener.getLogger().println("Setting build name to '" + result + "'");
        if (result.isEmpty()) {
            listener.getLogger().println("Build name is empty, nothing to set.");
            return;
        }
        try {
            build.setDisplayName(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFromMacro(AbstractBuild build, BuildListener listener, String macro){
        String result = null;
        try {
            result = TokenMacro.expandAll(build, listener, macro);
        } catch (MacroEvaluationException e) {
            listener.getLogger().println("Failed to evaluate macro '" + macro + "'");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            listener.getLogger().println("Macro evaluating failed with:");
            e.printStackTrace();
        }
        return result;
    }

    private String readFromFile(AbstractBuild build, BuildListener listener, String filePath){
        String version = "";

        if (filePath == null || filePath.isEmpty()){
            listener.getLogger().println("File path is empty.");
            return "";
        }

        FilePath fp = new FilePath(build.getWorkspace(), filePath);

        listener.getLogger().println("Getting version from file: " + fp);

        try {
            version = fp.act(new MyFileCallable());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            listener.getLogger().println("Macro evaluating failed with:");
            e.printStackTrace();
        }

        listener.getLogger().println("Loaded version is " + version);
        return version == null ? "" : version;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public static class BuildNameFromFile{
        private String buildName;

        @DataBoundConstructor
        public BuildNameFromFile(String buildName){
            this.buildName = buildName;
        }

        @SuppressWarnings("unused")
        public String getBuildName() {
            return buildName;
        }
    }

    public static class BuildNameFromMacro{
        private String macroTemplate;
        private boolean macroFirst;

        @DataBoundConstructor
        public BuildNameFromMacro(String macroTemplate, boolean macroFirst){
            this.macroTemplate = macroTemplate;
            this.macroFirst = macroFirst;
        }

        @SuppressWarnings("unused")
        public String getMacroTemplate() {
            return macroTemplate;
        }

        @SuppressWarnings("unused")
        public boolean getMacroFirst() {
            return macroFirst;
        }
    }

    private static class MyFileCallable implements FilePath.FileCallable<String> {
        private static final long serialVersionUID = 1L;

        @Override
        public String invoke(File file, VirtualChannel channel) throws IOException, InterruptedException {
            if (file.getAbsoluteFile().exists()){
                BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
                return br.readLine();
            } else {
                return "";
            }
        }
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a file path");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
            return FreeStyleProject.class.isAssignableFrom(jobType);
        }

        public String getDisplayName() {
            return "Update build name";
        }
    }
}

