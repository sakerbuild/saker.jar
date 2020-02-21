package saker.jar.create.option;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.zip.main.create.option.ZipContentsTaskOption;

public class JarCreateDataContext implements JarContentsTaskOption {
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

	public JarCreateDataContext() {
	}

	public JarCreateDataContext(JarCreateDataContext copy) {
		this.multiReleaseContents = ObjectUtils.cloneTreeMap(copy.getMultiReleaseContents(),
				Functionals.identityFunction(), ZipContentsTaskOption::clone);
		this.manifestOption = ObjectUtils.clone(copy.getManifest(), JarManifestTaskOption::clone);
		this.servicesOption = ObjectUtils.cloneTreeMap(copy.getServices(), Functionals.identityFunction(),
				ImmutableUtils::makeImmutableList);
		this.moduleInfoMainClass = copy.getModuleInfoMainClass();
		this.moduleInfoVersion = copy.getModuleInfoVersion();
	}

	@Override
	public Map<Integer, ZipContentsTaskOption> getMultiReleaseContents() {
		return multiReleaseContents;
	}

	@Override
	public JarCreateDataContext clone() {
		return new JarCreateDataContext(this);
	}

	@Override
	public JarManifestTaskOption getManifest() {
		return manifestOption;
	}

	@Override
	public Map<String, Collection<String>> getServices() {
		return servicesOption;
	}

	@Override
	public String getModuleInfoMainClass() {
		return moduleInfoMainClass;
	}

	@Override
	public String getModuleInfoVersion() {
		return moduleInfoVersion;
	}

}
