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
public class JarCreateManifestTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("MyAttr", "abc");

		Attributes subattrs = new Attributes();
		subattrs.putValue("SubAttr1", "123");
		subattrs.putValue("SubAttr2", "456");
		manifest.getEntries().put("MySubAttr", subattrs);

		Map<String, String> contents = new TreeMap<>();
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
