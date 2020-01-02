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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerFileProvider;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSource;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTestCase;

public class JarCreatorUtils {
	private JarCreatorUtils() {
		throw new UnsupportedOperationException();
	}

	public static ByteArrayRegion getJarBytes(Manifest manifest, Map<String, String> contents) throws IOException {
		ByteArrayRegion includezipbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream zipos = manifest == null ? new JarOutputStream(baos)
					: new JarOutputStream(baos, manifest)) {
				for (Entry<String, String> entry : contents.entrySet()) {
					zipos.putNextEntry(new ZipEntry(entry.getKey()));
					String entrycontents = entry.getValue();
					if (entrycontents != null) {
						zipos.write(entrycontents.getBytes());
					}
					zipos.closeEntry();
				}
			}
			includezipbytes = baos.toByteArrayRegion();
		}
		return includezipbytes;
	}

	public static void assertSameByteContents(SakerPath outpath, SakerFileProvider files,
			Map<String, byte[]> expectedcontents) throws IOException, FileNotFoundException {
		Map<String, byte[]> filesmap = new TreeMap<>(expectedcontents);

		try (ByteSource fileinput = files.openInput(outpath);
				ZipInputStream zis = new ZipInputStream(ByteSource.toInputStream(fileinput))) {
			for (ZipEntry e; (e = zis.getNextEntry()) != null;) {
				String name = e.getName();
				if (e.isDirectory()) {
					SakerTestCase.assertTrue(filesmap.containsKey(name), name);
					SakerTestCase.assertNull(filesmap.remove(name), name);
				} else {
					byte[] expectedcontent = filesmap.remove(name);
					SakerTestCase.assertNonNull(expectedcontent, name);
					if (JarFile.MANIFEST_NAME.equals(name)) {
						//check them as manifests, as the attributes are not ordered
						Manifest expectedmanifest = new Manifest(new UnsyncByteArrayInputStream(expectedcontent));
						Manifest actualmanifest = new Manifest(zis);
						if (!expectedmanifest.equals(actualmanifest)) {
							System.err.println("Expected manifest: ");
							expectedmanifest.write(System.err);
							System.err.println("Actual manifest: ");
							actualmanifest.write(System.err);
							throw SakerTestCase.fail("Different manifests.");
						}
						continue;
					}

					ByteArrayRegion entrybytes = StreamUtils.readStreamFully(zis);
					try {
						SakerTestCase.assertEquals(expectedcontent, entrybytes.copyOptionally(), name);
					} catch (AssertionError ex) {
						System.err.println(new String(expectedcontent, StandardCharsets.UTF_8));
						System.err.println(entrybytes);
						throw ex;
					}
				}
			}
		}
		SakerTestCase.assertEmpty(filesmap);
	}

	public static void assertSameContents(SakerPath outpath, SakerFileProvider files,
			Map<String, String> expectedcontents) throws IOException, FileNotFoundException {
		Map<String, byte[]> bytecontents = new TreeMap<>();
		for (Entry<String, String> entry : expectedcontents.entrySet()) {
			bytecontents.put(entry.getKey(), entry.getValue().getBytes(StandardCharsets.UTF_8));
		}
		assertSameByteContents(outpath, files, bytecontents);
	}

	public static String manifestToString(Manifest mf) {
		try {
			UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
			mf.write(baos);
			return baos.toString();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
}
