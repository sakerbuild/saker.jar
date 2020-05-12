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

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardUtils;

public class MultiReleaseOptimizerTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.jar.multi_release.optimize";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Input" }, required = true)
			public MultiReleaseOptimizerInputFileTaskOption inputOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				if (outputOption != null) {
					if (!outputOption.isForwardRelative()) {
						taskcontext.abortExecution(
								new InvalidPathFormatException("Output should be forward relative: " + outputOption));
					}
					if (outputOption.getFileName() == null) {
						taskcontext.abortExecution(
								new InvalidPathFormatException("Output should has no file name: " + outputOption));
					}
				}
				SakerPath output = this.outputOption;
				FileLocation filelocation = inputOption.toFileLocation(taskcontext);
				if (output == null) {
					output = SakerPath.valueOf(SakerStandardUtils.getFileLocationFileName(filelocation));
				}

				MultiReleaseOptimizerWorkerTaskIdentifier taskid = new MultiReleaseOptimizerWorkerTaskIdentifier(
						output);
				MultiReleaseOptimizerWorkerTaskFactory workertask = new MultiReleaseOptimizerWorkerTaskFactory(
						filelocation);
				taskcontext.startTask(taskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(taskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
