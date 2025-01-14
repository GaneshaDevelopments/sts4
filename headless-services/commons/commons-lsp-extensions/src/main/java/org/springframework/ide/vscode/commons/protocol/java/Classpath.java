/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Classpath {

	public static final String ENTRY_KIND_SOURCE = "source";
	public static final String ENTRY_KIND_BINARY = "binary";
	public static final Classpath EMPTY = new Classpath(Collections.<CPE>emptyList());

	private List<CPE> entries;

	public Classpath(List<CPE> entries) {
		super();
		this.entries = entries;
	}

	public List<CPE> getEntries() {
		return entries;
	}

	public void setEntries(List<CPE> entries) {
		this.entries = entries;
	}

	@Override
	public String toString() {
		return "Classpath [entries=" + entries + "]";
	}

	public static class CPE {

		// TODO: it seems like a good idea to make all classpath entries the same in that they all have
		// - a place with source code
		// - a place with compiled code
		// - a place with java doc
		// So it seems like we should be able to chnage this so that the same named attribute is used
		// in both cases (rather then one be 'getOutputFolder' and one 'getPath' to obtain location of the compiled code).

		private String kind;

		private String path; // TODO: Change to File, Path or URL?
		private String outputFolder;
		private URL sourceContainerUrl;
		private URL javadocContainerUrl;
		private boolean isSystem = false;
		private boolean isOwn = false;
		private boolean isTest = false;
		private boolean isJavaContent = false;
		
		private Map<String, String> extra;

		public CPE() {}

		public CPE(String kind, String path) {
			super();
			this.kind = kind;
			setPath(path);
		}

		public CPE(String kind, String path, Map<String, String> extra) {
			super();
			this.kind = kind;
			this.extra = extra;
			setPath(path);
		}
		
		public Map<String, String> getExtra() {
			return extra;
		}

		public void setExtra(Map<String, String> extra) {
			this.extra = extra;
		}

		public String getOutputFolder() {
			return outputFolder;
		}

		public void setOutputFolder(String outputFolder) {
			this.outputFolder = outputFolder;
		}

		public String getKind() {
			return kind;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public URL getJavadocContainerUrl() {
			return javadocContainerUrl;
		}

		public void setJavadocContainerUrl(URL javadocContainerUrl) {
			this.javadocContainerUrl = javadocContainerUrl;
		}

		public URL getSourceContainerUrl() {
			return sourceContainerUrl;
		}

		public void setSourceContainerUrl(URL sourceContainerUrl) {
			this.sourceContainerUrl = sourceContainerUrl;
		}

		public static CPE binary(String path) {
			return new CPE(ENTRY_KIND_BINARY, path);
		}

		public static CPE source(File sourceFolder, File outputFolder) {
			CPE cpe = new CPE(ENTRY_KIND_SOURCE, sourceFolder.getAbsolutePath());
			cpe.setOutputFolder(outputFolder.getAbsolutePath());
			return cpe;
		}

		public static CPE source(File sourceFolder, File outputFolder, Map<String, String> extra) {
			CPE cpe = new CPE(ENTRY_KIND_SOURCE, sourceFolder.getAbsolutePath(), extra);
			cpe.setOutputFolder(outputFolder.getAbsolutePath());
			return cpe;
		}
		
		public boolean isSystem() {
			return isSystem;
		}

		public void setSystem(boolean isSystem) {
			this.isSystem = isSystem;
		}

		public boolean isOwn() {
			return isOwn;
		}

		public void setOwn(boolean isOwn) {
			this.isOwn = isOwn;
		}

		public boolean isTest() {
			return isTest;
		}

		public void setTest(boolean isTest) {
			this.isTest = isTest;
		}

		public boolean isJavaContent() {
			return isJavaContent;
		}

		public void setJavaContent(boolean isJavaContent) {
			this.isJavaContent = isJavaContent;
		}

		@Override
		public String toString() {
			return "CPE [kind=" + kind + ", path=" + path + ", outputFolder=" + outputFolder + ", sourceContainerUrl="
					+ sourceContainerUrl + ", javadocContainerUrl=" + javadocContainerUrl + ", isSystem=" + isSystem
					+ ", isOwn=" + isOwn + ", isTest=" + isTest + ", isJavaContent=" + isJavaContent + ", extra="
					+ extra + "]";
		}

		@Override
		public int hashCode() {
			return Objects.hash(extra, isJavaContent, isOwn, isSystem, isTest, javadocContainerUrl, kind, outputFolder,
					path, sourceContainerUrl);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CPE other = (CPE) obj;
			return Objects.equals(extra, other.extra) && isJavaContent == other.isJavaContent && isOwn == other.isOwn
					&& isSystem == other.isSystem && isTest == other.isTest
					&& Objects.equals(javadocContainerUrl, other.javadocContainerUrl)
					&& Objects.equals(kind, other.kind) && Objects.equals(outputFolder, other.outputFolder)
					&& Objects.equals(path, other.path) && Objects.equals(sourceContainerUrl, other.sourceContainerUrl);
		}

	}

	public static boolean isSource(CPE e) {
		return e!=null && Classpath.ENTRY_KIND_SOURCE.equals(e.getKind());
	}

	public static boolean isBinary(CPE e) {
		return e!=null && Classpath.ENTRY_KIND_BINARY.equals(e.getKind());
	}
	
	public static boolean isProjectSource(CPE e) {
		return isSource(e) && e.isOwn();
	}
	
	public static boolean isProjectJavaSource(CPE cpe) {
		return isProjectSource(cpe) && cpe.isJavaContent();
	}
	
	public static boolean isProjectNonTestJavaSource(CPE cpe) {
		return isProjectJavaSource(cpe) && !cpe.isTest();
	}
	
	public static boolean isProjectTestJavaSource(CPE cpe) {
		return isProjectJavaSource(cpe) && cpe.isTest();
	}


}