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
import java.util.zip.ZipEntry;

import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

/**
 * Just a sanity check test for the compression functionality introduced in saker.zip-0.8.5.
 */
@SakerTest
public class CompressionSanityCheckTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("include.jar"), JarCreatorUtils.getJarBytes(null,
				TestUtils.<String, String>treeMapBuilder().put("inc1.txt", "incval").build()));

		Map<String, String> contents = new TreeMap<>();
		contents.put("a.txt", "alpha");
		contents.put("b.txt", "beta");
		contents.put("inc1.txt", "incval");
		contents.put("inctarget/inc1.txt", "incval");
		contents.put("mrdir/mr.dat", "multi-release-dat");
		contents.put("META-INF/versions/9/mrdir/mr.dat", "multi-release-dat");
		contents.put("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nMulti-Release: true\n");

		CombinedTargetTaskResult res;

		res = runScriptTask("build");
		assertSameContents(res, contents);

		res = runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), strTaskIdSetOf());
		assertSameContents(res, contents);

		SakerPath ctxtpath = PATH_WORKING_DIRECTORY.resolve("c.txt");
		files.putFile(ctxtpath, "gamma");
		contents.put("c.txt", "gamma");
		res = runScriptTask("build");
		assertSameContents(res, contents);

		files.putFile(ctxtpath, "gammamod");
		contents.put("c.txt", "gammamod");
		res = runScriptTask("build");
		assertSameContents(res, contents);

		files.delete(ctxtpath);
		contents.remove("c.txt");
		res = runScriptTask("build");
		assertSameContents(res, contents);

		files.putFile(PATH_WORKING_DIRECTORY.resolve("include.jar"), JarCreatorUtils.getJarBytes(null, TestUtils
				.<String, String>treeMapBuilder().put("inc1.txt", "incval").put("incadd.txt", "incadd").build()));
		contents.put("incadd.txt", "incadd");
		contents.put("inctarget/incadd.txt", "incadd");
		res = runScriptTask("build");
		assertSameContents(res, contents);
	}

	private void assertSameContents(CombinedTargetTaskResult res, Map<String, String> expectedcontents)
			throws IOException, FileNotFoundException {
		SakerPath jarpath = (SakerPath) res.getTargetTaskResult("jarpath");
		JarCreatorUtils.assertSameContents(jarpath, files, expectedcontents);
		
		JarCreatorUtils.assertCompression(files.getAllBytes(jarpath), ZipEntry.STORED, WildcardPath.valueOf("*.txt"));
		JarCreatorUtils.assertCompression(files.getAllBytes(jarpath), ZipEntry.STORED, WildcardPath.valueOf("**/mr.dat"));
	}
}
