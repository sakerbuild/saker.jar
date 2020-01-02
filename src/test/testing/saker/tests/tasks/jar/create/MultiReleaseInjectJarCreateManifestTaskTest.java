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
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class MultiReleaseInjectJarCreateManifestTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		testManifestFromBuildFile();
		testManifestFromFileSystem();
		testManifestGen();
	}

	private void testManifestGen() throws Throwable {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("Multi-Release", "true");

		Map<String, String> contents = new TreeMap<>();
		contents.put("a.txt", "alpha");
		contents.put("META-INF/versions/9/b.txt", "beta");
		contents.put(JarFile.MANIFEST_NAME, JarCreatorUtils.manifestToString(manifest));

		CombinedTargetTaskResult res;

		res = runScriptTask("genmanifest");
		assertSameContents(res, contents);

		res = runScriptTask("genmanifest");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertSameContents(res, contents);

	}

	private void testManifestFromFileSystem() throws Throwable {
		Manifest diskmanifest = new Manifest();
		diskmanifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		diskmanifest.getMainAttributes().putValue("MyDiskAttr", "123");
		files.putFile(PATH_WORKING_DIRECTORY.resolve(JarFile.MANIFEST_NAME), JarCreatorUtils.manifestToString(diskmanifest));
		
		Manifest expectedmanifest = new Manifest();
		expectedmanifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		expectedmanifest.getMainAttributes().putValue("MyDiskAttr", "123");
		expectedmanifest.getMainAttributes().putValue("Multi-Release", "true");

		Map<String, String> contents = new TreeMap<>();
		contents.put("a.txt", "alpha");
		contents.put("META-INF/versions/9/b.txt", "beta");
		contents.put(JarFile.MANIFEST_NAME, JarCreatorUtils.manifestToString(expectedmanifest));

		CombinedTargetTaskResult res;

		res = runScriptTask("resmanifest");
		assertSameContents(res, contents);

		res = runScriptTask("resmanifest");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertSameContents(res, contents);
		
		//update the manifest
		expectedmanifest.getMainAttributes().putValue("AddAttr", "add");
		diskmanifest.getMainAttributes().putValue("AddAttr", "add");
		files.putFile(PATH_WORKING_DIRECTORY.resolve(JarFile.MANIFEST_NAME), JarCreatorUtils.manifestToString(diskmanifest));
		contents.put(JarFile.MANIFEST_NAME, JarCreatorUtils.manifestToString(expectedmanifest));
		
		res = runScriptTask("resmanifest");
		assertSameContents(res, contents);
	}

	private void testManifestFromBuildFile() throws Throwable, IOException, FileNotFoundException, AssertionError {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("MyAttr", "abc");
		manifest.getMainAttributes().putValue("Multi-Release", "true");

		Map<String, String> contents = new TreeMap<>();
		contents.put("a.txt", "alpha");
		contents.put("META-INF/versions/9/b.txt", "beta");
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
