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
package saker.jar.create.transformer;

import saker.build.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.build.thirdparty.org.objectweb.asm.ModuleVisitor;
import saker.build.thirdparty.org.objectweb.asm.Opcodes;

public class ModuleInfoInjectingClassVisitor extends ClassVisitor {
	private static final int API = Opcodes.ASM7;

	private String mainClass;
	private String version;

	public ModuleInfoInjectingClassVisitor(ClassVisitor classVisitor, String mainClass, String version) {
		super(API, classVisitor);
		this.mainClass = mainClass;
		this.version = version;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if ((access & Opcodes.ACC_MODULE) != Opcodes.ACC_MODULE) {
			throw new IllegalArgumentException(
					"module-info.class doesn't represent a module class file. Cannot inject attributes.");
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		if (this.version != null) {
			version = this.version;
		}
		ModuleVisitor supermv = super.visitModule(name, access, version);
		if (this.mainClass == null) {
			return supermv;
		}
		return new MainClassInjectingModuleVisitor(supermv, mainClass);
	}

	private static class MainClassInjectingModuleVisitor extends ModuleVisitor {
		private String mainClass;

		public MainClassInjectingModuleVisitor(ModuleVisitor moduleVisitor, String mainClass) {
			super(API, moduleVisitor);
			this.mainClass = mainClass;
		}

		@Override
		public void visitMainClass(String mainClass) {
			//overwrite
			if (this.mainClass != null) {
				super.visitMainClass(this.mainClass);
				//null out to not inject in visitEnd()
				this.mainClass = null;
			}
		}

		@Override
		public void visitEnd() {
			if (this.mainClass != null) {
				super.visitMainClass(this.mainClass);
			}
			super.visitEnd();
		}
	}

}
