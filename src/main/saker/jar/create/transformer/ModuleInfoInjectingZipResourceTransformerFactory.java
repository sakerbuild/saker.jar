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
package saker.jar.create.transformer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.org.objectweb.asm.ClassReader;
import saker.build.thirdparty.org.objectweb.asm.ClassWriter;
import saker.jar.create.JarCreateTaskFactory;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.zip.api.create.ZipResourceTransformationContext;
import saker.zip.api.create.ZipResourceTransformer;
import saker.zip.api.create.ZipResourceTransformerFactory;

public class ModuleInfoInjectingZipResourceTransformerFactory implements ZipResourceTransformerFactory, Externalizable {
	private static final long serialVersionUID = 1L;

	private String moduleInfoMainClass;
	private String moduleInfoVersion;

	/**
	 * For {@link Externalizable}.
	 */
	public ModuleInfoInjectingZipResourceTransformerFactory() {
	}

	public ModuleInfoInjectingZipResourceTransformerFactory(String moduleInfoMainClass, String moduleInfoVersion) {
		this.moduleInfoMainClass = moduleInfoMainClass;
		this.moduleInfoVersion = moduleInfoVersion;
	}

	@Override
	public ZipResourceTransformer createTransformer() {
		return new ZipResourceTransformer() {
			private boolean transformed = false;

			@Override
			@SuppressWarnings("deprecation") // this process function was overridden, but we're fine with this implementation
			public boolean process(ZipResourceTransformationContext context, SakerPath resourcepath,
					InputStream resourceinput) throws IOException {
				if (transformed) {
					return false;
				}
				if (!JarCreateTaskFactory.PATH_MODULEINFO.equalsIgnoreCase(resourcepath)) {
					return false;
				}
				if (resourceinput == null) {
					return false;
				}
				transformed = true;
				ByteArrayRegion bufbytes;
				try (UnsyncByteArrayOutputStream buf = new UnsyncByteArrayOutputStream()) {
					buf.readFrom(resourceinput);
					bufbytes = buf.toByteArrayRegion();
				}
				ClassReader cr = new ClassReader(bufbytes.getArray(), bufbytes.getOffset(), bufbytes.getLength());
				ClassWriter cw = new ClassWriter(cr, 0);
				ModuleInfoInjectingClassVisitor injectingvisitor = new ModuleInfoInjectingClassVisitor(cw,
						moduleInfoMainClass, moduleInfoVersion);
				cr.accept(injectingvisitor, 0);
				try (OutputStream os = context.appendFile(JarCreateTaskFactory.PATH_MODULEINFO, null)) {
					os.write(cw.toByteArray());
				}
				return true;
			}
		};
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(moduleInfoMainClass);
		out.writeObject(moduleInfoVersion);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		moduleInfoMainClass = (String) in.readObject();
		moduleInfoVersion = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((moduleInfoMainClass == null) ? 0 : moduleInfoMainClass.hashCode());
		result = prime * result + ((moduleInfoVersion == null) ? 0 : moduleInfoVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuleInfoInjectingZipResourceTransformerFactory other = (ModuleInfoInjectingZipResourceTransformerFactory) obj;
		if (moduleInfoMainClass == null) {
			if (other.moduleInfoMainClass != null)
				return false;
		} else if (!moduleInfoMainClass.equals(other.moduleInfoMainClass))
			return false;
		if (moduleInfoVersion == null) {
			if (other.moduleInfoVersion != null)
				return false;
		} else if (!moduleInfoVersion.equals(other.moduleInfoVersion))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (moduleInfoMainClass != null ? "moduleInfoMainClass=" + moduleInfoMainClass + ", " : "")
				+ (moduleInfoVersion != null ? "moduleInfoVersion=" + moduleInfoVersion : "") + "]";
	}

}
