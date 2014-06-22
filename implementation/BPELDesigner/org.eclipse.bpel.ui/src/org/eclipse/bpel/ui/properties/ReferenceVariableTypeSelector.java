/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.bpel.ui.properties;

import org.eclipse.bpel.common.ui.details.IDetailsAreaConstants;
import org.eclipse.bpel.common.ui.details.widgets.DecoratedLabel;
import org.eclipse.bpel.common.ui.details.widgets.StatusLabel2;
import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
import org.eclipse.bpel.common.ui.flatui.FlatFormData;
import org.eclipse.bpel.common.ui.flatui.FlatFormLayout;
import org.eclipse.bpel.model.ReferenceVariable;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.Messages;
import org.eclipse.bpel.ui.adapters.ILabeledElement;
import org.eclipse.bpel.ui.details.providers.ModelTreeLabelProvider;
import org.eclipse.bpel.ui.details.providers.ReferenceVariableValueTypeTreeContentProvider;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.BrowseUtil;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.eclipse.xsd.XSDTypeDefinition;

/**
 * A composite for selecting either a "Data Type" (XSD type, or XSD element
 * containing an anonymous XSD type) an "Interface" type (actually a WSDL
 * message, specified by choosing then Interface+Operation+direction/fault)
 */
public class ReferenceVariableTypeSelector extends Composite {

	protected int lastChangeContext = -1;

	protected boolean inUpdate = false;

	public static final int STANDARD_LABEL_WIDTH_SM = 125;
	public static final int STANDARD_LABEL_WIDTH_AVG = STANDARD_LABEL_WIDTH_SM * 5 / 4;

	protected static final int DATATYPE_BROWSE_CONTEXT = 2;

	// private static final StructuredViewer dataTypeViewer = null;

	protected XSDTypeDefinition variableType;
	protected ReferenceVariable myVariable;

	protected Composite dataTypeComposite;

	protected TabbedPropertySheetWidgetFactory wf;

	protected Button dataTypeBrowseButton;
	protected Hyperlink dataTypeNameText;
	protected Tree dataTypeTree;
	protected TreeViewer dataTypeTreeViewer;
	protected StatusLabel2 dataTypeLabel;

	protected BPELEditor bpelEditor;
	protected Callback callback;
	protected Shell shell;
	protected boolean allowElements = false;
	protected boolean nullFilterAdded = false;

	public ReferenceVariableTypeSelector(Composite parent, int style,
			BPELEditor bpelEditor, TabbedPropertySheetWidgetFactory wf,
			Callback callback, boolean allowElements) {
		super(parent, style);
		this.bpelEditor = bpelEditor;
		this.shell = bpelEditor.getSite().getShell();
		this.wf = wf;
		this.callback = callback;
		this.allowElements = allowElements;

		Composite parentComposite = createComposite(this);
		this.setLayout(new FillLayout(SWT.VERTICAL));

		FlatFormLayout formLayout = new FlatFormLayout();

		formLayout.marginWidth = formLayout.marginHeight = 0;
		parentComposite.setLayout(formLayout);

		createDataTypeWidgets(parentComposite);
	}

	/**
	 * Expects an XSD type
	 */
	public void setVariableType(XSDTypeDefinition variableType) {
		// System.out.println("setVariableType: "+variableType);
		this.variableType = variableType;
		refresh();
	}

	/**
	 * This method is preferred over the EObject method if the caller has a
	 * BPELVariable model object, because this method supports hints stored in
	 * the VariableExtension.
	 */
	public void setVariable(ReferenceVariable variable) {
		myVariable = variable;

		if (variable != null) {
			if (variable.getValueType() != null) {
				setVariableType(variable.getValueType()); return;
			}
		}
		setVariableType(null);

	}

	protected void updateDataTypeWidgets() {

		String name = null;
		ILabeledElement label = (ILabeledElement) BPELUtil.adapt(variableType,
				ILabeledElement.class, myVariable);
		if (label != null) {
			name = label.getLabel(variableType);
		}
		
		dataTypeLabel.setText("Reference Value Type:");
		
		if (name == null) {
			dataTypeNameText.setText(Messages.VariableTypeSelector_None_1);
			dataTypeNameText.setEnabled(false);
			dataTypeTreeViewer.setInput(null);
			return;
		}

		if (variableType instanceof XSDTypeDefinition) {
			dataTypeNameText.setText(name);
			dataTypeNameText.setEnabled(true);
			dataTypeTreeViewer.setInput(variableType);
		}

		// scroll to the top ...
		ScrollBar bar = dataTypeTree.getVerticalBar();
		if (bar != null) {
			bar.setSelection(0);
		}

	}

	protected void doChildLayout() {
		dataTypeComposite.layout(true);
	}

	/**
	 * 
	 */

	public void refresh() {
		updateDataTypeWidgets();
	}

	/**
	 * Returns an XSD type or null.
	 * 
	 * @return the variable type
	 */

	public XSDTypeDefinition getVariableType() {
		return variableType;
	}

	protected Composite createFlatFormComposite(Composite parent) {
		Composite result = createComposite(parent);
		FlatFormLayout formLayout = new FlatFormLayout();
		formLayout.marginWidth = formLayout.marginHeight = 0;
		result.setLayout(formLayout);
		return result;

	}

