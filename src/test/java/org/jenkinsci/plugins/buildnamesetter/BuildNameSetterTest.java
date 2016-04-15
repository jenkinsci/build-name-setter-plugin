package org.jenkinsci.plugins.buildnamesetter;

import static org.junit.Assert.assertEquals;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;

public class BuildNameSetterTest {
	@Rule
	public JenkinsRule jenkins = new JenkinsRule();
	
	@Test
	public void shouldExpand_BUILD_NUMBER_macro() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject fooProj = jenkins.createFreeStyleProject("foo");
		fooProj.getBuildWrappersList().add(getDefaultSetter("a_#${BUILD_NUMBER}")); 
		
		FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "a_#1");
	}

	@Test
	public void shouldExpand_JOB_NAME_full_env_macro() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
		barProj.getBuildWrappersList().add(getDefaultSetter("b_${ENV,var=\"JOB_NAME\"}")); 
		
		FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "b_bar");
	}

	@Bug(13347)
	@Test
	public void shouldExpand_JOB_NAME_macro() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
		barProj.getBuildWrappersList().add(getDefaultSetter("c_${JOB_NAME}")); 
		
		FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "c_bar");
	}

	@Bug(13347)
	@Test
	public void shouldExpand_JOB_NAME_macro_twice() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
		barProj.getBuildWrappersList().add(getDefaultSetter("c_${JOB_NAME}_d_${JOB_NAME}")); 
		
		FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "c_bar_d_bar");
	}
	
	@Bug(13347)
	@Test
	public void shouldExpand_JOB_NAME_macro_and_JOB_NAME_full_env_macro() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject fooProj = jenkins.createFreeStyleProject("foo");
		fooProj.getBuildWrappersList().add(getDefaultSetter("d_${NODE_NAME}_${ENV,var=\"JOB_NAME\"}")); 
		
		FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "d_master_foo");
	}

	@Bug(34181)
	@Test
	public void shouldUse_default_config_values_if_null() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject fooProj = jenkins.createFreeStyleProject("foo");
		fooProj.getBuildWrappersList().add(new BuildNameSetter("${ENV,var=\"JOB_NAME\"}", null, null));

		FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "foo");
	}

	private void asssertDisplayName(FreeStyleBuild build, String expectedName) {
		assertEquals(Result.SUCCESS, build.getResult());
		assertEquals(expectedName, build.getDisplayName());
	}

	private BuildNameSetter getDefaultSetter(String template) {
		return new BuildNameSetter(template, true, true);
	}
}
