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
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class MultiReleaseOptimizerWorkerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath output;

	/**
	 * For {@link Externalizable}.
	 */
	public MultiReleaseOptimizerWorkerTaskIdentifier() {
	}

	public MultiReleaseOptimizerWorkerTaskIdentifier(SakerPath output) {
		this.output = output;
	}

	public SakerPath getOutput() {
		return output;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(output);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		output = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((output == null) ? 0 : output.hashCode());
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
		MultiReleaseOptimizerWorkerTaskIdentifier other = (MultiReleaseOptimizerWorkerTaskIdentifier) obj;
		if (output == null) {
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MultiReleaseOptimizerWorkerTaskIdentifier[" + (output != null ? "output=" + output : "") + "]";
	}

}
