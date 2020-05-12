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
package testing.saker.tests.tasks.jar.multirelease.optimize;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;
import testing.saker.tests.tasks.jar.create.JarCreatorUtils;

@SakerTest
public class InvalidVersionDirMultiReleaseOptimizeTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		TreeMap<String, String> jarcontents = TestUtils.<String, String>treeMapBuilder().put("file.txt", "content")
				.put("META-INF/versions/8/file.txt", "content").put("META-INF/versions/+10/file.txt", "content")
				.put("META-INF/versions/-11/file.txt", "content").put("META-INF/versions/0/file.txt", "content")
				.put("META-INF/versions/abc/file.txt", "content").put("META-INF/versions/9abc/file.txt", "content")
				.put("META-INF/versions/09/file.txt", "content")
				//some very large int that doesn't fit in an int or long
				.put("META-INF/versions/123456789123456789123456789123456789123456789/file.txt", "content").build();
		files.putFile(PATH_WORKING_DIRECTORY.resolve("input.jar"), JarCreatorUtils.getJarBytes(null, jarcontents));

		CombinedTargetTaskResult res;
		res = runScriptTask("build");
		assertSameContents(res, jarcontents);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

	private void assertSameContents(CombinedTargetTaskResult res, Map<String, String> expectedcontents)
			throws IOException, FileNotFoundException {
		JarCreatorUtils.assertSameContents((SakerPath) res.getTargetTaskResult("jarpath"), files, expectedcontents);
	}
}
