package org.eclipse.bpel.ui.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.bpel.common.ui.editmodel.AbstractEditModelCommand;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.simtech.FragmentWrapper;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.commands.util.AutoUndoCommand;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.bpel.ui.wizards.simtech.FragmentWizard;
import org.eclipse.bpel.ui.wizards.simtech.WizardModel;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.wizard.WizardDialog;

public class CompoundCommand extends AutoUndoCommand {
	private List<Command> commandList = new ArrayList<Command>();
	
	/**
	 * @author sonntamo
	 */
	WizardModel wmod = null;
	
	public CompoundCommand() { 
		super(new ArrayList<Object>());
	}

	public CompoundCommand(String label) {
		super(label, new ArrayList<Object>());
	}
	
	public void add(Command command) {
		if (command != null) {			
			commandList.add(command);
		}
	}
	
	public boolean canExecute() {
		if (commandList.size() == 0)
			return false;
		for (Command cmd : commandList) {
			if (cmd == null)
				return false;
			if (!cmd.canExecute())
				return false;
		}
		return true;
	}
	
	public boolean canDoExecute() {
		if (commandList.size() == 0)
			return false;
		for (Command cmd : commandList) {
			if (cmd == null)
				return false;
			if ((cmd instanceof AutoUndoCommand) && !((AutoUndoCommand)cmd).canDoExecute())
				return false;
		}
		return true;
	}
	
	public void dispose() {
		for (Command cmd : commandList)
			cmd.dispose();
	}
	
	public void doExecute() {
		
		/*
		 * Use the fragment wizard to configure fragment insertion
		 *     
		 * @sonntamo
		 */
		if (commandList != null && commandList.size() > 0) {
		    Command command = commandList.get(0);
		    if (command instanceof InsertInContainerCommand) {
		    	InsertInContainerCommand iicc = (InsertInContainerCommand)command;
		    	if (iicc.child instanceof FragmentWrapper) {
			    	int returnStatus = openFragmentWizard(iicc);
			    	
			    	// if the dialog was canceled, the commands mustn't be executed
			    	if (returnStatus == WizardDialog.CANCEL)
			    		return; 
		    	}
		    }
		}
	    
		
	    // now execute the contained commands
		for (Command cmd : commandList) {
			if (cmd instanceof AutoUndoCommand) {
				((AutoUndoCommand)cmd).doExecute();
			} else {
				cmd.execute();
			}			
		}
	}
	
	public String getLabel() {
		String label = super.getLabel();
		if (label == null)
			if (commandList.isEmpty())
				return null;
		if (label != null)
			return label;
		return commandList.get(0).getLabel();
	}
	
	public boolean isEmpty() {
		return commandList.isEmpty();
	}
	
	public List<Command> getCommands() {
		return commandList;
	}
	
	@Override
	public Set<Object> getModelRoots() {
		HashSet<Object> result = new HashSet<Object>();
		for (Command command : commandList) {
			if (command instanceof AutoUndoCommand) {
				result.addAll(((AutoUndoCommand)command).getModelRoots());
			} else if (command instanceof AbstractEditModelCommand) {				
				for (Resource res : ((AbstractEditModelCommand)command).getResources()) {
					if (res instanceof BPELResource) {
						result.add(((BPELResource)res).getProcess());
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param iicc
	 * @return
	 * 
	 * @author sonntamo
	 */
	private int openFragmentWizard(InsertInContainerCommand iicc) {
		
		// read wsdls and xsds from the project
		BPELEditor editor = ModelHelper.getBPELEditor(iicc.parent);
	    Process process = editor.getProcess();
	    
	    FragmentWrapper frag = (FragmentWrapper)iicc.child;
	    
	    // model behind the wizard
		wmod = new WizardModel();
		wmod.setProcess(process);
		wmod.setFragment(frag.getCompiledFragment());
		wmod.setWsdls(frag.getFragment().getWsdls());
		wmod.setXsds(frag.getFragment().getXsds());
		wmod.setDD(frag.getFragment().getDD());
		
		//@hahnml
//		wmod.setAssociatedElements(frag.getFragment().getAssociatedElement());
		
		wmod.resetFlags();
		
		// create the wizard
		FragmentWizard wizard = new FragmentWizard(wmod, process, frag.getCompiledFragment());
		wizard.init(editor.getSite().getWorkbenchWindow().getWorkbench(), 
				null);
		
		// Instantiates the wizard container with the wizard and opens it
		WizardDialog dialog = new WizardDialog( editor.getSite().getShell(), wizard);
		dialog.create();
		return dialog.open(); 
	}
}
