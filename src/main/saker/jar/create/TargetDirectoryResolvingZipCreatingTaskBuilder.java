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

import java.util.Date;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.std.api.file.location.FileLocation;
import saker.zip.api.create.IncludeResourceMapping;
import saker.zip.api.create.ZipCreationTaskBuilder;
import saker.zip.api.create.ZipResourceTransformerFactory;

class TargetDirectoryResolvingZipCreatingTaskBuilder implements ZipCreationTaskBuilder {
	private ZipCreationTaskBuilder builder;
	private SakerPath targetDirectory;

	public TargetDirectoryResolvingZipCreatingTaskBuilder(ZipCreationTaskBuilder builder, SakerPath targetDirectory) {
		this.builder = builder;
		this.targetDirectory = targetDirectory;
	}

	@Override
	public void setOutputPath(SakerPath outputPath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setModificationTime(Date modificationTime) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addIncludeArchive(FileLocation archive, IncludeResourceMapping mapping) {
		builder.addIncludeArchive(archive,
				IncludeResourceMapping.chain(IncludeResourceMapping.targetDirectory(targetDirectory), mapping));
	}

	@Override
	public void addResource(FileLocation location, SakerPath archivepath) {
		builder.addResource(location, targetDirectory.resolve(archivepath));
	}

	@Override
	public void addResourceTransformer(ZipResourceTransformerFactory transformer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TaskIdentifier buildTaskIdentifier() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TaskFactory<?> buildTaskFactory() {
		throw new UnsupportedOperationException();
	}

}
