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
package saker.jar.multirelease.optimize;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities.MirroredFileContents;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ArrayUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import saker.build.trace.BuildTrace;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;

public class MultiReleaseOptimizerWorkerTaskFactory implements TaskFactory<MultiReleaseOptimizerWorkerTaskOutput>,
		Task<MultiReleaseOptimizerWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final String META_INF_VERSIONS = "META-INF/versions/";

	private FileLocation input;

	/**
	 * For {@link Externalizable}.
	 */
	public MultiReleaseOptimizerWorkerTaskFactory() {
	}

	public MultiReleaseOptimizerWorkerTaskFactory(FileLocation input) {
		this.input = input;
	}

	@Override
	public Task<? extends MultiReleaseOptimizerWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public MultiReleaseOptimizerWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(
				MultiReleaseOptimizerTaskFactory.TASK_NAME + ":" + SakerStandardUtils.getFileLocationFileName(input));

		MultiReleaseOptimizerWorkerTaskIdentifier taskid = (MultiReleaseOptimizerWorkerTaskIdentifier) taskcontext
				.getTaskId();

		SakerDirectory builddir = SakerPathFiles.requireBuildDirectory(taskcontext);
		SakerPath outputrelativepath = taskid.getOutput();
		SakerDirectory outputparentdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(builddir,
				SakerPath.valueOf(MultiReleaseOptimizerTaskFactory.TASK_NAME).resolve(outputrelativepath.getParent()));

		String outputfilename = outputrelativepath.getFileName();

		Path mirrorpath = taskcontext
				.mirror(outputparentdir, OnlyDirectoryCreateSynchronizeDirectoryVisitPredicate.INSTANCE)
				.resolve(outputfilename);

		input.accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				try {
					SakerPath path = loc.getPath();
					MirroredFileContents mirrored = taskcontext.getTaskUtilities().mirrorFileAtPathContents(path);
					taskcontext.reportInputFileDependency(null, path, mirrored.getContents());
					handleLocalFile(mirrored.getPath());
				} catch (NullPointerException | IOException e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(path, UUID.randomUUID());
				handleLocalFile(LocalFileProvider.toRealPath(path));
			}

			private void handleLocalFile(Path path) {
				try (ZipFile zf = new ZipFile(path.toFile())) {
					try (OutputStream os = Files.newOutputStream(mirrorpath);
							ZipOutputStream zos = new ZipOutputStream(os)) {
						performOptimization(zf, zos);
					}
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});

		//XXX should use a better utility function
		taskcontext.getTaskUtilities().addSynchronizeInvalidatedProviderPathFileToDirectory(outputparentdir,
				LocalFileProvider.getInstance().getPathKey(mirrorpath), outputfilename);
		SakerFile outfile = outputparentdir.get(outputfilename);
		outfile.synchronize();

		SakerPath outfilepath = outfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outfilepath, outfile.getContentDescriptor());

		MultiReleaseOptimizerWorkerTaskOutputImpl result = new MultiReleaseOptimizerWorkerTaskOutputImpl(outfilepath);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	protected static void performOptimization(ZipFile zf, ZipOutputStream zos) throws IOException {
		byte[] buffer = new byte[StreamUtils.DEFAULT_BUFFER_SIZE * 2];
		{
			Enumeration<? extends ZipEntry> en = zf.entries();
			while (en.hasMoreElements()) {
				ZipEntry entry = en.nextElement();
				if (!entry.getName().startsWith(META_INF_VERSIONS)) {
					copyEntry(zf, zos, entry, buffer);
					continue;
				}
				ZipEntry preventry = getPreviousEntry(zf, entry);
				if (preventry == null) {
					copyEntry(zf, zos, entry, buffer);
					continue;
				}
				//a previous entry was found
				//check content equality
				try (InputStream previs = zf.getInputStream(preventry)) {
					try (InputStream currentis = zf.getInputStream(entry)) {
						if (inputStreamsEqual(previs, currentis, buffer)) {
							//we can skip this entry, as the previous one is already or will be added
							continue;
						}
					}
				}
				copyEntry(zf, zos, entry, buffer);
				continue;
			}
		}
	}

	private static boolean inputStreamsEqual(InputStream first, InputStream second, byte[] buffer) throws IOException {
		if (buffer == null || buffer.length <= 2) {
			while (true) {
				int f = first.read();
				int s = second.read();
				if (f != s) {
					return false;
				}
				if (f < 0) {
					return true;
				}
			}
		}

		final int regionlen = buffer.length / 2;

		int fc = 0;
		int sc = 0;
		while (true) {
			if (fc == 0) {
				fc = first.read(buffer, 0, regionlen);
			}
			if (sc == 0) {
				sc = second.read(buffer, regionlen, regionlen);
			}
			int common = Math.min(fc, sc);
			if (common <= 0) {
				//the streams equal if both of them ended
				return (fc <= 0) == (sc <= 0);
			}
			int mismatch = ArrayUtils.mismatch(buffer, 0, buffer, regionlen, common);
			if (mismatch >= 0) {
				return false;
			}
			//all bytes equal in the range
			if (fc > common) {
				System.arraycopy(buffer, common, buffer, 0, fc - common);
			}
			if (sc > common) {
				System.arraycopy(buffer, regionlen + common, buffer, regionlen, sc - common);
			}
			fc -= common;
			sc -= common;
		}
	}

	private static boolean isMultiReleaseVersionInteger(String s) {
		if (s.isEmpty()) {
			return false;
		}
		char c = s.charAt(0);
		if (c < '1' || c > '9') {
			return false;
		}
		int len = s.length();
		if (len == 1) {
			//the number 8 is not a valid multi-release version num
			return c == '9';
		}
		for (int i = 1; i < len; i++) {
			c = s.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	private static ZipEntry getPreviousEntry(ZipFile zf, ZipEntry en) {
		if (en.isDirectory()) {
			return null;
		}
		String n = en.getName();
		int slashidx = n.indexOf('/', META_INF_VERSIONS.length());
		if (slashidx < 0) {
			//no next slash found after the versions/ directory, no previous entry
			return null;
		}
		if (slashidx + 1 == n.length()) {
			return null;
		}
		String i = n.substring(META_INF_VERSIONS.length(), slashidx);
		if (!isMultiReleaseVersionInteger(i)) {
			return null;
		}
		String namewithslash = n.substring(slashidx);
		int v;
		try {
			v = Integer.parseUnsignedInt(i);
		} catch (NumberFormatException e) {
			//the versione
			return null;
		}
		while (--v > 8) {
			ZipEntry beforee = zf.getEntry(META_INF_VERSIONS + v + namewithslash);
			if (beforee != null) {
				return beforee;
			}
		}
		return zf.getEntry(namewithslash.substring(1));
	}

	private static void copyEntry(ZipFile zf, ZipOutputStream zos, ZipEntry entry, byte[] buffer) throws IOException {
		if (entry.isDirectory()) {
			zos.putNextEntry(entry);
			zos.closeEntry();
		} else {
			try (InputStream is = zf.getInputStream(entry)) {
				zos.putNextEntry(entry);
				if (!entry.isDirectory()) {
					StreamUtils.copyStream(is, zos, buffer);
				}
				zos.closeEntry();
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(input);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		input = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
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
		MultiReleaseOptimizerWorkerTaskFactory other = (MultiReleaseOptimizerWorkerTaskFactory) obj;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MultiReleaseOptimizerWorkerTaskFactory[" + (input != null ? "input=" + input : "") + "]";
	}

}
