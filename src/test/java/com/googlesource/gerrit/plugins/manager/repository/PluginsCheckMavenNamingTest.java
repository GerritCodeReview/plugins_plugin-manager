package com.googlesource.gerrit.plugins.manager.repository;

import java.util.Arrays;
import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;
import static com.googlesource.gerrit.plugins.manager.repository.JenkinsCiPluginsRepository.fixPluginNameForMavenBuilds;
import static com.googlesource.gerrit.plugins.manager.repository.JenkinsCiPluginsRepository.isMavenBuild;

public class PluginsCheckMavenNamingTest {

	@Test
	public void checkIsMavenBuildDetectionTrue(){
		String [] pluginPathParts =
				Arrays.asList("target", "github-plugin.jar").toArray(new String[0]);
		boolean isMavenBuild = isMavenBuild(pluginPathParts);
		assertThat(isMavenBuild).isTrue();
	}
	@Test
	public void checkIsMavenBuildDetectionFalse(){
		String [] pluginPathParts =
				Arrays.asList("bazel-bin", "plugins", "git-refs-filter.jar").toArray(new String[0]);
		boolean isMavenBuild = isMavenBuild(pluginPathParts);
		assertThat(isMavenBuild).isFalse();
	}

	@Test
	public void checkMavenBuildNameCorrectionWorks_SimpleName(){
		String [] pluginPathParts =
				Arrays.asList("target", "github-plugin-1.0.0.jar").toArray(new String[0]);
		boolean isMavenBuild = isMavenBuild(pluginPathParts);
		assertThat(isMavenBuild).isTrue();

		assertThat(fixPluginNameForMavenBuilds(pluginPathParts,
				"https://gerrit-ci.gerritforge.com/job/plugin-github-mvn-stable-3.9/1")).isEqualTo("github");
	}

	@Test
	public void checkMavenBuildNameCorrectionWorks_NameWithHyphens(){
		// This test ensures that we dont get the incorrect name being returned as "ai".
		String [] pluginPathParts =
				Arrays.asList("target", "ai-code-review-1.0.0.jar").toArray(new String[0]);
		boolean isMavenBuild = isMavenBuild(pluginPathParts);
		assertThat(isMavenBuild).isTrue();

		assertThat(fixPluginNameForMavenBuilds(pluginPathParts,
				"https://gerrit-ci.gerritforge.com/job/plugin-ai-code-review-mvn-stable-3.9/1")).isEqualTo("ai-code-review");
	}

}
