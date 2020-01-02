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
package saker.jar.create.option;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.TreeMap;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class SimpleJarManifestTaskOption implements JarManifestTaskOption, Externalizable {
	private static final long serialVersionUID = 1L;

	private Map<String, String> mainAttributes;
	private Map<String, Map<String, String>> attributes;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleJarManifestTaskOption() {
	}

	public SimpleJarManifestTaskOption(JarManifestTaskOption copy) {
		this.mainAttributes = ImmutableUtils.makeImmutableNavigableMap(copy.getMainAttributes());
		this.attributes = ObjectUtils.cloneTreeMap(copy.getEntryAttributes(), Functionals.identityFunction(),
				ImmutableUtils::makeImmutableNavigableMap);
	}

	@Override
	public JarManifestTaskOption clone() {
		return this;
	}

	@Override
	public Map<String, String> getMainAttributes() {
		return mainAttributes;
	}

	public void setMainAttributes(Map<String, String> mainAttributes) {
		this.mainAttributes = mainAttributes;
	}

	@Override
	public Map<String, Map<String, String>> getEntryAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Map<String, String>> attributes) {
		this.attributes = attributes;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, mainAttributes);
		SerialUtils.writeExternalMap(out, attributes, SerialUtils::writeExternalObject, SerialUtils::writeExternalMap);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		mainAttributes = SerialUtils.readExternalImmutableNavigableMap(in);
		attributes = ImmutableUtils.unmodifiableNavigableMap(SerialUtils.readExternalMap(new TreeMap<>(), in,
				SerialUtils::readExternalObject, SerialUtils::readExternalImmutableNavigableMap));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((mainAttributes == null) ? 0 : mainAttributes.hashCode());
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
		SimpleJarManifestTaskOption other = (SimpleJarManifestTaskOption) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (mainAttributes == null) {
			if (other.mainAttributes != null)
				return false;
		} else if (!mainAttributes.equals(other.mainAttributes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[mainAttributes=" + mainAttributes + ", attributes=" + attributes + "]";
	}

}
