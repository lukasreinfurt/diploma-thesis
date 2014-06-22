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
package org.eclipse.bpel.ui.agora.views;

import org.eclipse.bpel.common.ui.details.IDetailsAreaConstants;
import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
import org.eclipse.bpel.common.ui.flatui.FlatFormData;
import org.eclipse.bpel.common.ui.flatui.FlatFormLayout;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.adapters.AdapterNotification;
import org.eclipse.bpel.ui.agora.manager.VariableManager;
import org.eclipse.bpel.ui.properties.BPELPropertySection;
import org.eclipse.bpel.ui.properties.VariableTypeSelector;
import org.eclipse.bpel.ui.uiextensionmodel.VariableExtension;
import org.eclipse.bpel.ui.util.BatchedMultiObjectAdapter;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.bpel.ui.util.MultiObjectAdapter;
import org.eclipse.core.resources.IMarker;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * VariableValueSection provides viewing and editing of a BPEL variable
 * 
 * @author aeichel
 */
public class VariableValueSection extends BPELPropertySection implements IViewListener {

	public VariableValueSection() {
		super();
		VariableManager.registerAsListener(this);
	}
	
	/**
	 * Make this section use all the vertical space it can get. 
	 * 
	 */
	@Override
	public boolean shouldUseExtraSpace() { 
		return true;
	}
	
	protected VariableTypeSelector variableTypeSelector;
	
	protected Composite parentComposite;
	
	private Text text;
	private Button button;
	private Variable variable;

	protected boolean isMessageTypeAffected(Notification n) {
		return (n.getFeatureID(Variable.class) == BPELPackage.VARIABLE__MESSAGE_TYPE);
	}
	
	protected boolean isTypeAffected(Notification n) {
		return (n.getFeatureID(Variable.class) == BPELPackage.VARIABLE__TYPE);
	}
	
	protected boolean isElementAffected(Notification n) {
		return (n.getFeatureID(Variable.class) == BPELPackage.VARIABLE__ELEMENT);
	}


	@Override
	protected MultiObjectAdapter[] createAdapters() {
		return new MultiObjectAdapter[] {
			/* model object */
			new BatchedMultiObjectAdapter() {
				
				boolean update = false;
				
				@Override
				public void notify (Notification n) {
					if (update) {
						return ;
					}
					
					int eventGroup = n.getEventType() / 100; 
					if (eventGroup == AdapterNotification.NOTIFICATION_MARKERS_CHANGED_GROUP) {
						update = true;
						return;
					}

					if (isMessageTypeAffected(n)) {
						update = true;
						return;
					}
					if (isTypeAffected(n)) {
						update = true;
						return ;
					}
					if (isElementAffected(n)) {
						update = true;
						return;
					}
					if (n.getNotifier() instanceof VariableExtension) {
						update = true;
						return ;
					}
				}
				
				@Override
				public void finish() {
					if (update) {
						updateVariableTypeSelector();
					}
					update = false;
				}
			}
		};
	}

	
	@Override
	protected void addAllAdapters() {
		super.addAllAdapters();
		VariableExtension varExt = (VariableExtension)ModelHelper.getExtension(getInput());
		if (varExt != null) fAdapters[0].addToObject(varExt);
	}	
	
	@Override
	protected void createClient(Composite parent) {
		 parentComposite = createFlatFormComposite(parent);
		 Composite composite = fWidgetFactory.createComposite(parentComposite);
		composite.setLayout(new FillLayout(SWT.VERTICAL));
		
		FlatFormLayout formLayout = new FlatFormLayout();
		
		formLayout.marginWidth = formLayout.marginHeight = 0;
		composite.setLayout(formLayout);
		
		FlatFormData data;
		
		data = new FlatFormData();
		data.top = new FlatFormAttachment(0, IDetailsAreaConstants.VMARGIN);
		data.left = new FlatFormAttachment(0, 0);
		data.right = new FlatFormAttachment(100, 0);
		data.bottom = new FlatFormAttachment(100, 0);
		composite.setLayoutData(data);
		
		this.button = fWidgetFactory.createButton(composite, "Save Variable Value", SWT.PUSH);
		this.text = fWidgetFactory.createText(composite, "", SWT.MULTI|SWT.V_SCROLL);
		this.button.addMouseListener(new MouseListener(){

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// nothing to do
				
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				VariableManager.writeVariable(variable.getName(), variable.getXPath(), text.getText(), variable.getScopeID());
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// nothing to do
				
			}
			
		});
		
		data = new FlatFormData();
		data.top = new FlatFormAttachment(0,30);
		data.left = new FlatFormAttachment(0,0);
		data.right = new FlatFormAttachment(100,0);
		data.bottom = new FlatFormAttachment(100,0);
		
		this.text.setLayoutData(data);
	}
	
	/**
	 * 
	 */
	public void updateVariableTypeSelector() {
		variableTypeSelector.setVariable((Variable)getInput());
		updateMarkers();
	}
	
	
	@Override
	protected void basicSetInput(EObject newInput) {
		super.basicSetInput(newInput);
		this.variable = (Variable)newInput;
		String variableValue = ((Variable)newInput).getValue();
		if (variableValue != null){
			this.text.setText(variableValue);
		} else {
			this.text.setText("");
		}
	}


	/**
	 * @see org.eclipse.bpel.ui.properties.BPELPropertySection#getUserContext()
	 */
	@Override
	public Object getUserContext() {
		return variableTypeSelector.getUserContext();
	}
	
	
	/**
	 * @see org.eclipse.bpel.ui.properties.BPELPropertySection#restoreUserContext(java.lang.Object)
	 */
	
	@Override
	public void restoreUserContext(Object userContext) {
		variableTypeSelector.restoreUserContext(userContext);
	}
	
		
	/**
	 * 
	 * @see org.eclipse.bpel.ui.properties.BPELPropertySection#isValidMarker(org.eclipse.core.resources.IMarker)
	 */
	
	@SuppressWarnings("nls")
	@Override
	public boolean isValidMarker (IMarker marker) {

		String context = null;
		try {
			context = (String) marker.getAttribute("href.context");
		} catch (Exception ex) {
			return false;
		}
		
		return "name".equals (context) == false; 
	}	
	
	

	@Override
	protected void updateMarkers () {				
//		variableTypeSelector.dataTypeLabel.clear();		
//		for(IMarker m : getMarkers(getInput())) {
//			variableTypeSelector.dataTypeLabel.addStatus(BPELUtil.adapt(m, IStatus.class));
//		}		
	}
	
	// @vonstepk update method which rereads the variable value and updates
	// the textbox with the new value. Should be called whenever the variable's
	// value changed in order to make the view reflect this change.
	@Override
	public void update() {
		if (!text.isDisposed()) {
			text.getDisplay().asyncExec(new Runnable() {
				
				public void run() {
					
					if (!text.isDisposed()) {
						String variableValue = variable.getValue();
						if (variableValue != null){
							text.setText(variableValue);
						} else {
							text.setText("");
						}
						text.redraw();
					}
				}
			});			
		}
	}	
}
