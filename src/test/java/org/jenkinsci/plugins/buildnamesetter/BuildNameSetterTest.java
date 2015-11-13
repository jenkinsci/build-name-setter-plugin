package org.jenkinsci.plugins.buildnamesetter;

import static org.junit.Assert.assertEquals;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
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
		fooProj.getBuildWrappersList().add(new BuildNameSetter("a_#${BUILD_NUMBER}", "")); 
		
		FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "a_#1");
	}

	@Test
	public void shouldExpand_JOB_NAME_full_env_macro() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
		barProj.getBuildWrappersList().add(new BuildNameSetter("b_${ENV,var=\"JOB_NAME\"}", "")); 
		
		FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "b_bar");
	}

	@Bug(13347)
	@Test
	public void shouldExpand_JOB_NAME_macro() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
		barProj.getBuildWrappersList().add(new BuildNameSetter("c_${JOB_NAME}", "")); 
		
		FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "c_bar");
	}

	@Bug(13347)
	@Test
	public void shouldExpand_JOB_NAME_macro_twice() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject barProj = jenkins.createFreeStyleProject("bar");
		barProj.getBuildWrappersList().add(new BuildNameSetter("c_${JOB_NAME}_d_${JOB_NAME}", "")); 
		
		FreeStyleBuild barBuild = barProj.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "c_bar_d_bar");
	}
	
	@Bug(13347)
	@Test
	public void shouldExpand_JOB_NAME_macro_and_JOB_NAME_full_env_macro() throws InterruptedException, ExecutionException, IOException {
		FreeStyleProject fooProj = jenkins.createFreeStyleProject("foo");
		fooProj.getBuildWrappersList().add(new BuildNameSetter("d_${NODE_NAME}_${ENV,var=\"JOB_NAME\"}", "")); 
		
		FreeStyleBuild fooBuild = fooProj.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "d_master_foo");
	}
	
	@Bug(26574)
	@Test
	public void shouldExpand_BUILD_NUMBER_macro_matrix() throws InterruptedException, ExecutionException, IOException {
		//JENKINS-26574 - only matrix set
		MatrixProject barMatrix = jenkins.createMatrixProject("bar");
		barMatrix.getBuildWrappersList().add(new BuildNameSetter("", "a_#${BUILD_NUMBER}")); 
		MatrixBuild barBuild = barMatrix.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "a_#1");
		//JENKINS-26574 - template and matrix set
		MatrixProject fooMatrix = jenkins.createMatrixProject("foo");
		fooMatrix.getBuildWrappersList().add(new BuildNameSetter("a_#${BUILD_NUMBER}", "foobar")); 
		MatrixBuild fooBuild = fooMatrix.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "foobar");
	}
	@Bug(26574)
	@Test
	public void shouldExpand_JOB_NAME_full_env_macro_matrix() throws InterruptedException, ExecutionException, IOException {
		//JENKINS-26574 - only matrix set
		MatrixProject fooMatrix = jenkins.createMatrixProject("foo");
		fooMatrix.getBuildWrappersList().add(new BuildNameSetter("", "b_${ENV,var=\"JOB_NAME\"}")); 
		MatrixBuild fooBuild = fooMatrix.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "b_foo");
		//JENKINS-26574 - template and matrix set
		MatrixProject barMatrix = jenkins.createMatrixProject("bar");
		barMatrix.getBuildWrappersList().add(new BuildNameSetter("b_${ENV,var=\"JOB_NAME\"}", "foobar")); 
		MatrixBuild barBuild = barMatrix.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "foobar");
	}

	@Bug(26574)
	@Test
	public void shouldExpand_JOB_NAME_macro_matrix() throws InterruptedException, ExecutionException, IOException {
		//JENKINS-26574 - only matrix set
		MatrixProject fooMatrix = jenkins.createMatrixProject("foo");
		fooMatrix.getBuildWrappersList().add(new BuildNameSetter("", "c_${JOB_NAME}")); 
		MatrixBuild fooBuild = fooMatrix.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "c_foo");
		//JENKINS-26574 - template and matrix set
		MatrixProject barMatrix = jenkins.createMatrixProject("bar");
		barMatrix.getBuildWrappersList().add(new BuildNameSetter("c_${JOB_NAME}", "foobar")); 
		MatrixBuild barBuild = barMatrix.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "foobar");

	}
	@Bug(26574)
	@Test
	public void shouldExpand_JOB_NAME_macro_twice_matrix() throws InterruptedException, ExecutionException, IOException {
		//JENKINS-26574 - only matrix set
		MatrixProject fooMatrix = jenkins.createMatrixProject("foo");
		fooMatrix.getBuildWrappersList().add(new BuildNameSetter("", "c_${JOB_NAME}_d_${JOB_NAME}")); 
		MatrixBuild fooBuild = fooMatrix.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "c_foo_d_foo");
		//JENKINS-26574 - template and matrix set
		MatrixProject barMatrix = jenkins.createMatrixProject("bar");
		barMatrix.getBuildWrappersList().add(new BuildNameSetter("c_${JOB_NAME}_d_${JOB_NAME}", "foobar")); 
		MatrixBuild barBuild = barMatrix.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "foobar");
		
	}
	
	@Bug(26574)
	@Test
	public void shouldExpand_JOB_NAME_macro_and_JOB_NAME_full_env_macro_matrix() throws InterruptedException, ExecutionException, IOException {
		//JENKINS-26574 - only matrix set
		MatrixProject barMatrix = jenkins.createMatrixProject("bar");
		barMatrix.getBuildWrappersList().add(new BuildNameSetter("", "d_${NODE_NAME}_${ENV,var=\"JOB_NAME\"}")); 
		MatrixBuild barBuild = barMatrix.scheduleBuild2(0).get();
		asssertDisplayName(barBuild, "d_master_bar");
		//JENKINS-26574 - template and matrix set
		MatrixProject fooMatrix = jenkins.createMatrixProject("foo");
		fooMatrix.getBuildWrappersList().add(new BuildNameSetter("d_${NODE_NAME}_${ENV,var=\"JOB_NAME\"}", "foobar")); 
		MatrixBuild fooBuild = fooMatrix.scheduleBuild2(0).get();
		asssertDisplayName(fooBuild, "foobar");
	}

	@Bug(26574)
	private void asssertDisplayName(MatrixBuild build, String expectedName) {
		assertEquals(Result.SUCCESS, build.getResult());
		assertEquals(expectedName, build.getDisplayName());
	}
	
	private void asssertDisplayName(FreeStyleBuild build, String expectedName) {
		assertEquals(Result.SUCCESS, build.getResult());
		assertEquals(expectedName, build.getDisplayName());
	}
	
}