	protected void createDataTypeWidgets(Composite parent) {
		FlatFormData data;

		Composite composite = dataTypeComposite = createFlatFormComposite(parent);

		data = new FlatFormData();
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VMARGIN);
		data.left = new FlatFormAttachment(0, 0);
		data.right = new FlatFormAttachment(100, 0);
		data.bottom = new FlatFormAttachment(100, 0);
		dataTypeComposite.setLayoutData(data);

		dataTypeBrowseButton = createButton(composite,
				Messages.VariableTypeSelector_Browse_2, SWT.PUSH);
		DecoratedLabel label = new DecoratedLabel(composite, SWT.LEFT);
		label.setText(Messages.VariableTypeSelector_Data_Type_2);
		wf.adapt(label);
		dataTypeLabel = new StatusLabel2(label);
		dataTypeNameText = createHyperlink(composite, "", SWT.NONE); //$NON-NLS-1$
		dataTypeNameText.setToolTipText(Messages.VariableTypeSelector_3);
		dataTypeNameText.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				BPELUtil.openEditor(getVariableType(), bpelEditor);
			}
		});

		Label dataTypeTreeLabel = createLabel(composite,
				Messages.VariableTypeSelector_2);

		// Tree viewer for variable structure ...
		dataTypeTree = wf.createTree(composite, SWT.NONE);
		ReferenceVariableValueTypeTreeContentProvider variableContentProvider = new ReferenceVariableValueTypeTreeContentProvider(
				true, true);
		dataTypeTreeViewer = new TreeViewer(dataTypeTree);
		dataTypeTreeViewer.setContentProvider(variableContentProvider);
		dataTypeTreeViewer.setLabelProvider(new ModelTreeLabelProvider());
		dataTypeTreeViewer.setInput(null);
		dataTypeTreeViewer.setAutoExpandLevel(3);
		// end tree viewer for variable structure

		// layout data type label
		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, BPELUtil.calculateLabelWidth(
				dataTypeLabel.getControl(), STANDARD_LABEL_WIDTH_SM));
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VSPACE);
		data.right = new FlatFormAttachment(60, 0);
		dataTypeNameText.setLayoutData(data);

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, IDetailsAreaConstants.HSPACE);
		data.right = new FlatFormAttachment(dataTypeNameText,
				-IDetailsAreaConstants.HSPACE);
		// data.top = new FlatFormAttachment(dataTypeNameText,
		// IDetailsAreaConstants.VSPACE, SWT.TOP);
		data.bottom = new FlatFormAttachment(dataTypeNameText, 0, SWT.BOTTOM);
		dataTypeLabel.setLayoutData(data);

		data = new FlatFormData();
		data.top = new FlatFormAttachment(dataTypeNameText, -2, SWT.TOP);
		data.bottom = new FlatFormAttachment(dataTypeLabel.getLabel(), +2,
				SWT.BOTTOM);
		data.right = new FlatFormAttachment(100, -IDetailsAreaConstants.HSPACE);
		dataTypeBrowseButton.setLayoutData(data);

		dataTypeBrowseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				Object xsdType = BrowseUtil.browseForXSDTypeOrElement(
						bpelEditor.getProcess(), getShell());
				if (xsdType != null) {
					lastChangeContext = DATATYPE_BROWSE_CONTEXT;
					if (xsdType instanceof XSDTypeDefinition) {
						// TODO:WDG: if type is anonymous, use enclosing element
						callback.selectXSDType((XSDTypeDefinition) xsdType);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, IDetailsAreaConstants.HSPACE);
		data.top = new FlatFormAttachment(dataTypeLabel.getLabel(),
				3 * IDetailsAreaConstants.VSPACE, SWT.BOTTOM);
		dataTypeTreeLabel.setLayoutData(data);

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, BPELUtil.calculateLabelWidth(
				dataTypeLabel.getLabel(), STANDARD_LABEL_WIDTH_SM));
		data.top = new FlatFormAttachment(dataTypeTreeLabel, 0, SWT.TOP);
		data.right = new FlatFormAttachment(100, -IDetailsAreaConstants.HSPACE);
		data.bottom = new FlatFormAttachment(100, -IDetailsAreaConstants.HSPACE);
		dataTypeTree.setLayoutData(data);

	}

	public interface Callback {
		public void selectXSDType(XSDTypeDefinition xsdType);
	}

	public Object getUserContext() {
		return new Integer(lastChangeContext);
	}

	public void restoreUserContext(Object userContext) {
		switch (((Integer) userContext).intValue()) {
		case DATATYPE_BROWSE_CONTEXT:
			dataTypeBrowseButton.setFocus();
			return;
		}
		throw new IllegalStateException();
	}

	@Override
	public void setEnabled(boolean enabled) {
		setEnabled(enabled, this, 0);
	}

	void setEnabled(boolean enabled, Control control, int depth) {

		if (control instanceof Composite) {
			Composite root = (Composite) control;
			Control list[] = root.getChildren();
			for (int i = 0; i < list.length; i++) {
				setEnabled(enabled, list[i], depth + 1);
			}
		}
		if (depth > 0) {
			control.setEnabled(enabled);
		}
	}

	protected Button createButton(Composite parent, String text, int style) {
		return wf.createButton(parent, text, style);
	}

	protected Composite createComposite(Composite parent) {
		return wf.createComposite(parent);
	}

	protected Label createLabel(Composite parent, String text) {
		return wf.createLabel(parent, text);
	}

	protected Hyperlink createHyperlink(Composite parent, String text, int style) {
		return wf.createHyperlink(parent, text, style);
	}

	protected CCombo createCCombo(Composite parent) {
		return wf.createCCombo(parent);
	}
}
