package org.ic4j.gradle.test;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class PluginTest {
	
	@Test
	public void test(){
	    Project project = ProjectBuilder.builder().build();
	    project.getPluginManager().apply("org.ic4j.ic4j-gradle-plugin");
	 
	    assert(project.getPluginManager()
	      .hasPlugin("org.ic4j.ic4j-gradle-plugin"));
	 
	    assert(project.getTasks().getByName("install") != null);
	    assert(project.getTasks().getByName("uninstall") != null);
	}	

}
