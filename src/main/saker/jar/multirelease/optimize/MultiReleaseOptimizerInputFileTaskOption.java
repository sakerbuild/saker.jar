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

import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.zip.api.create.ZipCreatorTaskOutput;

public abstract class MultiReleaseOptimizerInputFileTaskOption {
	public abstract FileLocation toFileLocation(TaskContext taskcontext);

	public static MultiReleaseOptimizerInputFileTaskOption valueOf(ZipCreatorTaskOutput input) {
		return valueOf(input.getPath());
	}
	
	public static MultiReleaseOptimizerInputFileTaskOption valueOf(String input) {
		return valueOf(SakerPath.valueOf(input));
	}

	public static MultiReleaseOptimizerInputFileTaskOption valueOf(SakerPath input) {
		if (input.isAbsolute()) {
			return valueOf(ExecutionFileLocation.create(input));
		}
		return new MultiReleaseOptimizerInputFileTaskOption() {
			@Override
			public FileLocation toFileLocation(TaskContext taskcontext) {
				return ExecutionFileLocation.create(taskcontext.getTaskWorkingDirectoryPath().resolve(input));
			}
		};
	}

	public static MultiReleaseOptimizerInputFileTaskOption valueOf(FileLocation input) {
		return new MultiReleaseOptimizerInputFileTaskOption() {
			@Override
			public FileLocation toFileLocation(TaskContext taskcontext) {
				return input;
			}
		};
	}
}
