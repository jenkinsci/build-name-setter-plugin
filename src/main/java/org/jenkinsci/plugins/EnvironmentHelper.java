package org.jenkinsci.plugins;

import hudson.model.Hudson;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Leo on 4/20/2016.
 * Helper to work with environment variables.
 */
public class EnvironmentHelper {
    private static final Logger LOGGER = Logger.getLogger(EnvironmentHelper.class.getName());

    public static void SetEnvironmentVariable(@CheckForNull String key, @CheckForNull String value, PrintStream logger) {
        if (StringUtils.isBlank(key)) {
            logger.println("Unable to set variable with empty key.");
            return;
        }

        logger.println("Set var " + key + "=" + value);
        try {
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> properties = Hudson.getInstance().getGlobalNodeProperties();
            List<EnvironmentVariablesNodeProperty> envNodes = new ArrayList<EnvironmentVariablesNodeProperty>();
            for (NodeProperty node : properties) {
                if (node instanceof EnvironmentVariablesNodeProperty) {
                    EnvironmentVariablesNodeProperty envNode = (EnvironmentVariablesNodeProperty) node;
                    if (envNode.getEnvVars().containsKey(key)) {
                        logger.println("Variable already exist, update value");
                        envNode.getEnvVars().put(key, value);
                        return;
                    }
                    else {
                        envNodes.add(envNode);
                    }
                }
            }
            logger.println("Variable doesn't exists, create a new one");
            if (envNodes.size() > 0) {
                envNodes.get(0).getEnvVars().put(key, value);
            }
            else {
                Hudson.getInstance().getGlobalNodeProperties().add(
                        new EnvironmentVariablesNodeProperty(new EnvironmentVariablesNodeProperty.Entry(key, value)));
            }
        } catch (IOException e) {
            logger.println("Failed to set variable because of exception");
            logger.println(e.getMessage());
        }
    }

    public static String GetEnvironmentVariable(@CheckForNull String key) {
        if (StringUtils.isBlank(key)) {
            LOGGER.warning("Unable to set variable with empty key.");
            return null;
        }

        LOGGER.info("Get var " + key);
        try {
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodes =
                    Hudson.getInstance().getGlobalNodeProperties();
            for (NodeProperty node: nodes) {
                if (node instanceof EnvironmentVariablesNodeProperty) {
                    EnvironmentVariablesNodeProperty vars = (EnvironmentVariablesNodeProperty) node;
                    if (vars.getEnvVars().containsKey(key)) {
                        return vars.getEnvVars().get(key);
                    }
                }
            }
            return "";

        } catch (Exception e) {
            LOGGER.warning("Failed to set variable because of exception");
            LOGGER.warning(e.getMessage());
            return null;
        }
    }
}
