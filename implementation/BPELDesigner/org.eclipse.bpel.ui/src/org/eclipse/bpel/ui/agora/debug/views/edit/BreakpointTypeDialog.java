package org.eclipse.bpel.ui.agora.debug.views.edit;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.BreakpointTypeEnum;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.bpel.ui.agora.manager.XPathMapper;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class BreakpointTypeDialog extends TitleAreaDialog {

	private Button fActivityReady;
	private Button fActivityExecuted;
	private Button fActivityFaulted;
	private Button fEvaluatingTransitionConditionFaulted;
	private Button fScopeCompensating;
	private Button fScopeHandlingTermination;
	private Button fScopeCompleteWithFault;
	private Button fScopeHandlingFault;
	private Button fLoopIterationComplete;
	private Button fLoopConditionTrue;
	private Button fLoopConditionFalse;
	private Button fLinkEvaluated;

	EList<BreakpointTypeEnum> typeList;
	Breakpoint breakpoint;

	private BreakpointManagementView parent;

	public EList<BreakpointTypeEnum> getTypeList() {
		return this.typeList;
	}

	public BreakpointTypeDialog(Shell parentShell, Breakpoint breakpoint,
			BreakpointManagementView breakpointManagementView) {
		super(parentShell);
		this.typeList = breakpoint.getType();
		this.breakpoint = breakpoint;
		this.parent = breakpointManagementView;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Change the breakpoint type");
		setMessage("Select or deselect the types of the breakpoint",
				IMessageProvider.INFORMATION);
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;

		fActivityReady = new Button(composite, SWT.CHECK);
		fActivityReady.setText(BreakpointTypeEnum.ACTIVITY_READY.getName());
		fActivityReady.setSelection(this.typeList
				.contains(BreakpointTypeEnum.ACTIVITY_READY));

		fActivityExecuted = new Button(composite, SWT.CHECK);
		fActivityExecuted.setText(BreakpointTypeEnum.ACTIVITY_EXECUTED
				.getName());
		fActivityExecuted.setSelection(this.typeList
				.contains(BreakpointTypeEnum.ACTIVITY_EXECUTED));

		fActivityFaulted = new Button(composite, SWT.CHECK);
		fActivityFaulted.setText(BreakpointTypeEnum.ACTIVITY_FAULTED.getName());
		fActivityFaulted.setSelection(this.typeList
				.contains(BreakpointTypeEnum.ACTIVITY_FAULTED));

		fEvaluatingTransitionConditionFaulted = new Button(composite, SWT.CHECK);
		fEvaluatingTransitionConditionFaulted
				.setText(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED
						.getName());
		fEvaluatingTransitionConditionFaulted
				.setSelection(this.typeList
						.contains(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED));

		fScopeCompensating = new Button(composite, SWT.CHECK);
		fScopeCompensating.setText(BreakpointTypeEnum.SCOPE_COMPENSATING
				.getName());
		fScopeCompensating.setSelection(this.typeList
				.contains(BreakpointTypeEnum.SCOPE_COMPENSATING));

		fScopeHandlingTermination = new Button(composite, SWT.CHECK);
		fScopeHandlingTermination
				.setText(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION
						.getName());
		fScopeHandlingTermination.setSelection(this.typeList
				.contains(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION));

		fScopeCompleteWithFault = new Button(composite, SWT.CHECK);
		fScopeCompleteWithFault
				.setText(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT.getName());
		fScopeCompleteWithFault.setSelection(this.typeList
				.contains(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT));

		fScopeHandlingFault = new Button(composite, SWT.CHECK);
		fScopeHandlingFault.setText(BreakpointTypeEnum.SCOPE_HANDLING_FAULT
				.getName());
		fScopeHandlingFault.setSelection(this.typeList
				.contains(BreakpointTypeEnum.SCOPE_HANDLING_FAULT));

		fLoopIterationComplete = new Button(composite, SWT.CHECK);
		fLoopIterationComplete
				.setText(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE.getName());
		fLoopIterationComplete.setSelection(this.typeList
				.contains(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE));

		fLoopConditionTrue = new Button(composite, SWT.CHECK);
		fLoopConditionTrue.setText(BreakpointTypeEnum.LOOP_CONDITION_TRUE
				.getName());
		fLoopConditionTrue.setSelection(this.typeList
				.contains(BreakpointTypeEnum.LOOP_CONDITION_TRUE));

		fLoopConditionFalse = new Button(composite, SWT.CHECK);
		fLoopConditionFalse.setText(BreakpointTypeEnum.LOOP_CONDITION_FALSE
				.getName());
		fLoopConditionFalse.setSelection(this.typeList
				.contains(BreakpointTypeEnum.LOOP_CONDITION_FALSE));

		fLinkEvaluated = new Button(composite, SWT.CHECK);
		fLinkEvaluated.setText(BreakpointTypeEnum.LINK_EVALUATED.getName());
		fLinkEvaluated.setSelection(this.typeList
				.contains(BreakpointTypeEnum.LINK_EVALUATED));

		fActivityReady.setLayoutData(gridData);
		fActivityExecuted.setLayoutData(gridData);
		fActivityFaulted.setLayoutData(gridData);
		fEvaluatingTransitionConditionFaulted.setLayoutData(gridData);
		fScopeCompensating.setLayoutData(gridData);
		fScopeHandlingTermination.setLayoutData(gridData);
		fScopeCompleteWithFault.setLayoutData(gridData);
		fScopeHandlingFault.setLayoutData(gridData);
		fLoopIterationComplete.setLayoutData(gridData);
		fLoopConditionTrue.setLayoutData(gridData);
		fLoopConditionFalse.setLayoutData(gridData);
		fLinkEvaluated.setLayoutData(gridData);

		composite.setLayoutData(gridData);

		// Get the target element of the breakpoint by its xpath
		Object element = XPathMapper
				.handleXPath(this.breakpoint.getTargetXPath(), this.parent
						.getProcessManager().getProcess());

		if (element instanceof ForEach || element instanceof RepeatUntil
				|| element instanceof While) {
			fActivityReady.setEnabled(true);
			fActivityExecuted.setEnabled(true);
			fActivityFaulted.setEnabled(true);
			fEvaluatingTransitionConditionFaulted.setEnabled(false);
			fScopeCompensating.setEnabled(false);
			fScopeHandlingTermination.setEnabled(false);
			fScopeCompleteWithFault.setEnabled(false);
			fScopeHandlingFault.setEnabled(false);
			fLoopIterationComplete.setEnabled(true);
			fLoopConditionTrue.setEnabled(true);
			fLoopConditionFalse.setEnabled(true);
			fLinkEvaluated.setEnabled(false);
		} else if (element instanceof Scope) {
			fActivityReady.setEnabled(false);
			fActivityExecuted.setEnabled(false);
			fActivityFaulted.setEnabled(false);
			fEvaluatingTransitionConditionFaulted.setEnabled(false);
			fScopeCompensating.setEnabled(true);
			fScopeHandlingTermination.setEnabled(true);
			fScopeCompleteWithFault.setEnabled(true);
			fScopeHandlingFault.setEnabled(true);
			fLoopIterationComplete.setEnabled(false);
			fLoopConditionTrue.setEnabled(false);
			fLoopConditionFalse.setEnabled(false);
			fLinkEvaluated.setEnabled(false);
		} else if (element instanceof Invoke) {
			fActivityReady.setEnabled(true);
			fActivityExecuted.setEnabled(true);
			fActivityFaulted.setEnabled(true);
			fEvaluatingTransitionConditionFaulted.setEnabled(false);
			fScopeCompensating.setEnabled(true);
			fScopeHandlingTermination.setEnabled(true);
			fScopeCompleteWithFault.setEnabled(true);
			fScopeHandlingFault.setEnabled(true);
			fLoopIterationComplete.setEnabled(false);
			fLoopConditionTrue.setEnabled(false);
			fLoopConditionFalse.setEnabled(false);
			fLinkEvaluated.setEnabled(false);
		} else if (element instanceof Process) {
			fActivityReady.setEnabled(true);
			fActivityExecuted.setEnabled(true);
			fActivityFaulted.setEnabled(true);
			fEvaluatingTransitionConditionFaulted.setEnabled(true);
			fScopeCompensating.setEnabled(true);
			fScopeHandlingTermination.setEnabled(true);
			fScopeCompleteWithFault.setEnabled(true);
			fScopeHandlingFault.setEnabled(true);
			fLoopIterationComplete.setEnabled(true);
			fLoopConditionTrue.setEnabled(true);
			fLoopConditionFalse.setEnabled(true);
		} else {
			fActivityReady.setEnabled(true);
			fActivityExecuted.setEnabled(true);
			fActivityFaulted.setEnabled(true);
			fEvaluatingTransitionConditionFaulted.setEnabled(false);
			fScopeCompensating.setEnabled(false);
			fScopeHandlingTermination.setEnabled(false);
			fScopeCompleteWithFault.setEnabled(false);
			fScopeHandlingFault.setEnabled(false);
			fLoopIterationComplete.setEnabled(false);
			fLoopConditionTrue.setEnabled(false);
			fLoopConditionFalse.setEnabled(false);
			fLinkEvaluated.setEnabled(false);
		}

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okay = createButton(parent, IDialogConstants.IGNORE_ID,
				IDialogConstants.OK_LABEL, true);

		okay.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Clear the loaded type list
				typeList.clear();

				// Save the selected types to the type list
				if (fActivityReady.getSelection()) {
					typeList.add(BreakpointTypeEnum.ACTIVITY_READY);
				}
				if (fActivityExecuted.getSelection()) {
					typeList.add(BreakpointTypeEnum.ACTIVITY_EXECUTED);
				}
				if (fActivityFaulted.getSelection()) {
					typeList.add(BreakpointTypeEnum.ACTIVITY_FAULTED);
				}
				if (fEvaluatingTransitionConditionFaulted.getSelection()) {
					typeList.add(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED);
				}
				if (fScopeCompensating.getSelection()) {
					typeList.add(BreakpointTypeEnum.SCOPE_COMPENSATING);
				}
				if (fScopeHandlingTermination.getSelection()) {
					typeList.add(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION);
				}
				if (fScopeCompleteWithFault.getSelection()) {
					typeList.add(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT);
				}
				if (fScopeHandlingFault.getSelection()) {
					typeList.add(BreakpointTypeEnum.SCOPE_HANDLING_FAULT);
				}
				if (fLoopIterationComplete.getSelection()) {
					typeList.add(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE);
				}
				if (fLoopConditionTrue.getSelection()) {
					typeList.add(BreakpointTypeEnum.LOOP_CONDITION_TRUE);
				}
				if (fLoopConditionFalse.getSelection()) {
					typeList.add(BreakpointTypeEnum.LOOP_CONDITION_FALSE);
				}
				if (fLinkEvaluated.getSelection()) {
					typeList.add(BreakpointTypeEnum.LINK_EVALUATED);
				}

				close();
			}
		});

		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

}
