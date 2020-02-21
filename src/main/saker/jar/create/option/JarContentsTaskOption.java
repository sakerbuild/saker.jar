package saker.jar.create.option;

import java.util.Collection;
import java.util.Map;

import saker.zip.main.create.option.ZipContentsTaskOption;

public interface JarContentsTaskOption {
	public default JarContentsTaskOption clone() {
		return new SimpleJarContentsTaskOption(this);
	}

	public Map<Integer, ZipContentsTaskOption> getMultiReleaseContents();

	public JarManifestTaskOption getManifest();

	public Map<String, Collection<String>> getServices();

	public String getModuleInfoMainClass();

	public String getModuleInfoVersion();
}
