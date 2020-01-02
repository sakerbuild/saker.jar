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

import java.util.Map;

import saker.jar.create.TaskDocs.ManifestEntryAttributes;
import saker.jar.create.TaskDocs.ManifestEntryName;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Represents a configuration of Java Archive Manifest entry attributes.")
@NestFieldInformation(value = "MainAttributes",
		type = @NestTypeUsage(ManifestEntryAttributes.class),
		info = @NestInformation("Specifies attributes for the main attributes of the Manifest.\n"
				+ "Main-Class and similar attributes should be defined here."))
@NestFieldInformation(value = "EntryAttributes",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { ManifestEntryName.class, ManifestEntryAttributes.class }),
		info = @NestInformation("Specifies attributes for a given entry in the Manifest.\n"
				+ "The option keys should be the entry names while the values are a map of attributes for the given entry."))
public interface JarManifestTaskOption {
	public default JarManifestTaskOption clone() {
		return new SimpleJarManifestTaskOption(this);
	}

	public Map<String, String> getMainAttributes();

	public Map<String, Map<String, String>> getEntryAttributes();
}