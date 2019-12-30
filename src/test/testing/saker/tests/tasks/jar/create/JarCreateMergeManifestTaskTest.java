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
public class JarCreateMergeManifestTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		Manifest diskmanifest = new Manifest();
		diskmanifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		diskmanifest.getMainAttributes().putValue("MyDiskAttr", "123");
		files.putFile(PATH_WORKING_DIRECTORY.resolve(JarFile.MANIFEST_NAME), JarCreatorUtils.manifestToString(diskmanifest));
		
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("MyAttr", "abc");
		manifest.getMainAttributes().putValue("MyDiskAttr", "123");

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
