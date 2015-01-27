package org.jenkinsci.plugins.buildnamesetter;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
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
public class BuildNameSetter extends BuildWrapper implements MatrixAggregatable {

    public final String template;
    public final String matrixTemplate;
    public boolean isMatrix;

    @DataBoundConstructor
    public BuildNameSetter(String template, String matrixTemplate) {
        this.template = template;
        this.matrixTemplate = matrixTemplate;
    }
    
    @Deprecated
    public BuildNameSetter(String template) {
        this.template = template;
        this.matrixTemplate = null;
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
 		   if(isMatrix(build)){
	    		build.setDisplayName(TokenMacro.expandAll(build, listener, matrixTemplate));
	       }else{
	    	   build.setDisplayName(TokenMacro.expandAll(build, listener, template));
	       }
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
    public boolean isMatrix(AbstractBuild build){
    	if(build instanceof MatrixBuild){
    		isMatrix = true;
    	} else {
    		isMatrix = false;
    	}
    	return isMatrix;
    }
    
    public boolean isMatrix(AbstractProject proj){
    	if(proj instanceof MatrixProject){
    		isMatrix = true;
    	} else {
    		isMatrix = false;
    	}
    	return isMatrix;
    }
}
