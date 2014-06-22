package org.eclipse.bpel.ui.agora.debug.properties;

import org.eclipse.bpel.common.ui.details.IDetailsAreaConstants;
import org.eclipse.bpel.common.ui.details.IOngoingChange;
import org.eclipse.bpel.common.ui.details.widgets.DecoratedLabel;
import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
import org.eclipse.bpel.common.ui.flatui.FlatFormData;
import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.BreakpointTypeEnum;
import org.eclipse.bpel.debug.debugmodel.Breakpoints;
import org.eclipse.bpel.debug.debugmodel.Debug;
import org.eclipse.bpel.debug.debugmodel.DebugPackage;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.ui.agora.debug.DebugModelHelper;
import org.eclipse.bpel.ui.agora.debug.commands.AddBreakpointCommand;
import org.eclipse.bpel.ui.agora.debug.commands.AddBreakpointTypeCommand;
import org.eclipse.bpel.ui.agora.debug.commands.RemoveBreakpointTypeCommand;
import org.eclipse.bpel.ui.commands.CompoundCommand;
import org.eclipse.bpel.ui.commands.SetCommand;
import org.eclipse.bpel.ui.properties.BPELPropertySection;
import org.eclipse.bpel.ui.properties.ChangeTracker;
import org.eclipse.bpel.ui.properties.EditController;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author hahnml
 * 
 */
public class DebugSection extends BPELPropertySection {

	public static final int SPLIT_POINT = 55;
	public static final int SPLIT_POINT_OFFSET = 3 * IDetailsAreaConstants.HSPACE;

	private Debug debug;
	private Breakpoint breakpoint;
	private Breakpoints breakpointsType;

	protected EditController fNameEditHelper;
	protected ChangeTracker breakpointNameTracker;

	protected Text fName;
	protected Button fEnabled;

	protected Group fBreakpointGroup;

	protected Button fActivityReady;
	protected Button fActivityExecuted;
	protected Button fActivityFaulted;
	protected Button fEvaluatingTransitionConditionFaulted;
	protected Button fScopeCompensating;
	protected Button fScopeHandlingTermination;
	protected Button fScopeCompleteWithFault;
	protected Button fScopeHandlingFault;
	protected Button fLoopIterationComplete;
	protected Button fLoopConditionTrue;
	protected Button fLoopConditionFalse;
	protected Button fLinkEvaluated;

	@Override
	protected void createClient(Composite parent) {

		Composite composite = createFlatFormComposite(parent);

		Composite ref = createNameWidgets(composite);
		ref = createEnabledWidgets(ref, composite);
		ref = createTypeWidgets(ref, composite);

		createChangeTrackers();
	}

	@Override
	protected void basicSetInput(EObject input) {
		super.basicSetInput(input);

		Debug debug = getBPELEditor().getDebug();

		this.debug = debug;

		if (getInput() != null) {

			String elementName = "";
			String elementXPath = "";

			if (getInput() instanceof Process) {
				this.breakpointsType = this.debug.getGlobalBreakpoints();

				elementName = getProcess().getName();
				elementXPath = getProcess().getXPath();
			} else {
				this.breakpointsType = this.debug.getLocalBreakpoints();

				BPELExtensibleElement element = DebugModelHelper.getElement(getInput());
				if (element instanceof Activity) {
					elementName = ((Activity)element).getName();
					elementXPath = ((Activity)element).getXPath();
				} else if (element instanceof Link){
					elementName = ((Link)element).getName();
					elementXPath = ((Link)element).getXPath();
				}
			}

			this.breakpoint = DebugModelHelper.getBreakpoint(elementXPath,
					this.breakpointsType);

			if (this.breakpoint == null) {
				this.breakpoint = DebugModelHelper.createNewDefaultBreakpoint(
						elementXPath, elementName);

				getCommandFramework().execute(
						new AddBreakpointCommand(this.breakpointsType,
								this.breakpoint));
			} else {
				if (this.breakpoint.getTargetName() != null) {
					// If the (target) element name has changed we update it in
					// the
					// breakpoint
					if (!this.breakpoint.getTargetName().equals(elementName)) {
						getCommandFramework().execute(
								new SetCommand(this.breakpoint, elementName,
										DebugPackage.eINSTANCE
												.getBreakpoint_TargetName()));
					}
				}
			}
		}
	}

	private Composite createEnabledWidgets(Composite top, Composite parent) {
		FlatFormData data;

		final Composite composite = createFlatFormComposite(parent);
		data = new FlatFormData();
		data.top = new FlatFormAttachment(top, IDetailsAreaConstants.VSPACE);
		data.left = new FlatFormAttachment(0, IDetailsAreaConstants.HSPACE);
		data.right = new FlatFormAttachment(100, (-2)
				* IDetailsAreaConstants.HSPACE);
		composite.setLayoutData(data);

		// Create a new Checkbox and connect it with the enabled-feature of the
		// breakpoint
		fEnabled = createButton(composite, "Enabled", DebugPackage.eINSTANCE
				.getBreakpoint_Enabled(), false, SWT.CHECK);

		return composite;
	}

