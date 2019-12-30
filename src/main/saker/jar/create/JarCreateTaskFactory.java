package saker.jar.create;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.DataContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.jar.create.TaskDocs.ModuleInfoMainInjectOption;
import saker.jar.create.TaskDocs.ModuleInfoVersionInjectOption;
import saker.jar.create.TaskDocs.MultiReleaseVersionKey;
import saker.jar.create.TaskDocs.ServicesKey;
import saker.jar.create.TaskDocs.ServicesValue;
import saker.jar.create.option.JarManifestTaskOption;
import saker.jar.create.option.SimpleJarManifestTaskOption;
import saker.jar.create.transformer.ManifestInjectingZipResourceTransformerFactory;
import saker.jar.create.transformer.ModuleInfoInjectingZipResourceTransformerFactory;
import saker.jar.create.transformer.ServiceInjectingZipResourceTransformerFactory;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.zip.api.create.ZipCreationTaskBuilder;
import saker.zip.main.create.ZipCreateTaskFactory;
import saker.zip.main.create.option.ZipContentsTaskOption;
import saker.zip.main.create.option.ZipCreateDataContext;
import saker.zip.main.create.option.ZipIncludeTaskOption;
import saker.zip.main.create.option.ZipResourceTransformerTaskOption;
import saker.zip.main.create.option.ZipResourcesTaskOption;
import saker.zip.main.doc.TaskDocs;
import saker.zip.main.doc.TaskDocs.DocZipCreatorTaskOutput;

@NestTaskInformation(returnType = @NestTypeUsage(DocZipCreatorTaskOutput.class))
@NestInformation("Creates a JAR archive with the specified contents.\n"
		+ "The task is based on the ZIP archive creation " + ZipCreateTaskFactory.TASK_NAME
		+ "() task with additional features for Java Archive creation."
		+ "The archive will be written to the specified location in the " + JarCreateTaskFactory.TASK_NAME
		+ " subdirectory of the build directory.\n"
		+ "By default, the archive is constructed in a deterministic manner, meaning that it will not include timestamps of "
		+ "the current build time, and the entries are put in the archive in a deterministic order.")

@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Specifies the output path of the created archive.\n"
				+ "The path must be forward relative, and the task will write the archive under the "
				+ JarCreateTaskFactory.TASK_NAME + " subdirectory of the build directory.\n"
				+ "If not specified, the archive is created with the \"output.jar\" name."))

@NestParameterInformation(value = "Resources",
		type = @NestTypeUsage(value = Collection.class, elementTypes = ZipResourcesTaskOption.class),
		info = @NestInformation(TaskDocs.ZIP_CREATE_RESOURCES))
@NestParameterInformation(value = "Includes",
		aliases = { "Include" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = ZipIncludeTaskOption.class),
		info = @NestInformation(TaskDocs.ZIP_CREATE_INCLUDES))

@NestParameterInformation(value = "Transformers",
		aliases = { "Transformer" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = ZipResourceTransformerTaskOption.class),
		info = @NestInformation(TaskDocs.ZIP_CREATE_TRANSFORMERS))

@NestParameterInformation(value = "ModificationTime",
		type = @NestTypeUsage(value = Date.class, kind = TypeInformationKind.LITERAL),
		info = @NestInformation(TaskDocs.ZIP_CREATE_MODIFICATION_TIME))

@NestParameterInformation(value = "MultiReleaseContents",
		aliases = "MultiReleaseContents",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { MultiReleaseVersionKey.class, ZipContentsTaskOption.class }),
		info = @NestInformation("Specifies the Multi-Release contents of the created JAR file.\n"
				+ "Multi-Release JAR files can be used to include JRE version specific files in a JAR which are only used "
				+ "if the given runtime major version is greater or equals to the specified requirement.\n"
				+ "The option allows specifying contents for the different releases. Each entry in the configuration "
				+ "must have an integer key that specifies the target major version, and the value which contains the "
				+ "resource configurations to include.\n"
				+ "Each resource configuration will be placed under the META-INF/versions/<target-major> directory. "
				+ "Any further target directory configurations will be appended after the specified version root directory.\n"
				+ "Specifying this option will cause the Multi-Release: true attribute value to be injected into the "
				+ "created JAR Manifest file. (Only if not present yet.)\n"
				+ "For more information about Multi-Release JARs: https://openjdk.java.net/jeps/238"))

