package testing.saker.tests.tasks.jar.create;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class IncludeRemoveManifestAttributeTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		Manifest m = new Manifest();
		m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		m.getMainAttributes().putValue("First-Attr", "123");
		m.getMainAttributes().putValue("Second-Attr", "456");
		files.putFile(PATH_WORKING_DIRECTORY.resolve("include.jar"),
				JarCreatorUtils.getJarBytes(m, Collections.emptyMap()));

		m.getMainAttributes().remove(new Attributes.Name("Second-Attr"));
		Map<String, String> contents = new TreeMap<>();
		contents.put("META-INF/MANIFEST.MF", JarCreatorUtils.manifestToString(m));

		CombinedTargetTaskResult res;

		res = runScriptTask("build");
		assertSameContents(res, contents);
	}

	private void assertSameContents(CombinedTargetTaskResult res, Map<String, String> expectedcontents)
			throws IOException, FileNotFoundException {
		JarCreatorUtils.assertSameContents((SakerPath) res.getTargetTaskResult("jarpath"), files, expectedcontents);
	}
}
