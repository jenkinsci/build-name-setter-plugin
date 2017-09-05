package org.jenkinsci.plugins;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Created by Leo on 4/20/2016.
 * Helper to work with environment variables.
 */
public class EnvironmentVarSetter implements EnvironmentContributingAction {
    private PrintStream log;
    private final Map<String, String> envVars = new ConcurrentHashMap<String, String>();

    public static final String buildDisplayNameVar = "BUILD_DISPLAY_NAME";

    public EnvironmentVarSetter(@CheckForNull String key, @CheckForNull String value, @CheckForNull PrintStream logger) {
        log = logger;
        envVars.put(key, value);
    }

    public static void setVar(AbstractBuild build, String key, String value, PrintStream logger) {
        EnvironmentVarSetter action = build.getAction(EnvironmentVarSetter.class);
        if (action == null) {
            action = new EnvironmentVarSetter(key, value, logger);
            build.addAction(action);
        }
        else {
            action.setVar(key, value);
        }
    }

    public void setVar(@CheckForNull String key, @CheckForNull String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key shouldn't be null or empty.");
        }
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("value shouldn't be null or empty.");
        }

        if (envVars.containsKey(key)) {
            if (!envVars.get(key).equals(value)) {
                log.println(
                        "Variable with name '" + key + "' already exists, current value: '" + envVars.get(key) +
                                "', new value: '" + value + "'");
            }
        }
        else {
            log.println("Create new variable " + key + "=" + value);
        }

        envVars.put(key, value);
    }

    public String getVar(String key) {
        if (envVars.containsKey(key)) {
            log.println("Get var: " + key + "=" + envVars.get(key));
            return envVars.get(key);
        }
        else {
            log.println("Var '" + key + "' doesn't exist");
            return "";
        }
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
        envVars.putAll(this.envVars);
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