@NestParameterInformation(value = "Manifest",
		type = @NestTypeUsage(JarManifestTaskOption.class),
		info = @NestInformation("Specifies JAR Manifest attributes to be injected into the created archive.\n"
				+ "Any attributes specified in this option will result in them being injected into the META-INF/MANIFEST.MF "
				+ "Manifest entry in the archive.\n"
				+ "If a manifest entry was not specified using resources, a new one will be added to the archive.\n"
				+ "The Manifest-Version: 1.0 required attribute is automatically added if missing."))
@NestParameterInformation(value = "Services",
		type = @NestTypeUsage(value = Map.class, elementTypes = { ServicesKey.class, ServicesValue.class }),
		info = @NestInformation("Specifies Java services that should be declared by the created Java Archive.\n"
				+ "The service declarations are Java implementation class names in the META-INF/services/<service-class-name> entry.\n"
				+ "In this parameter services entries can be specified for which one or more implementations can be set. "
				+ "These implementation declarations will be injected in the resulting archive.\n"
				+ "See ServiceLoader class for more information: https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html"))

@NestParameterInformation(value = "ModuleInfoMainClass",
		type = @NestTypeUsage(ModuleInfoMainInjectOption.class),
		info = @NestInformation("Specifies a Java main class name that should be injected into the module-info.class entry.\n"
				+ "The specified class name will be recorded in the module-info of the created JAR. If there's no module-info.class "
				+ "entry in the output, the attribute is not injected.\n"
				+ "The format of the class name is not validated."))
@NestParameterInformation(value = "ModuleInfoVersion",
		type = @NestTypeUsage(ModuleInfoVersionInjectOption.class),
		info = @NestInformation("Specifies a version that should be injected into the module-info.class entry.\n"
				+ "The specified version will be recorded in the module-info of the created JAR. If there's no module-info.class "
				+ "entry in the output, the attribute is not injected.\n"
				+ "The format of the version is not validated."))
