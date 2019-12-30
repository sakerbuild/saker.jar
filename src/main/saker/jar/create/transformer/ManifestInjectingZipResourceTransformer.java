package saker.jar.create.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import saker.build.file.path.SakerPath;
import saker.jar.create.JarCreateTaskFactory;
import saker.jar.create.option.JarManifestTaskOption;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.zip.api.create.ZipResourceTransformationContext;
import saker.zip.api.create.ZipResourceTransformer;

public final class ManifestInjectingZipResourceTransformer implements ZipResourceTransformer {
	private final JarManifestTaskOption manifest;
	private boolean hadManifest = false;

	public ManifestInjectingZipResourceTransformer(JarManifestTaskOption manifest) {
		this.manifest = manifest;
	}

	@Override
	public boolean process(ZipResourceTransformationContext context, SakerPath resourcepath, InputStream resourceinput)
			throws IOException {
		if (hadManifest) {
			//do not process twice if already encountered manifest
			return false;
		}
		if (!JarCreateTaskFactory.PATH_METAINF_MANIFESTMF.equalsIgnoreCase(resourcepath)) {
			return false;
		}

		hadManifest = true;

		Manifest m;
		if (resourceinput == null) {
			m = new Manifest();
			m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		} else {
			m = new Manifest(resourceinput);
		}
		putAttributes(m.getMainAttributes(), manifest.getMainAttributes());
		Map<String, Map<String, String>> injectattributes = manifest.getEntryAttributes();
		if (!ObjectUtils.isNullOrEmpty(injectattributes)) {
			Map<String, Attributes> manifestentries = m.getEntries();
			for (Entry<String, Map<String, String>> entry : injectattributes.entrySet()) {
				Map<String, String> attrvals = entry.getValue();
				if (ObjectUtils.isNullOrEmpty(attrvals)) {
					continue;
				}

				Attributes subattrs = new Attributes(attrvals.size());
				putAttributes(subattrs, attrvals);
				manifestentries.put(entry.getKey(), subattrs);
			}
		}
		try (OutputStream os = context.appendFile(JarCreateTaskFactory.PATH_METAINF_MANIFESTMF, null)) {
			m.write(os);
		}

		return true;
	}

	@Override
	public void flush(ZipResourceTransformationContext context) throws IOException {
		if (hadManifest) {
			return;
		}
		hadManifest = true;
		try (OutputStream os = context.appendFile(JarCreateTaskFactory.PATH_METAINF_MANIFESTMF, null)) {
			createManifest(manifest).write(os);
		}
	}

	private static Manifest createManifest(JarManifestTaskOption manifest) {
		if (manifest == null) {
			return null;
		}
		Manifest result = new Manifest();
		Attributes resultmainattributes = result.getMainAttributes();

		resultmainattributes.putIfAbsent(Attributes.Name.MANIFEST_VERSION, "1.0");

		putAttributes(resultmainattributes, manifest.getMainAttributes());

		Map<String, Map<String, String>> attrs = manifest.getEntryAttributes();
		if (!ObjectUtils.isNullOrEmpty(attrs)) {
			Map<String, Attributes> manifestentries = result.getEntries();

			for (Entry<String, Map<String, String>> entry : attrs.entrySet()) {
				Map<String, String> attrvals = entry.getValue();
				if (attrvals != null) {
					Attributes subattrs = new Attributes(attrvals.size());
					putAttributes(subattrs, attrvals);
					manifestentries.put(entry.getKey(), subattrs);
				}
			}
		}
		return result;
	}

	private static void putAttributes(Attributes attrs, Map<String, String> attrmap) {
		if (ObjectUtils.isNullOrEmpty(attrmap)) {
			return;
		}
		for (Entry<String, String> entry : attrmap.entrySet()) {
			attrs.putValue(entry.getKey(), entry.getValue());
		}
	}
}