package org.jenkinsci.plugins.buildnamesetter;

import java.io.File;
import java.io.IOException;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
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
        } catch (IOException e) {
            listener.getLogger().println(e.getMessage());
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
            listener.getLogger().println(e.getMessage());
        }
    }

    private String evaluateMacro(String template) {
        try {
            File workspace = run.getRootDir();
            return TokenMacro.expandAll(run, new FilePath(workspace), listener, template);
        } catch (InterruptedException | IOException | MacroEvaluationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