public class JarCreateTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.jar.create";

	public static final SakerPath PATH_MODULEINFO = SakerPath.valueOf("module-info.class");
	public static final SakerPath PATH_METAINF_MANIFESTMF = SakerPath.valueOf(JarFile.MANIFEST_NAME);
	public static final SakerPath PATH_METAINF_SERVICES = SakerPath.valueOf("META-INF/services");
	public static final SakerPath PATH_METAINF_VERSIONS = SakerPath.valueOf("META-INF/versions");

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new JarCreateTaskImpl();
	}

	protected static class JarCreateTaskImpl implements ParameterizableTask<Object> {
		private static final SakerPath DEFAULT_BUILD_SUBDIRECTORY_PATH = SakerPath.valueOf(TASK_NAME);
		private static final SakerPath DEFAULT_OUTPUT_PATH = SakerPath.valueOf("output.jar");

		@SakerInput({ "MultiReleaseContent", "MultiReleaseContents" })
		public Map<Integer, ZipContentsTaskOption> multiReleaseContents = Collections.emptyNavigableMap();

		@SakerInput("Manifest")
		public JarManifestTaskOption manifestOption;
		@SakerInput("Services")
		public Map<String, Collection<String>> servicesOption = Collections.emptyNavigableMap();

		@SakerInput("ModuleInfoMainClass")
		public String moduleInfoMainClass;
		@SakerInput("ModuleInfoVersion")
		public String moduleInfoVersion;

		@DataContext
		public ZipCreateDataContext zipDataContext;

		public JarCreateTaskImpl() {
		}

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			ZipCreateDataContext zipdata = this.zipDataContext.clone();
			Map<String, Collection<String>> services = ObjectUtils.cloneTreeMap(this.servicesOption,
					Functionals.identityFunction(), ImmutableUtils::makeImmutableLinkedHashSet);
			JarManifestTaskOption manifestopt = ObjectUtils.clone(this.manifestOption, JarManifestTaskOption::clone);
			Map<Integer, ZipContentsTaskOption> multireleasecontents = ObjectUtils.cloneTreeMap(
					this.multiReleaseContents, Functionals.identityFunction(), ZipContentsTaskOption::clone);

			SakerPath output = zipdata.outputOption;
			if (output == null || output.equals(SakerPath.EMPTY)) {
				output = DEFAULT_OUTPUT_PATH;
			}
			if (!output.isForwardRelative()) {
				taskcontext.abortExecution(
						new InvalidPathFormatException("Jar output path must be forward relative: " + output));
				return null;
			}

			SakerPath builddirpath = SakerPathFiles.requireBuildDirectoryPath(taskcontext)
					.resolve(DEFAULT_BUILD_SUBDIRECTORY_PATH);

			SakerPath absoluteoutpath = builddirpath.resolve(output);

			ZipCreationTaskBuilder taskbuilder = ZipCreationTaskBuilder.newBuilder();
			taskbuilder.setOutputPath(absoluteoutpath);
			zipdata.addTo(taskcontext, taskbuilder);

			if (!ObjectUtils.isNullOrEmpty(multireleasecontents)) {
				Integer below9 = null;
				for (Entry<Integer, ZipContentsTaskOption> entry : multireleasecontents.entrySet()) {
					ZipContentsTaskOption contents = entry.getValue();
					if (contents == null) {
						continue;
					}
					int version = entry.getKey();
					SakerPath targetdir;
					if (version <= 8) {
						if (below9 != null) {
							SakerLog.warning().taskScriptPosition(taskcontext)
									.println("Multiple multi-release versions specified below version 9. (" + below9
											+ " and " + version + ")");
						} else {
							below9 = entry.getKey();
						}
						targetdir = null;
					} else {
						targetdir = PATH_METAINF_VERSIONS.resolve(Integer.toString(version));
					}
					if (targetdir != null) {
						ZipCreateDataContext.addContentsTo(taskcontext,
								new TargetDirectoryResolvingZipCreatingTaskBuilder(taskbuilder, targetdir), contents);
					} else {
						ZipCreateDataContext.addContentsTo(taskcontext, taskbuilder, contents);
					}
				}
			}
			if (!ObjectUtils.isNullOrEmpty(services)) {
				for (Entry<String, Collection<String>> entry : services.entrySet()) {
					Collection<String> implnames = entry.getValue();
					if (ObjectUtils.isNullOrEmpty(implnames)) {
						continue;
					}
					Set<String> injectservices = new LinkedHashSet<>();
					String serviceclass = entry.getKey();
					for (String iname : implnames) {
						if (ObjectUtils.isNullOrEmpty(iname)) {
							continue;
						}
						injectservices.add(iname);
					}
					taskbuilder.addResourceTransformer(new ServiceInjectingZipResourceTransformerFactory(
							PATH_METAINF_SERVICES.resolve(serviceclass), injectservices));
				}
			}

			JarManifestTaskOption manifest = cloneInjectManifest(manifestopt, multireleasecontents);
			if (manifest != null) {
				taskbuilder.addResourceTransformer(new ManifestInjectingZipResourceTransformerFactory(manifest));
			}
			if (moduleInfoMainClass != null || moduleInfoVersion != null) {
				taskbuilder.addResourceTransformer(
						new ModuleInfoInjectingZipResourceTransformerFactory(moduleInfoMainClass, moduleInfoVersion));
			}

			TaskFactory<?> workerfactory = taskbuilder.buildTaskFactory();
			TaskIdentifier taskid = taskbuilder.buildTaskIdentifier();

			taskcontext.startTask(taskid, workerfactory, null);

			SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(taskid);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}

		private static JarManifestTaskOption cloneInjectManifest(JarManifestTaskOption manifest,
				Map<Integer, ZipContentsTaskOption> multireleasecontents) {
			if (ObjectUtils.isNullOrEmpty(multireleasecontents)) {
				return manifest;
			}
			//we need to inject the multi-release attribute
			if (manifest == null) {
				SimpleJarManifestTaskOption result = new SimpleJarManifestTaskOption();
				Map<String, String> mainattrs = new TreeMap<>();
				mainattrs.put("Multi-Release", "true");
				result.setMainAttributes(mainattrs);
				return result;
			}
			SimpleJarManifestTaskOption result = new SimpleJarManifestTaskOption(manifest);
			Map<String, String> mainattrs = ObjectUtils.newTreeMap(result.getMainAttributes());
			mainattrs.putIfAbsent("Multi-Release", "true");
			result.setMainAttributes(mainattrs);
			return result;
		}

	}

}
