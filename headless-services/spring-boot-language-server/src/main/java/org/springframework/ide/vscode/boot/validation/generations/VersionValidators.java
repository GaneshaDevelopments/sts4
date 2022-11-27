/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.springframework.ide.vscode.boot.java.rewrite.SpringBootUpgrade;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.boot.validation.generations.preferences.VersionValidationProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;

import com.google.common.collect.ImmutableList;

public class VersionValidators {

	public static final String BOOT_VERSION_VALIDATION_CODE = "BOOT_VERSION_VALIDATION_CODE";

	private final VersionValidator[] validators;

	public VersionValidators(DiagnosticSeverityProvider diagnosticSeverityProvider) {
		this.validators = new VersionValidator[] {
//				new SupportedOssValidator(diagnosticSeverityProvider),
//				new UnsupportedCommercialValidator(diagnosticSeverityProvider),
//				new UnsupportedOssValidator(diagnosticSeverityProvider),
//				new SupportedCommercialValidator(diagnosticSeverityProvider),
				new UpdateLatestMajorVersion(diagnosticSeverityProvider),
				new UpdateLatestMinorVersion(diagnosticSeverityProvider),
				new UpdateLatestPatchVersion(diagnosticSeverityProvider) };
	}

	public List<VersionValidator> getValidators() {
		return Arrays.asList(this.validators);
	}

	private static class SupportedOssValidator extends AbstractDiagnosticValidator {

		public SupportedOssValidator(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Diagnostic validate(ResolvedSpringProject springProject, IJavaProject javaProject,
				Generation javaProjectGen, Version javaProjectVersion) throws Exception {

			if (VersionValidationUtils.isOssValid(javaProjectGen)) {
				VersionValidationProblemType problemType = VersionValidationProblemType.SUPPORTED_OSS_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("OSS support for Spring Boot " + javaProjectGen.getName() + " ends on: ");
				message.append(javaProjectGen.getOssSupportEndDate());

				return createDiagnostic(problemType, message.toString());
			}
			return null;
		}
	}

	private static class SupportedCommercialValidator extends AbstractDiagnosticValidator {

		public SupportedCommercialValidator(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Diagnostic validate(ResolvedSpringProject springProject, IJavaProject javaProject,
				Generation javaProjectGen, Version javaProjectVersion) throws Exception {

			if (VersionValidationUtils.isCommercialValid(javaProjectGen)) {

				VersionValidationProblemType problemType = VersionValidationProblemType.SUPPORTED_COMMERCIAL_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Commercial support for Spring Boot " + javaProjectGen.getName() + " ends on: ");
				message.append(javaProjectGen.getCommercialSupportEndDate());

				return createDiagnostic(problemType, message.toString());
			}
			return null;
		}
	}

	private static class UnsupportedOssValidator extends AbstractDiagnosticValidator {

		public UnsupportedOssValidator(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Diagnostic validate(ResolvedSpringProject springProject, IJavaProject javaProject,
				Generation javaProjectGen, Version javaProjectVersion) throws Exception {
			if (!VersionValidationUtils.isOssValid(javaProjectGen)) {

				VersionValidationProblemType problemType = VersionValidationProblemType.UNSUPPORTED_OSS_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("OSS support for Spring Boot " + javaProjectGen.getName() + " no longer available, ended on: ");
				message.append(javaProjectGen.getOssSupportEndDate());

				return createDiagnostic(problemType, message.toString());
			}
			return null;
		}
	}

	private static class UnsupportedCommercialValidator extends AbstractDiagnosticValidator {

		public UnsupportedCommercialValidator(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Diagnostic validate(ResolvedSpringProject springProject, IJavaProject javaProject,
				Generation javaProjectGen, Version javaProjectVersion) throws Exception {
			if (!VersionValidationUtils.isCommercialValid(javaProjectGen)) {

				VersionValidationProblemType problemType = VersionValidationProblemType.UNSUPPORTED_COMMERCIAL_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Commercial support for Spring Boot " + javaProjectGen.getName() + " no longer available, ended on: ");
				message.append(javaProjectGen.getCommercialSupportEndDate());

				return createDiagnostic(problemType, message.toString());
			}
			return null;
		}
	}

	private static class UpdateLatestPatchVersion extends AbstractDiagnosticValidator {

		public UpdateLatestPatchVersion(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Diagnostic validate(ResolvedSpringProject springProject, IJavaProject javaProject,
				Generation javaProjectGen, Version javaProjectVersion) throws Exception {
			Version latest = VersionValidationUtils.getNewerLatestPatchRelease(springProject.getReleases(), javaProjectVersion);

			if (latest != null) {
				VersionValidationProblemType problemType = VersionValidationProblemType.UPDATE_LATEST_PATCH_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Newer patch version of Spring Boot available: ");
				message.append(latest.toString());

				CodeAction ca = new CodeAction();
				ca.setKind(CodeActionKind.QuickFix);
				ca.setTitle("Upgrade to Version " + latest.toString());
				String commandId = SpringBootUpgrade.CMD_UPGRADE_SPRING_BOOT;
				ca.setCommand(new Command("Upgrade to Version " + latest.toString(), commandId,
						ImmutableList.of(javaProject.getLocationUri().toString(), latest.toString())));

				
				return createDiagnostic(ca, problemType, message.toString());
			}
			return null;
		}
	}

	private static class UpdateLatestMinorVersion extends AbstractDiagnosticValidator {

		public UpdateLatestMinorVersion(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Diagnostic validate(ResolvedSpringProject springProject, IJavaProject javaProject,
				Generation javaProjectGen, Version javaProjectVersion) throws Exception {
			Version latest = VersionValidationUtils.getNewerLatestMinorRelease(springProject.getReleases(), javaProjectVersion);

			if (latest != null) {
				VersionValidationProblemType problemType = VersionValidationProblemType.UPDATE_LATEST_MINOR_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Newer minor version of Spring Boot available: ");
				message.append(latest.toString());

				CodeAction ca = new CodeAction();
				ca.setKind(CodeActionKind.QuickFix);
				ca.setTitle("Upgrade to Version " + latest.toString());
				String commandId = SpringBootUpgrade.CMD_UPGRADE_SPRING_BOOT;
				ca.setCommand(new Command("Upgrade to Version " + latest.toString(), commandId,
						ImmutableList.of(javaProject.getLocationUri().toString(), latest.toString())));

				
				return createDiagnostic(ca, problemType, message.toString());
			}
			return null;
		}
	}

	private static class UpdateLatestMajorVersion extends AbstractDiagnosticValidator {

		public UpdateLatestMajorVersion(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Diagnostic validate(ResolvedSpringProject springProject, IJavaProject javaProject,
				Generation javaProjectGen, Version javaProjectVersion) throws Exception {
			Version latest = VersionValidationUtils.getNewerLatestMajorRelease(springProject.getReleases(), javaProjectVersion);

			if (latest != null) {
				VersionValidationProblemType problemType = VersionValidationProblemType.UPDATE_LATEST_MAJOR_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Newer major version of Spring Boot available: ");
				message.append(latest.toString());

				CodeAction ca = new CodeAction();
				ca.setKind(CodeActionKind.QuickFix);
				ca.setTitle("Upgrade to Version " + latest.toString());
				String commandId = SpringBootUpgrade.CMD_UPGRADE_SPRING_BOOT;
				ca.setCommand(new Command("Upgrade to Version " + latest.toString(), commandId,
						ImmutableList.of(javaProject.getLocationUri().toString(), latest.toString())));

				
				return createDiagnostic(ca, problemType, message.toString());
			}
			return null;
		}
	}



}
