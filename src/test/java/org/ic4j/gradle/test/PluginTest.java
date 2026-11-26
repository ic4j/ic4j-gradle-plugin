package org.ic4j.gradle.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class PluginTest {
	
	@Test
	public void test(){
	    Project project = ProjectBuilder.builder().build();
	    project.getPluginManager().apply("org.ic4j.ic4j-gradle-plugin");
	 
        assertTrue(project.getPluginManager().hasPlugin("org.ic4j.ic4j-gradle-plugin"));
        assertNotNull(project.getTasks().getByName("install"));
        assertNotNull(project.getTasks().getByName("uninstall"));
        assertNotNull(project.getTasks().getByName("call"));
    }
}
