package org.jenkinsci.plugins;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import org.apache.commons.lang.StringUtils;

/**
 * Created by Leo on 4/20/2016.
 * Helper to work with environment variables.
 */
public class EnvironmentVarSetter implements EnvironmentContributingAction {

    @CheckForNull
    private transient PrintStream log;
    private final Map<String, String> envVars = new ConcurrentHashMap<String, String>();

    private static final Logger LOGGER = Logger.getLogger(EnvironmentVarSetter.class.getName());

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
        } else {
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
                log("Variable with name '%s' already exists, current value: '%s', new value: '%s'",
                        key, envVars.get(key), value);
            }
        } else {
            log("Create new variable %s=%s", key, value);
        }

        envVars.put(key, value);
    }

    public String getVar(String key) {
        if (envVars.containsKey(key)) {
            log("Get var: %s=%s", key, envVars.get(key));
            return envVars.get(key);
        } else {
            log("Var '%s' doesn't exist", key);
            return "";
        }
    }

    private void log(String format, Object... args) {
        if (log == null && !LOGGER.isLoggable(Level.FINE)) { // not loggable
            return;
        }

        String message = String.format(format, args);
        LOGGER.fine(message);
        if (log != null) {
            log.println(message);
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
