package saker.jar.multirelease.optimize;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;

final class MultiReleaseOptimizerWorkerTaskOutputImpl
		implements MultiReleaseOptimizerWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath outputFilePath;

	/**
	 * For {@link Externalizable}.
	 */
	public MultiReleaseOptimizerWorkerTaskOutputImpl() {
	}

	public MultiReleaseOptimizerWorkerTaskOutputImpl(SakerPath outfilepath) {
		this.outputFilePath = outfilepath;
	}

	@Override
	public SakerPath getPath() {
		return outputFilePath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputFilePath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputFilePath = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outputFilePath == null) ? 0 : outputFilePath.hashCode());
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
		MultiReleaseOptimizerWorkerTaskOutputImpl other = (MultiReleaseOptimizerWorkerTaskOutputImpl) obj;
		if (outputFilePath == null) {
			if (other.outputFilePath != null)
				return false;
		} else if (!outputFilePath.equals(other.outputFilePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MultiReleaseOptimizerWorkerTaskOutputImpl["
				+ (outputFilePath != null ? "outputFilePath=" + outputFilePath : "") + "]";
	}
}