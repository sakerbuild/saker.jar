package testing.saker.tests.tasks.jar.create;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.org.objectweb.asm.ClassReader;
import saker.build.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.build.thirdparty.org.objectweb.asm.ClassWriter;
import saker.build.thirdparty.org.objectweb.asm.ModuleVisitor;
import saker.build.thirdparty.org.objectweb.asm.Opcodes;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSource;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import testing.saker.SakerTest;
import testing.saker.nest.util.RepositoryLoadingVariablesMetricEnvironmentTestCase;

@SakerTest
public class ModuleInfoInjectJarCreateTaskTest extends RepositoryLoadingVariablesMetricEnvironmentTestCase {
	private static final class AssertingClassVisitor extends ClassVisitor {
		private String mainClass;
		private String version;

		private AssertingClassVisitor(int api, String mainclass, String version) {
			super(api);
			this.mainClass = mainclass;
			this.version = version;
		}

		@Override
		public ModuleVisitor visitModule(String name, int access, String version) {
			assertEquals(this.version, version);
			this.version = null;
			return new ModuleVisitor(this.api) {
				@Override
				public void visitMainClass(String mainClass) {
					assertEquals(AssertingClassVisitor.this.mainClass, mainClass);
					AssertingClassVisitor.this.mainClass = null;
					super.visitMainClass(mainClass);
				}
			};
		}

		@Override
		public void visitEnd() {
			if (mainClass != null) {
				throw fail("Main class not found");
			}
			if (version != null) {
				throw fail("Version not found.");
			}
			super.visitEnd();
		}
	}

	private String mainClass;
	private String version;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.main.class", mainClass);
		result.put("test.version", version);
		return result;
	}

	@Override
	protected void runTestImpl() throws Throwable {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("module-info.class"), getStartingModuleInfo());

		CombinedTargetTaskResult res;

		this.mainClass = "my.entry.Point";
		this.version = "1.0";
		res = runScriptTask("build");
		assertModuleInfoData(res, this.mainClass, this.version);

		res = runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertModuleInfoData(res, this.mainClass, this.version);

		this.mainClass = "changed.entry.Point";
		res = runScriptTask("build");
		assertModuleInfoData(res, this.mainClass, this.version);

		this.version = "1.2";
		res = runScriptTask("build");
		assertModuleInfoData(res, this.mainClass, this.version);

		this.version = null;
		res = runScriptTask("build");
		assertModuleInfoData(res, this.mainClass, this.version);

		this.version = "1.3";
		this.mainClass = null;
		res = runScriptTask("build");
		assertModuleInfoData(res, this.mainClass, this.version);
	}

	private static byte[] getStartingModuleInfo() {
		ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
		ModuleVisitor mw = cw.visitModule("dummy.java.project", 0, null);
		mw.visitEnd();
		cw.visitEnd();
		byte[] expectedbytes = cw.toByteArray();
		return expectedbytes;
	}

	private void assertModuleInfoData(CombinedTargetTaskResult res, String mainclass, String version)
			throws NoSuchFileException, IOException {
		try (ByteSource fileinput = files.openInput((SakerPath) res.getTargetTaskResult("jarpath"));
				ZipInputStream zis = new ZipInputStream(ByteSource.toInputStream(fileinput))) {
			for (ZipEntry e; (e = zis.getNextEntry()) != null;) {
				String name = e.getName();
				if (!"module-info.class".equals(name)) {
					continue;
				}
				ByteArrayRegion minfobytes = StreamUtils.readStreamFully(zis);
				ClassReader cr = new ClassReader(minfobytes.copyOptionally());
				cr.accept(new AssertingClassVisitor(Opcodes.ASM7, mainclass, version), 0);
			}
		}
	}

}
