package org.jenkinsci.plugins.buildnamesetter;

import java.io.File;
import java.io.IOException;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.EnvironmentVarSetter;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

/**
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class Executor {

    private final Run run;
    private final TaskListener listener;

    public Executor(Run run, TaskListener listener) {
        this.run = run;
        this.listener = listener;
    }

    public void setName(String nameTemplate) {
        try {
            String name = evaluateMacro(nameTemplate);
            listener.getLogger().println("New run name is '" + name + "'");
            run.setDisplayName(name);
            setVariable(nameTemplate);
        } catch (IOException e) {
            listener.error(e.getMessage());
        } catch (MacroEvaluationException e) {
            // should be marked as failure but then many configuration
            // that work with older version of the plugin will fail
            listener.getLogger().println("Failed to evaluate name macro:" + e.toString());
        }
    }

    public void setDescription(String descriptionTemplate) {
        // skip when the description is not provided (because plugin was updated but configuration not)
        if (StringUtils.isEmpty(descriptionTemplate)) {
            return;
        }

        try {
            String description = evaluateMacro(descriptionTemplate);
            listener.getLogger().println("New run description is '" + description + "'");
            run.setDescription(description);
        } catch (IOException e) {
            listener.error(e.getMessage());
        } catch (MacroEvaluationException e) {
            // should be marked as failure but then many configuration
            // that work with older version of the plugin will fail
            listener.getLogger().println("Failed to evaluate description macro:" + e.toString());
        }
    }

    public void setVariable(String nameTemplate) throws MacroEvaluationException {
        if (run instanceof AbstractBuild abstractBuild) {
            EnvironmentVarSetter.setVar(abstractBuild, EnvironmentVarSetter.buildDisplayNameVar,
                    evaluateMacro(nameTemplate), listener.getLogger());
        }
    }

    public String evaluateMacro(String template) throws MacroEvaluationException {
        try {
            File workspace = run.getRootDir();
            return TokenMacro.expandAll(run, new FilePath(workspace), listener, template);
        } catch (InterruptedException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
