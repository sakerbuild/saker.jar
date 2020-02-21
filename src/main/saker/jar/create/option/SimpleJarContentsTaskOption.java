package saker.jar.create.option;

import java.util.Collection;
import java.util.Map;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.zip.main.create.option.ZipContentsTaskOption;

public class SimpleJarContentsTaskOption implements JarContentsTaskOption {

	private Map<Integer, ZipContentsTaskOption> multiReleaseContents;
	private JarManifestTaskOption manifest;
	private Map<String, Collection<String>> services;
	private String moduleInfoMainClass;
	private String moduleInfoVersion;

	public SimpleJarContentsTaskOption(JarContentsTaskOption copy) {
		this.multiReleaseContents = ObjectUtils.cloneTreeMap(copy.getMultiReleaseContents(),
				Functionals.identityFunction(), ZipContentsTaskOption::clone);
		this.manifest = ObjectUtils.clone(copy.getManifest(), JarManifestTaskOption::clone);
		this.services = ObjectUtils.cloneTreeMap(copy.getServices(), Functionals.identityFunction(),
				ImmutableUtils::makeImmutableList);
		this.moduleInfoMainClass = copy.getModuleInfoMainClass();
		this.moduleInfoVersion = copy.getModuleInfoVersion();
	}

	@Override
	public JarContentsTaskOption clone() {
		return this;
	}

	@Override
	public Map<Integer, ZipContentsTaskOption> getMultiReleaseContents() {
		return multiReleaseContents;
	}

	@Override
	public JarManifestTaskOption getManifest() {
		return manifest;
	}

	@Override
	public Map<String, Collection<String>> getServices() {
		return services;
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
