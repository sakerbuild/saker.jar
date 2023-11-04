/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package testing.saker.tests.tasks.jar.create;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.classpath.JarFileClassPathLocation;
import saker.build.runtime.classpath.ServiceLoaderClassPathServiceEnumerator;
import saker.build.runtime.execution.ExecutionParametersImpl;
import saker.build.runtime.params.ExecutionRepositoryConfiguration;
import saker.build.runtime.repository.SakerRepositoryFactory;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import testing.saker.SakerTest;
import testing.saker.build.tests.VariablesMetricEnvironmentTestCase;

/**
 * Similar test to {@link MultiReleaseInjectJarCreateManifestTaskTest} but the saker.zip dependency is pinned to 0.8.4
 * which doesn't have some of the newly introduced and used classes.
 * <p>
 * Test that the <code>TargetDirectoryResolvingZipCreatingTaskBuilder</code> type successfully instantiates on older
 * saker.zip releases and doesn't cause {@link NoClassDefFoundError} if <code>ZipResourceEntry</code> is not available
 * on the classpath.
 */
@SakerTest
public class OlderZipBundleCompatibilityTaskTest extends VariablesMetricEnvironmentTestCase {
	protected Set<SakerPath> repositoryParameterBundlePaths = Collections.emptySet();

	@Override
	protected void setupParameters(ExecutionParametersImpl params) {
		super.setupParameters(params);

		Path repojarpath = Paths.get(testParameters.get("RepositoryJarPath")).toAbsolutePath();
		String parambundlepaths = testParameters.get("OlderZipBundles");

		String[] parambundles = parambundlepaths.split("[;]+");

		ExecutionRepositoryConfiguration repoconf = ExecutionRepositoryConfiguration.builder()
				.add(new JarFileClassPathLocation(LocalFileProvider.getInstance().getPathKey(repojarpath)),
						new ServiceLoaderClassPathServiceEnumerator<>(SakerRepositoryFactory.class), "nest")
				.build();
		params.setRepositoryConfiguration(repoconf);

		System.out.println("Param bundles:");
		Set<String> parambundlepathpaths = new LinkedHashSet<>();
		for (String pb : parambundles) {
			if (ObjectUtils.isNullOrEmpty(pb)) {
				continue;
			}
			parambundlepathpaths.add("//" + pb);
			System.out.println("    " + pb);
		}
		repositoryParameterBundlePaths = ImmutableUtils.makeImmutableLinkedHashSet(
				Stream.of(parambundles).filter(pb -> !pb.isEmpty()).map(SakerPath::valueOf).toArray(SakerPath[]::new));

		Map<String, String> nparams = new TreeMap<>(params.getUserParameters());
		nparams.put("nest.params.bundles", StringUtils.toStringJoin(";", parambundlepathpaths));
		nparams.put("nest.repository.storage.configuration", getRepositoryStorageConfiguration());
		params.setUserParameters(nparams);
	}

	protected String getRepositoryStorageConfiguration() {
		return "[:params]";
	}

	@Override
	protected void runTestImpl() throws Throwable {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("MyAttr", "abc");
		manifest.getMainAttributes().putValue("Multi-Release", "true");

		Map<String, String> contents = new TreeMap<>();
		contents.put("asub/a.txt", "alpha");
		contents.put("META-INF/versions/9/bsub/b.txt", "beta");
		contents.put(JarFile.MANIFEST_NAME, JarCreatorUtils.manifestToString(manifest));

		CombinedTargetTaskResult res;

		res = runScriptTask("build");
		assertSameContents(res, contents);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertSameContents(res, contents);
	}

	private void assertSameContents(CombinedTargetTaskResult res, Map<String, String> expectedcontents)
			throws IOException, FileNotFoundException {
		JarCreatorUtils.assertSameContents((SakerPath) res.getTargetTaskResult("jarpath"), files, expectedcontents);
	}
}
