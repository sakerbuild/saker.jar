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
package saker.jar.create;

import java.util.Map;

import saker.build.scripting.model.info.TypeInformationKind;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

public class TaskDocs {

	@NestTypeInformation(relatedTypes = @NestTypeUsage(int.class),
			qualifiedName = "MultiReleaseJavaMajorNumber",
			kind = TypeInformationKind.NUMBER,
			enumValues = {

					@NestFieldInformation(value = "8",
							info = @NestInformation("Multi-Release major version 8.\n"
									+ "The specified resources are placed in the root of the created archive.")),
					@NestFieldInformation(value = "9",
							info = @NestInformation("Multi-Release major version 9.\n"
									+ "The specified resources are placed under META-INF/versions/9/ directory.")),
					@NestFieldInformation(value = "10",
							info = @NestInformation("Multi-Release major version 10.\n"
									+ "The specified resources are placed under META-INF/versions/10/ directory.")),
					@NestFieldInformation(value = "11",
							info = @NestInformation("Multi-Release major version 11.\n"
									+ "The specified resources are placed under META-INF/versions/11/ directory.")),
					@NestFieldInformation(value = "12",
							info = @NestInformation("Multi-Release major version 12.\n"
									+ "The specified resources are placed under META-INF/versions/12/ directory.")),
					@NestFieldInformation(value = "13",
							info = @NestInformation("Multi-Release major version 13.\n"
									+ "The specified resources are placed under META-INF/versions/13/ directory.")),
					@NestFieldInformation(value = "14",
							info = @NestInformation("Multi-Release major version 14.\n"
									+ "The specified resources are placed under META-INF/versions/14/ directory.")),

			})
	@NestInformation("Represents the major version for which the Multi-Release contents are specified.")
	public static class MultiReleaseVersionKey {
	}

	@NestInformation("Specifies a Java service class name for which the given implementations should be defined for.")
	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "JavaServiceClassName")
	public static class ServicesKey {
	}

	@NestInformation("Specifies one or more a Java service implementations that should be declared by the created Java Archive.")
	@NestTypeInformation(kind = TypeInformationKind.COLLECTION,
			elementTypes = { @NestTypeUsage(ServiceImplementationName.class) },
			qualifiedName = "JavaServiceImplementationName")
	public static class ServicesValue {
	}

	@NestInformation("A Java service implementation name that is included in the service declarations of the Java Archive.")
	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "JavaServiceImplementationClassName")
	public static class ServiceImplementationName {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "ManifestAttributeName")
	@NestInformation("Represents an attribute name in a Java Archive Manifest file.")
	public static class ManifestAttributeKey {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "ManifestAttributeValue")
	@NestInformation("Represents an attribute value in a Java Archive Manifest file.")
	public static class ManifestAttributeValue {
	}

	@NestTypeInformation(kind = TypeInformationKind.STRING, qualifiedName = "ManifestEntry")
	@NestInformation("Represents an entry name to the the associated attribute map in a Java Archive Manifest file.")
	public static class ManifestEntryName {
	}

	@NestTypeInformation(kind = TypeInformationKind.MAP,
			qualifiedName = "java.util.Map",
			relatedTypes = @NestTypeUsage(Map.class),
			elementTypes = { @NestTypeUsage(ManifestAttributeKey.class), @NestTypeUsage(ManifestAttributeValue.class) })
	@NestInformation("Map of string key-value attribute entries for a Java Archive Manifest file.")
	public static class ManifestEntryAttributes {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "ModuleInfoMainAttribute")
	@NestInformation("Java main class name that is injected into the module-info.class entry of the created JAR.")
	public static class ModuleInfoMainInjectOption {
	}

	@NestTypeInformation(kind = TypeInformationKind.LITERAL, qualifiedName = "ModuleInfoVersionAttribute")
	@NestInformation("Version string that is injected into the module-info.class entry of the created JAR.")
	public static class ModuleInfoVersionInjectOption {
	}
}
