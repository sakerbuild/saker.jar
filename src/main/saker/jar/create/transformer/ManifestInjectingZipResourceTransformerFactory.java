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
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.jar.create.option.JarManifestTaskOption;
import saker.zip.api.create.ZipResourceTransformer;
import saker.zip.api.create.ZipResourceTransformerFactory;

public final class ManifestInjectingZipResourceTransformerFactory
		implements ZipResourceTransformerFactory, Externalizable {
	private static final long serialVersionUID = 1L;

	private JarManifestTaskOption manifest;

	/**
	 * For {@link Externalizable}.
	 */
	public ManifestInjectingZipResourceTransformerFactory() {
	}

	public ManifestInjectingZipResourceTransformerFactory(JarManifestTaskOption manifest) {
		this.manifest = manifest;
	}

	@Override
	public ZipResourceTransformer createTransformer() {
		return new ManifestInjectingZipResourceTransformer(manifest);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(manifest);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		manifest = (JarManifestTaskOption) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((manifest == null) ? 0 : manifest.hashCode());
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
		ManifestInjectingZipResourceTransformerFactory other = (ManifestInjectingZipResourceTransformerFactory) obj;
		if (manifest == null) {
			if (other.manifest != null)
				return false;
		} else if (!manifest.equals(other.manifest))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ManifestInjectingZipResourceTransformerFactory[" + (manifest != null ? "manifest=" + manifest : "")
				+ "]";
	}

}