	private Composite createNameWidgets(Composite parent) {
		FlatFormData data;

		final Composite composite = createFlatFormComposite(parent);

		data = new FlatFormData();
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE);
		data.left = new FlatFormAttachment(0, IDetailsAreaConstants.HSPACE);
		data.right = new FlatFormAttachment(100, (-2)
				* IDetailsAreaConstants.HSPACE);

		composite.setLayoutData(data);

		DecoratedLabel nameLabel = new DecoratedLabel(composite, SWT.LEFT);
		fWidgetFactory.adapt(nameLabel);
		nameLabel.setText("Name");

		fName = fWidgetFactory.createText(composite, EMPTY_STRING);
		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, BPELUtil.calculateLabelWidth(
				nameLabel, STANDARD_LABEL_WIDTH_AVG));
		data.right = new FlatFormAttachment(100, (-2)
				* IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE);
		fName.setLayoutData(data);

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, 0);
		data.right = new FlatFormAttachment(fName,
				-IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(fName, 0, SWT.CENTER);
		nameLabel.setLayoutData(data);

		return composite;
	}

	private Composite createTypeWidgets(Composite top, Composite parent) {
		FlatFormData data;

		final Composite composite = createFlatFormComposite(parent);
		data = new FlatFormData();
		data.top = new FlatFormAttachment(top, IDetailsAreaConstants.VSPACE);
		data.left = new FlatFormAttachment(0, 0);
		data.right = new FlatFormAttachment(100, (-2)
				* IDetailsAreaConstants.HSPACE);
		composite.setLayoutData(data);

		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		fBreakpointGroup = getWidgetFactory().createGroup(composite,
				"Breakpoint Type");
		layout = new GridLayout();
		fBreakpointGroup.setLayout(layout);
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		fBreakpointGroup.setLayoutData(gridData);

		// Create all buttons and connect them with the type-feature of the
		// breakpoint
		fActivityReady = createButton(fBreakpointGroup,
				BreakpointTypeEnum.ACTIVITY_READY.getName(),
				BreakpointTypeEnum.ACTIVITY_READY, false, SWT.CHECK);

		fActivityExecuted = createButton(fBreakpointGroup,
				BreakpointTypeEnum.ACTIVITY_EXECUTED.getName(),
				BreakpointTypeEnum.ACTIVITY_EXECUTED, false, SWT.CHECK);

		fActivityFaulted = createButton(fBreakpointGroup,
				BreakpointTypeEnum.ACTIVITY_FAULTED.getName(),
				BreakpointTypeEnum.ACTIVITY_FAULTED, false, SWT.CHECK);

		fEvaluatingTransitionConditionFaulted = createButton(fBreakpointGroup,
				BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED
						.getName(),
				BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED,
				false, SWT.CHECK);

		fScopeCompensating = createButton(fBreakpointGroup,
				BreakpointTypeEnum.SCOPE_COMPENSATING.getName(),
				BreakpointTypeEnum.SCOPE_COMPENSATING, false, SWT.CHECK);

		fScopeHandlingTermination = createButton(fBreakpointGroup,
				BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION.getName(),
				BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION, false, SWT.CHECK);

		fScopeCompleteWithFault = createButton(fBreakpointGroup,
				BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT.getName(),
				BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT, false, SWT.CHECK);

		fScopeHandlingFault = createButton(fBreakpointGroup,
				BreakpointTypeEnum.SCOPE_HANDLING_FAULT.getName(),
				BreakpointTypeEnum.SCOPE_HANDLING_FAULT, false, SWT.CHECK);

		fLoopIterationComplete = createButton(fBreakpointGroup,
				BreakpointTypeEnum.LOOP_ITERATION_COMPLETE.getName(),
				BreakpointTypeEnum.LOOP_ITERATION_COMPLETE, false, SWT.CHECK);

		fLoopConditionTrue = createButton(fBreakpointGroup,
				BreakpointTypeEnum.LOOP_CONDITION_TRUE.getName(),
				BreakpointTypeEnum.LOOP_CONDITION_TRUE, false, SWT.CHECK);

		fLoopConditionFalse = createButton(fBreakpointGroup,
				BreakpointTypeEnum.LOOP_CONDITION_FALSE.getName(),
				BreakpointTypeEnum.LOOP_CONDITION_FALSE, false, SWT.CHECK);

		fLinkEvaluated = createButton(fBreakpointGroup,
				BreakpointTypeEnum.LINK_EVALUATED.getName(),
				BreakpointTypeEnum.LINK_EVALUATED, false, SWT.CHECK);

		return composite;
	}

	Button createButton(Composite parent, String label, Object object,
			boolean checked, int type) {

		Button button = fWidgetFactory.createButton(parent, label, type);

		button.setData(object);
		button.setSelection(checked);

		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.widget;
				buttonPressed(b.getData(), b.getSelection());
			}
		});

		return button;
	}

	// Act on button pressed events
	protected void buttonPressed(Object object, boolean selection) {
		if (this.breakpoint != null) {
			if (object instanceof EStructuralFeature) {
				getCommandFramework().execute(
						new SetCommand(breakpoint, selection,
								(EStructuralFeature) object));
			} else {
				// object instanceof BreakpointTypeEnum

				if (selection) {
					// if the type was selected, we add it to the list of types
					getCommandFramework().execute(
							new AddBreakpointTypeCommand(breakpoint,
									(BreakpointTypeEnum) object));
				} else {
					// if the type was deselected, we remove it from the list of
					// types
					getCommandFramework().execute(
							new RemoveBreakpointTypeCommand(breakpoint,
									(BreakpointTypeEnum) object));
				}
			}
		}
	}

	@Override
	public void refresh() {
		super.refresh();
		updateNameWidget();
		updateEnabledWidget();
		updateTypeWidget();
	}

	protected void createChangeTrackers() {
		IOngoingChange change = new IOngoingChange() {
			public String getLabel() {
				return "Debug Change";
			}

			public Command createApplyCommand() {
				String s = fName.getText();
				if ("".equals(s))
					s = null;
				CompoundCommand result = new CompoundCommand();
				if (s != null) {
					// Set the new name to the breakpoint
					result.add(new SetCommand(breakpoint, s,
							DebugPackage.eINSTANCE.getBreakpoint_Name()));
				}

				return wrapInShowContextCommand(result);
			}

			public void restoreOldState() {
				updateNameWidget();
			}
		};
		breakpointNameTracker = new ChangeTracker(fName, change,
				getCommandFramework());
	}

	protected void updateNameWidget() {
		Assert.isNotNull(getInput());

		breakpointNameTracker.stopTracking();
		try {

			fName
					.setText(breakpoint.getName() == null ? "" : breakpoint.getName()); //$NON-NLS-1$

		} finally {
			breakpointNameTracker.startTracking();
		}

	}

	protected void updateEnabledWidget() {
		fEnabled.setSelection(breakpoint.isEnabled());
	}

	protected void updateTypeWidget() {
		Assert.isNotNull(getInput());

		//Update available breakpoint types
		// TODO: Handle all the different activity types and their breakpoint
		// types
		if (getInput() instanceof ForEach || getInput() instanceof RepeatUntil
				|| getInput() instanceof While) {
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
		} else if (getInput() instanceof Scope) {
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
		} else if (getInput() instanceof Invoke){
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
		} else if (getInput() instanceof Link) {
			fActivityReady.setEnabled(false);
			fActivityExecuted.setEnabled(false);
			fActivityFaulted.setEnabled(false);
			fEvaluatingTransitionConditionFaulted.setEnabled(false);
			fScopeCompensating.setEnabled(false);
			fScopeHandlingTermination.setEnabled(false);
			fScopeCompleteWithFault.setEnabled(false);
			fScopeHandlingFault.setEnabled(false);
			fLoopIterationComplete.setEnabled(false);
			fLoopConditionTrue.setEnabled(false);
			fLoopConditionFalse.setEnabled(false);
			fLinkEvaluated.setEnabled(true);
		} else if (getInput() instanceof Process) {
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
		
		//Update selection
		fActivityReady.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.ACTIVITY_READY));
		fActivityExecuted.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.ACTIVITY_EXECUTED));
		fActivityFaulted.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.ACTIVITY_FAULTED));
		fEvaluatingTransitionConditionFaulted.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.EVALUATING_TRANSITION_CONDITION_FAULTED));
		fScopeCompensating.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.SCOPE_COMPENSATING));
		fScopeHandlingTermination.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.SCOPE_HANDLING_TERMINATION));
		fScopeCompleteWithFault.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.SCOPE_COMPLETE_WITH_FAULT));
		fScopeHandlingFault.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.SCOPE_HANDLING_FAULT));
		fLoopIterationComplete.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.LOOP_ITERATION_COMPLETE));
		fLoopConditionTrue.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.LOOP_CONDITION_TRUE));
		fLoopConditionFalse.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.LOOP_CONDITION_FALSE));
		fLinkEvaluated.setSelection(breakpoint.getType().contains(BreakpointTypeEnum.LINK_EVALUATED));
	}
}
