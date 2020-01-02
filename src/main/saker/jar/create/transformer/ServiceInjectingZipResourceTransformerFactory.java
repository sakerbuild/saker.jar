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

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.zip.api.create.ZipResourceTransformationContext;
import saker.zip.api.create.ZipResourceTransformer;
import saker.zip.api.create.ZipResourceTransformerFactory;

public class ServiceInjectingZipResourceTransformerFactory implements ZipResourceTransformerFactory, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath resourcePath;
	private Set<String> services;

	/**
	 * For {@link Externalizable}.
	 */
	public ServiceInjectingZipResourceTransformerFactory() {
	}

	public ServiceInjectingZipResourceTransformerFactory(SakerPath resourcePath, Set<String> services) {
		this.resourcePath = resourcePath;
		this.services = services;
	}

	@Override
	public ZipResourceTransformer createTransformer() {
		return new ZipResourceTransformer() {
			private boolean transformed = false;

			@Override
			public boolean process(ZipResourceTransformationContext context, SakerPath resourcepath,
					InputStream resourceinput) throws IOException {
				if (transformed) {
					return false;
				}
				if (!ServiceInjectingZipResourceTransformerFactory.this.resourcePath.equalsIgnoreCase(resourcepath)) {
					return false;
				}
				transformed = true;

				//maps service class names to lines
				Set<String> presentservices;
				Collection<String> servicelines;
				if (resourceinput == null) {
					servicelines = services;
				} else {
					presentservices = new TreeSet<>();
					servicelines = new ArrayList<>();
					try (BufferedReader reader = new BufferedReader(
							new InputStreamReader(resourceinput, StandardCharsets.UTF_8))) {
						String line;
						while ((line = reader.readLine()) != null) {
							String trimmed = line.trim();
							if (trimmed.isEmpty()) {
								continue;
							}
							int idx = trimmed.indexOf('#');
							if (idx < 0) {
								if (!presentservices.add(trimmed)) {
									continue;
								}
								servicelines.add(line);
								continue;
							}
							//there's a comment
							String service = line.substring(0, idx).trim();
							if (service.isEmpty()) {
								//no service, only a comment
								//add the comment to the result
								servicelines.add(line);
								continue;
							}
							if (!presentservices.add(service)) {
								//the service is already present. add the comment to the output nonetheless
								servicelines.add(line.substring(idx ));
								continue;
							}
							servicelines.add(line);
						}
					}
					for (String s : services) {
						if (!presentservices.add(s)) {
							continue;
						}
						servicelines.add(s);
					}
				}
				appendServicesEntry(context, servicelines);
				return true;
			}

			@Override
			public void flush(ZipResourceTransformationContext context) throws IOException {
				if (transformed) {
					return;
				}
				appendServicesEntry(context, services);
				transformed = true;
			}

			private void appendServicesEntry(ZipResourceTransformationContext context, Collection<String> servicelines)
					throws IOException {
				byte[] bytes = StringUtils.toStringJoin(null, "\n", servicelines, "\n")
						.getBytes(StandardCharsets.UTF_8);
				try (OutputStream os = context
						.appendFile(ServiceInjectingZipResourceTransformerFactory.this.resourcePath, null)) {
					os.write(bytes);
				}
			}
		};
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(resourcePath);
		SerialUtils.writeExternalCollection(out, services);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		resourcePath = (SakerPath) in.readObject();
		services = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourcePath == null) ? 0 : resourcePath.hashCode());
		result = prime * result + ((services == null) ? 0 : services.hashCode());
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
		ServiceInjectingZipResourceTransformerFactory other = (ServiceInjectingZipResourceTransformerFactory) obj;
		if (resourcePath == null) {
			if (other.resourcePath != null)
				return false;
		} else if (!resourcePath.equals(other.resourcePath))
			return false;
		if (services == null) {
			if (other.services != null)
				return false;
		} else if (!services.equals(other.services))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (resourcePath != null ? "resourcePath=" + resourcePath + ", " : "")
				+ (services != null ? "services=" + services : "") + "]";
	}
}
