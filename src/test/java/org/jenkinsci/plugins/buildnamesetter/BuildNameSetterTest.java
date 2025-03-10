package org.jenkinsci.plugins.buildnamesetter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.EnvironmentVarSetter;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuildNameSetterTest {

    @Test
    void shouldExpand_BUILD_NUMBER_macro(JenkinsRule jenkins) throws Exception {
        FreeStyleProject fooProj = jenkins.createFreeStyleProject("foo");
        fooProj.getBuildWrappersList().add(getDefaultSetter("a_#${BUILD_NUMBER}"));

        FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
        assertDisplayName(fooBuild, "a_#1");
    }

    @Test
    void shouldExpand_JOB_NAME_full_env_macro(JenkinsRule jenkins) throws Exception {
        FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
        barProj.getBuildWrappersList().add(getDefaultSetter("b_${ENV,var=\"JOB_NAME\"}"));

        FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
        assertDisplayName(barBuild, "b_bar");
    }

    @Issue("13347")
    @Test
    void shouldExpand_JOB_NAME_macro(JenkinsRule jenkins) throws Exception {
        FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
        barProj.getBuildWrappersList().add(getDefaultSetter("c_${JOB_NAME}"));

        FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
        assertDisplayName(barBuild, "c_bar");
    }

    @Issue("13347")
    @Test
    void shouldExpand_JOB_NAME_macro_twice(JenkinsRule jenkins) throws Exception {
        FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
        barProj.getBuildWrappersList().add(getDefaultSetter("c_${JOB_NAME}_d_${JOB_NAME}"));

        FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
        assertDisplayName(barBuild, "c_bar_d_bar");
    }

    @Issue("13347")
    @Test
    void shouldExpand_NODE_NAME_macro_and_JOB_NAME_full_env_macro(JenkinsRule jenkins) throws Exception {
        FreeStyleProject fooProj = jenkins.createFreeStyleProject("foo");
        fooProj.getBuildWrappersList().add(getDefaultSetter("d_${NODE_NAME}_${ENV,var=\"JOB_NAME\"}"));

        FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
        assertDisplayName(fooBuild, "d_built-in_foo");
    }

    @Issue("34181")
    @Test
    void shouldUse_default_config_values_if_null(JenkinsRule jenkins) throws Exception {
        FreeStyleProject fooProj = jenkins.createFreeStyleProject("foo");
        fooProj.getBuildWrappersList().add(new BuildNameSetter("${ENV,var=\"JOB_NAME\"}", null, null));

        FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
        assertDisplayName(fooBuild, "foo");
    }

    private static void assertDisplayName(FreeStyleBuild build, String expectedName) {
        assertEquals(Result.SUCCESS, build.getResult());
        assertEquals(expectedName, build.getDisplayName());
        EnvironmentVarSetter action = build.getAction(EnvironmentVarSetter.class);
        assertEquals(expectedName, action.getVar(EnvironmentVarSetter.buildDisplayNameVar));
        EnvVars envVars = assertDoesNotThrow(
                () -> build.getEnvironment(TaskListener.NULL),
            "Exception was thrown during getting build environment");
        assertEquals(expectedName, envVars.get(EnvironmentVarSetter.buildDisplayNameVar));
    }

    private static BuildNameSetter getDefaultSetter(String template) {
        return new BuildNameSetter(template, true, true);
    }
}
