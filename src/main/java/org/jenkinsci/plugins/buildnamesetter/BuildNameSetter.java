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
    private boolean matches;
    private int scenario = -1;
    private boolean started = false;
    
    @DataBoundConstructor
    public BuildNameSetter(String template, String matrixTemplate) {
        this.template = template;
        this.matrixTemplate = matrixTemplate;
        matches = template.equals(matrixTemplate);
        
        if(matches || matrixTemplate.length()>0 && template.length()==0){
        	scenario = 0;
        }else if (matrixTemplate.length()>0 && template.length()>0){
        	scenario = 1;
        }else if (matrixTemplate.length()==0 && template.length()>0){
        	scenario = 2;
        }
    }
    
    @Deprecated
    public BuildNameSetter(String template) {
        this.template = template;
        this.matrixTemplate = template;
    }
    
    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        try{
        	//not checking before setting name because this check is done via Jelly call to isMatrix()
        	setDisplayName(build, listener);
        }catch(Exception e){
        	e.printStackTrace();
        	return null;
        }
    	return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                try {
					setDisplayName(build, listener);
	                return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
                return false;
            }
    	};
    }

    private void setDisplayName(AbstractBuild build, BuildListener listener) throws Exception {
    	try{
    		switch(scenario){
    		case 0:
    	    		build.setDisplayName(TokenMacro.expandAll(build, listener, matrixTemplate));
    				return;
    		case 1:
	    			if(started){	
	    				build.setDisplayName(TokenMacro.expandAll(build, listener, matrixTemplate));
	    			}else{
	    				build.setDisplayName(TokenMacro.expandAll(build, listener, template));
	    			}
    				return;
    		case 2:
	    			build.setDisplayName(TokenMacro.expandAll(build, listener, template));
    				return;
    		case -1:
    				throw new Exception("SetUp not performed!");
    		}
    	} catch (MacroEvaluationException e) {
          listener.getLogger().println(e.getMessage());
    	}  
    }

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build,launcher,listener) {
            @Override
            public boolean startBuild() throws InterruptedException, IOException {
                try {
					setDisplayName(build,listener);
					started = true;
	                return super.startBuild();
				} catch (Exception e) {
					e.printStackTrace();
				}
                return false;
            }

            @Override
            public boolean endBuild() throws InterruptedException, IOException {
            	try{
            		setDisplayName(build,listener);
            		return super.endBuild();
            	}catch(Exception e){
            		e.printStackTrace();
            	}
            	return false;
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
        
        public boolean isMatrix(AbstractProject<?,?> proj){
        	return proj instanceof MatrixProject;
        }
    }
    
}
