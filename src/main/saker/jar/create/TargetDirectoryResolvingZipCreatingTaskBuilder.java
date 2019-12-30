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
