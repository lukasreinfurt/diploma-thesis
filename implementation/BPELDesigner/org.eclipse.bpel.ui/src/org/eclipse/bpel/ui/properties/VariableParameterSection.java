package org.eclipse.bpel.ui.properties;

import java.io.IOException;

import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
import org.eclipse.bpel.common.ui.flatui.FlatFormData;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.uiextensionmodel.VariableExtension;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author hahnml
 * 
 *         VariableParameterSection provides editing and viewing, if a variable
 *         is a parameter for the process instance start message.
 */
public class VariableParameterSection extends BPELPropertySection {

	/** The current variable being edited. */
	Variable fVariable;
	VariableExtension fVarExt;

	Button fParameter;
	Text fDefaultValue;
	Label fDefaultLabel;
	
	@Override
	protected void createClient(Composite parent) {
		Composite parameterComposite = createFlatFormComposite(parent);

		FlatFormData data = new FlatFormData();
		data.left = new FlatFormAttachment(0, 5);
		data.top = new FlatFormAttachment(0, 0);
		data.right = new FlatFormAttachment(100, 0);
		data.bottom = new FlatFormAttachment(100, 0);

		createParameterCheckBox(parameterComposite, data);
	}

	/**
	 * Creates a parameter check box for setting a variable as parameter
	 * 
	 * @param composite
	 * @param data
	 */
	private void createParameterCheckBox(Composite composite, FlatFormData data) {
		// tolevar
		fParameter = fWidgetFactory.createButton(composite,
				"Use variable as parameter", SWT.CHECK);
		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, 5);
		data.height = 20;
		fParameter.setLayoutData(data);

		fParameter.setEnabled(false);
		fParameter.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fVarExt.isParameter() != fParameter.getSelection()) {
					fVarExt.setParameter(fParameter.getSelection());
				}

				// @hahnml: This is a HACK to save the changed isParameter
				// value.
				// It is not possible to execute a command over the
				// command stack in case of a ClassCastException because
				// VariableExtension doesn't extend WSDLElement.
				try {
					getBPELEditor().getEditModelClient()
							.getExtensionsResourceInfo().save();
				} catch (IOException exc) {
					// TODO Auto-generated catch block
					exc.printStackTrace();
				}
				
				//@hahnml
				ProcessManager manager = MonitoringProvider.getInstance().getProcessManager(BPELUtils.getProcess(fVariable));
				
				if (fParameter.getSelection()) {
					manager.getParameterHandler().addParameterVariable(
							fVariable);
					fDefaultValue.setEnabled(true);
					String defVal = fVarExt.getDefault();
					fDefaultValue.setText(defVal!=null?defVal:"");
				} else {
					manager.getParameterHandler().removeParameterVariable(
							fVariable);
					fDefaultValue.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});
		
		fDefaultLabel = fWidgetFactory.createLabel(composite, "Default parameter value (optional): ");
		
		fDefaultValue = fWidgetFactory.createText(composite, "");
		
		data = new FlatFormData();
		data.top = new FlatFormAttachment(50, 0);
		data.right = new FlatFormAttachment(100, 0);
		data.left = new FlatFormAttachment(0, 5 + BPELUtil.calculateLabelWidth(fDefaultLabel, STANDARD_LABEL_WIDTH_SM));
		
		fDefaultValue.setLayoutData(data);
		fDefaultValue.setEnabled(false);

		data = new FlatFormData();
		data.left = new FlatFormAttachment(0, 5);
		data.right = new FlatFormAttachment(fDefaultValue, 0);
		data.top = new FlatFormAttachment(fDefaultValue, 0, SWT.CENTER);
		fDefaultLabel.setLayoutData(data);
		
		fDefaultValue.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				fVarExt.setDefault(fDefaultValue.getText());

				try {
					getBPELEditor().getEditModelClient()
							.getExtensionsResourceInfo().save();
				} catch (IOException exc) {
					// TODO Auto-generated catch block
					exc.printStackTrace();
				}
				
				ProcessManager manager = MonitoringProvider.getInstance().getProcessManager(BPELUtils.getProcess(fVariable));
				if (fDefaultValue.getText().equals("")) {
					manager.getParameterHandler().removeDefaultValue(fVariable);
				} else {
					manager.getParameterHandler().addDefaultValue(fVariable, fDefaultValue.getText());
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
			
		});
		
	}

	@Override
	protected void basicSetInput(EObject newInput) {
		super.basicSetInput(newInput);

		fVariable = getModel();

		fVarExt = (VariableExtension) ModelHelper.getExtension(fVariable);

		if (!fParameter.isDisposed()) {
			fParameter.setSelection(fVarExt.isParameter());
		}
		if (!fDefaultValue.isDisposed()) {
			String defaultVal = fVarExt.getDefault() == null?"":fVarExt.getDefault();
			fDefaultValue.setText(defaultVal);
		}

		setParameterEnabled();
	}

	/**
	 * 
	 */
	private void setParameterEnabled() {
		fParameter.setEnabled(false);
		fDefaultValue.setEnabled(false);
		if (fVariable.getType() != null
				&& (fVariable.getType().getName().equalsIgnoreCase("string")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("boolean")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("dateTime")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("int")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("integer")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("float")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("double")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("short") 
						|| fVariable.getType().getName()
								.equalsIgnoreCase("decimal")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("file")
						|| fVariable.getType().getName()
								.equalsIgnoreCase("rmFile"))) {
			fParameter.setEnabled(true);
			fDefaultValue.setEnabled(fParameter.getSelection());
		}
	}
}
