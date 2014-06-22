package org.eclipse.bpel.ui.wizards.simtech;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.Extension;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.adapters.INamespaceMap;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * 
 * @author sonntamo
 */
public class FragmentWizard extends Wizard implements INewWizard {

	// @hahnml:
	private boolean isProcessFragment = false;

	public void setModel(WizardModel model) {
		this.model = model;
	}

	// wizard pages
	PageWSDL wsdlPage;
	PageXSD xsdPage;

	// @hahnml:
	PageVariableConflicts variablePage;
	PagePartnerLinkConflicts partnerLinkPage;
	PageCorrelationSet corSetPage;
	PageMessageExchange mexPage;
	PageExtension extPage;
	PageNamespaceConflicts nsPage;

	// workbench selection when the wizard was started
	protected IStructuredSelection selection;

	// the workbench instance
	protected IWorkbench workbench;

	protected WizardModel model;

	protected Process process;

	protected Activity compiledFragment;

	public FragmentWizard(WizardModel model, Process process,
			BPELExtensibleElement compiledFragment) {
		super();

		// @hahnml: Initialize the FragmentUtils and calculate conflicts if the
		// fragment contains a process description
		FragmentUtils.getUtils(model);
		if (compiledFragment instanceof Process) {
			isProcessFragment = true;
			FragmentUtils.getUtils(model)
					.calculateAllProcessFragmentConflicts();
		}

		this.model = model;
		this.process = process;
		// @hahnml: Check if the compiled fragment is a process or activity
		if (isProcessFragment) {
			this.compiledFragment = ((Process) compiledFragment).getActivity();
		} else {
			this.compiledFragment = (Activity) compiledFragment;
		}

	}

	public void addPages() {
		// @hahnml: Create and add the new pages
		variablePage = new PageVariableConflicts(workbench, selection, model);
		partnerLinkPage = new PagePartnerLinkConflicts(workbench, selection,
				model);
		corSetPage = new PageCorrelationSet(workbench, selection, model);
		mexPage = new PageMessageExchange(workbench, selection, model);
		extPage = new PageExtension(workbench, selection, model);
		addPage(variablePage);
		addPage(partnerLinkPage);
		addPage(corSetPage);
		addPage(mexPage);
		addPage(extPage);

		wsdlPage = new PageWSDL(workbench, selection, model);
		addPage(wsdlPage);
		xsdPage = new PageXSD(workbench, selection, model);
		addPage(xsdPage);
		nsPage = new PageNamespaceConflicts(workbench, selection, model);
		addPage(nsPage);
	}

	@Override
	public IWizardPage getStartingPage() {
		return variablePage;
	}

	@Override
	public boolean performFinish() {

		FileCreator
				.createWsdlsForImport(model.wsdls, process, compiledFragment);
		FileCreator.createXsdsForImport(model.xsds, process, compiledFragment);
		if (model.dd != null)
			FileCreator.updateDD(model.dd, process);

		// Get the process from the FragmentUtils
		Process compiledProcess = FragmentUtils.getUtils(null)
				.getFragmentProcess();

		// @hahnml: Add the associated elements to the process
		if (compiledProcess != null) {
			if (compiledProcess.getVariables() != null
					&& !compiledProcess.getVariables().getChildren().isEmpty()) {
				// Loop through all fragment variables and check if they should
				// be added
				List<Variable> variables = new ArrayList<Variable>();
				variables.addAll(compiledProcess.getVariables().getChildren());

				for (Variable var : variables) {
					// If a variable was removed over the wizard it won't be
					// added to the process
					if (!FragmentUtils.getUtils(null).getRemovedEObjects()
							.contains(var)) {
						process.getVariables().getChildren().add(var);
					}
				}
			}
			if (compiledProcess.getPartnerLinks() != null
					&& !compiledProcess.getPartnerLinks().getChildren()
							.isEmpty()) {
				// Loop through all fragment partnerLinks and check if they
				// should be added
				List<PartnerLink> partnerLinks = new ArrayList<PartnerLink>();
				partnerLinks.addAll(compiledProcess.getPartnerLinks()
						.getChildren());

				for (PartnerLink ptnl : partnerLinks) {
					// If a partnerLink was removed over the wizard it won't be
					// added to the process
					if (!FragmentUtils.getUtils(null).getRemovedEObjects()
							.contains(ptnl)) {
						process.getPartnerLinks().getChildren().add(ptnl);
					}
				}
			}
			if (compiledProcess.getCorrelationSets() != null
					&& !compiledProcess.getCorrelationSets().getChildren()
							.isEmpty()) {
				// Loop through all fragment correlation sets and check if they
				// should
				// be added
				List<CorrelationSet> correlationSets = new ArrayList<CorrelationSet>();
				correlationSets.addAll(compiledProcess.getCorrelationSets()
						.getChildren());

				for (CorrelationSet cor : correlationSets) {
					// If a correlation set was removed over the wizard it won't
					// be
					// added to the process
					if (!FragmentUtils.getUtils(null).getRemovedEObjects()
							.contains(cor)) {
						process.getCorrelationSets().getChildren().add(cor);
					}
				}
			}
			if (compiledProcess.getMessageExchanges() != null
					&& !compiledProcess.getMessageExchanges().getChildren()
							.isEmpty()) {
				// Loop through all fragment message exchanges and check if they
				// should
				// be added
				List<MessageExchange> messageExchanges = new ArrayList<MessageExchange>();
				messageExchanges.addAll(compiledProcess.getMessageExchanges()
						.getChildren());

				for (MessageExchange mex : messageExchanges) {
					// If a message exchange was removed over the wizard it
					// won't be
					// added to the process
					if (!FragmentUtils.getUtils(null).getRemovedEObjects()
							.contains(mex)) {
						process.getMessageExchanges().getChildren().add(mex);
					}
				}
			}
			if (compiledProcess.getExtensions() != null
					&& !compiledProcess.getExtensions().getChildren().isEmpty()) {
				// Loop through all fragment extensions and check if they should
				// be added
				List<Extension> extensions = new ArrayList<Extension>();
				extensions
						.addAll(compiledProcess.getExtensions().getChildren());

				for (Extension ext : extensions) {
					// If a extension was removed over the wizard it won't be
					// added to the process
					if (!FragmentUtils.getUtils(null).getRemovedEObjects()
							.contains(ext)) {
						process.getExtensions().getChildren().add(ext);
					}
				}
			}
		}
		
		INamespaceMap<String, String> compiledProcessNS = BPELUtils.getNamespaceMap(compiledProcess);
		for (String prefix : compiledProcessNS.keySet()) {
			if (!BPELUtils.getNamespaceMap(process).keySet().contains(prefix)) {
				BPELUtils.setNamespacePrefix(process, compiledProcessNS.get(prefix), prefix);
			}
		}

		// refresh project in workspace
		IFile bpelFile = BPELUtil.getBPELFile(process);
		try {
			// @schrotbn : code simplified
			IProject proj = bpelFile.getProject();
			proj.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	public boolean canFinish() {
		// @hahnml: Changed that the wizard could only completed on the last
		// page
		// @schrotbn Wizard can only complete after resolving namespace conflicts 
		if (this.getContainer().getCurrentPage() == nsPage && nsPage.isPageComplete())
			return true;
		return false;
	}

}
