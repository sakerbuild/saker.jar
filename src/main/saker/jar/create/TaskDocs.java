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

	@NestTypeInformation(kind = TypeInformationKind.MAP,
			qualifiedName = "java.util.Map",
			relatedTypes = @NestTypeUsage(Map.class),
			elementTypes = { @NestTypeUsage(ManifestAttributeKey.class), @NestTypeUsage(ManifestAttributeValue.class) })
	@NestInformation("Map of string key-value attribute entries for the main section of the Java Archive Manifest file.")

	@NestFieldInformation(value = "Main-Class",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("Manifest attribute used for launching applications packaged in JAR files.\n"
					+ "The Main-Class attribute is used in conjunction with the -jar command-line option of the java application launcher."))

	@NestFieldInformation(value = "Manifest-Version",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("This attribute indicates the version number of the manifest standard to which a JAR file's manifest conforms."))

	@NestFieldInformation(value = "Created-By",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("Defines the version and the vendor of the java implementation on top of which this manifest file is generated."))

	@NestFieldInformation(value = "Class-Path",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("The value of this attribute specifies the relative URLs of the extensions or libraries that this application or extension needs. \n"
					+ "URLs are separated by one or more spaces. The application or extension class loader uses the value of this attribute to construct its internal search path."))
	@NestFieldInformation(value = "Sealed",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("This attribute defines whether this JAR file is sealed or not.\n"
					+ "The value can be either \"true\" or \"false\", case is ignored. If it is set to \"true\", then all the packages in "
					+ "the JAR file are defaulted to be sealed, unless they are defined otherwise individually."))
	@NestFieldInformation(value = "Implementation-Title",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("The value is a string that defines the title of the extension implementation."))
	@NestFieldInformation(value = "Implementation-Version",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("The value is a string that defines the version of the extension implementation."))
	@NestFieldInformation(value = "Implementation-Vendor",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("The value is a string that defines the organization that maintains the extension implementation."))
	@NestFieldInformation(value = "Implementation-Vendor-Id",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			deprecated = true,
			info = @NestInformation("The value is a string id that uniquely defines the organization that maintains the extension implementation."))
	@NestFieldInformation(value = "Implementation-URL",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			deprecated = true,
			info = @NestInformation("This attribute defines the URL from which the extension implementation can be downloaded from."))
	@NestFieldInformation(value = "Specification-Title",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("The value is a string that defines the title of the extension specification."))
	@NestFieldInformation(value = "Specification-Version",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("The value is a string that defines the version of the extension specification."))
	@NestFieldInformation(value = "Specification-Vendor",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			info = @NestInformation("The value is a string that defines the organization that maintains the extension specification."))
	@NestFieldInformation(value = "Extension-List",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			deprecated = true,
			info = @NestInformation("This attribute indicates the extensions that are needed by the applet.\n"
					+ "Each extension listed in this attribute will have a set of additional attributes that the applet uses to specify "
					+ "which version and vendor of the extension it requires."))
	@NestFieldInformation(value = "Extension-Installation",
			type = @NestTypeUsage(ManifestAttributeValue.class),
			deprecated = true,
			info = @NestInformation("Manifest attribute used for declaring dependencies on installed extensions."))
	public static class ManifestMainAttributes {
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
