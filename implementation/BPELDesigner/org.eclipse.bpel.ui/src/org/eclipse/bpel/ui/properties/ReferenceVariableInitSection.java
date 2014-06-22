package org.eclipse.bpel.ui.properties;

import org.eclipse.bpel.common.ui.details.IDetailsAreaConstants;
import org.eclipse.bpel.common.ui.details.widgets.DecoratedLabel;
import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
import org.eclipse.bpel.common.ui.flatui.FlatFormData;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.ReferenceType;
import org.eclipse.bpel.model.ReferenceVariable;
import org.eclipse.bpel.ui.commands.SetReferenceVariableReferenceTypeCommand;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

/**
 * <b>Purpose:</b> Provides UI elements to initialize all other (not the
 * valueType) elements of a ReferenceVariable in the "Details" section of a
 * ReferenceVariable. <br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Licensed under the Apache License, Version 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0<br>
 * <b>Company:</b> SIMPL<br>
 * 
 * @author Michael Hahn <hahnml@studi.informatik.uni-stuttgart.de> <br>
 * @version $Id$ <br>
 * @link http://code.google.com/p/simpl09/
 * 
 */
public class ReferenceVariableInitSection extends BPELPropertySection {

	protected CCombo fReferenceTypeCombo;

	ReferenceVariable fVariable;

	@Override
	protected void basicSetInput(EObject input) {
		super.basicSetInput(input);

		fVariable = (ReferenceVariable) input;
		
		// preselect the reference type of the variable
		this.fReferenceTypeCombo.setText(fVariable.getReferenceType().getName());
	}

	protected void createReferenceTypeWidgets(Composite composite) {
		FlatFormData data;

		DecoratedLabel refTypeLabel = new DecoratedLabel(composite, SWT.LEFT);
		fWidgetFactory.adapt(refTypeLabel);
		refTypeLabel.setText("Reference type:");
		refTypeLabel
				.setToolTipText("Specify which kind of reference should be used.");

		fReferenceTypeCombo = fWidgetFactory.createCCombo(composite, SWT.NONE);
		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, BPELUtil.calculateLabelWidth(
				refTypeLabel, STANDARD_LABEL_WIDTH_AVG));
		data.right = new FlatFormAttachment(30, (-2)
				* IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE);
		fReferenceTypeCombo.setLayoutData(data);

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, 0);
		data.right = new FlatFormAttachment(fReferenceTypeCombo,
				-IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(fReferenceTypeCombo, 0, SWT.CENTER);
		refTypeLabel.setLayoutData(data);

		// Load the model data in the referenceType combo
		fReferenceTypeCombo.setItems(getReferenceTypes());

		if (fVariable != null && fVariable.getReferenceType() != null) {
			fReferenceTypeCombo.setText(fVariable.getReferenceType().getName());
		}

		fReferenceTypeCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				String selection = fReferenceTypeCombo.getText();

				fVariable = (ReferenceVariable) getInput();

				Command cmd = new SetReferenceVariableReferenceTypeCommand(
						fVariable,
						selection.equals(ReferenceType.ON_INSTANTIATION
								.getName()) ? ReferenceType.ON_INSTANTIATION
								: ReferenceType.FRESH);
				getCommandFramework().execute(wrapInShowContextCommand(cmd));
			}
		});
		
		fReferenceTypeCombo.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent e) {
				if (fVariable != null && fVariable.getReferenceType() != null) {
					fReferenceTypeCombo.setText(fVariable.getReferenceType().getName());
				}
			}
			
			public void focusGained(FocusEvent e) {
				if (fVariable != null && fVariable.getReferenceType() != null) {
					fReferenceTypeCombo.setText(fVariable.getReferenceType().getName());
				}
			}
		});
	}

	@Override
	protected void createClient(Composite parent) {

		Composite composite = createFlatFormComposite(parent);
		createReferenceTypeWidgets(composite);

		fVariable = (ReferenceVariable) getInput();

		if (fVariable != null && fVariable.getReferenceType() != null) {
			fReferenceTypeCombo.setText(fVariable.getReferenceType().getName());
		}
	}

	private String[] getReferenceTypes() {
		EList<EEnumLiteral> literals = BPELPackage.eINSTANCE.getReferenceType()
				.getELiterals();

		String[] types = new String[literals.size()];

		int index = 0;
		for (EEnumLiteral literal : literals) {
			types[index] = literal.getName();
			index++;
		}

		return types;
	}
}
