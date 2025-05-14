package com.googlesource.gerrit.plugins.manager.repository;

import java.util.Arrays;
import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;
import static com.googlesource.gerrit.plugins.manager.repository.JenkinsCiPluginsRepository.fixPluginNameForMavenBuilds;
import static com.googlesource.gerrit.plugins.manager.repository.JenkinsCiPluginsRepository.isMavenBuild;


// Class, to assist is checking the maven plugin naming functions.
//
// - fixPluginNameForMavenBuilds
// - isMavenBuild.
//
public class PluginsCheckMavenNamingTest {

	@Test
	public void checkIsMavenBuildDetectionTrue(){
		String [] pluginPathParts = {"target", "ai-code-review-3.1.0.jar"};
		assertThat(isMavenBuild(pluginPathParts)).isTrue();
	}
	@Test
	public void checkIsMavenBuildDetectionFalse(){
		String [] pluginPathParts = {"bazel-bin", "plugins", "git-refs-filter.jar"};
		assertThat(isMavenBuild(pluginPathParts)).isFalse();
	}

	@Test
	public void checkMavenBuildNameCorrectionWorks_SimpleName(){
		String [] pluginPathParts = {"target", "maintainer-3.10.0.jar"};
		assertThat(isMavenBuild(pluginPathParts)).isTrue();
		assertThat(fixPluginNameForMavenBuilds(pluginPathParts)).isEqualTo("maintainer");
	}

	@Test
	public void checkMavenBuildNameCorrectionWorks_NameWithHyphens(){
		// This test ensures that we dont get the incorrect name being returned as "ai".
		String [] pluginPathParts = {"target", "ai-code-review-1.0.0.jar"};
		assertThat(isMavenBuild(pluginPathParts)).isTrue();
		assertThat(fixPluginNameForMavenBuilds(pluginPathParts)).isEqualTo("ai-code-review");
	}

}
