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

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ChangeServicesOrderIncrementalTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		SakerPath buildfile = PATH_WORKING_DIRECTORY.resolve(DEFAULT_BUILD_FILE_NAME);

		Map<String, String> contents = new TreeMap<>();
		contents.put("META-INF/services/my.Service", "first.Service\nsecond.Service\n");

		CombinedTargetTaskResult res;

		res = runScriptTask("build");
		assertSameContents(res, contents);

		res = runScriptTask("build");
		assertSameContents(res, contents);
		assertEmpty(getMetric().getRunTaskIdResults());

		//the service order should be changed
		contents.put("META-INF/services/my.Service", "second.Service\nfirst.Service\n");
		files.putFile(buildfile, files.getAllBytes(buildfile).toString().replace("[first.Service,second.Service]",
				"[second.Service,first.Service]"));
		res = runScriptTask("build");
		assertSameContents(res, contents);
		assertNotEmpty(getMetric().getRunTaskIdResults());

		res = runScriptTask("build");
		assertSameContents(res, contents);
		assertEmpty(getMetric().getRunTaskIdResults());
	}

	private void assertSameContents(CombinedTargetTaskResult res, Map<String, String> expectedcontents)
			throws IOException, FileNotFoundException {
		JarCreatorUtils.assertSameContents((SakerPath) res.getTargetTaskResult("jarpath"), files, expectedcontents);
	}
}
