package org.eclipse.bpel.ui.agora.views;

import org.eclipse.bpel.common.ui.details.IDetailsAreaConstants;
import org.eclipse.bpel.common.ui.flatui.FlatFormAttachment;
import org.eclipse.bpel.common.ui.flatui.FlatFormData;
import org.eclipse.bpel.common.ui.flatui.FlatFormLayout;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.ui.agora.manager.PartnerLinkManager;
import org.eclipse.bpel.ui.agora.manager.VariableManager;
import org.eclipse.bpel.ui.properties.BPELPropertySection;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class PartnerLinkValueSection extends BPELPropertySection implements IViewListener {

	public PartnerLinkValueSection() {
		super();
		PartnerLinkManager.registerAsListener(this);
	}
	
	/**
	 * Make this section use all the vertical space it can get.
	 * 
	 */
	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	protected Composite parentComposite;

	private PartnerLink partnerLink;

	private Button button;

	private Text text;

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

		this.button = fWidgetFactory.createButton(composite,
				"Save Partner Link Value", SWT.PUSH);
		this.text = fWidgetFactory.createText(composite, "", SWT.MULTI
				| SWT.V_SCROLL);
		this.button.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// nothing to do

			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				PartnerLinkManager.writePartnerLink(partnerLink.getName(),
						partnerLink.getXPath(), text.getText(),
						partnerLink.getScopeID());
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				// nothing to do

			}

		});

		data = new FlatFormData();
		data.top = new FlatFormAttachment(0, 30);
		data.left = new FlatFormAttachment(0, 0);
		data.right = new FlatFormAttachment(100, 0);
		data.bottom = new FlatFormAttachment(100, 0);

		this.text.setLayoutData(data);

	}

	@Override
	protected void basicSetInput(EObject newInput) {
		super.basicSetInput(newInput);
		this.partnerLink = (PartnerLink) newInput;
		String partnerLinkValue = ((PartnerLink) newInput).getValue();
		if (partnerLinkValue != null) {
			this.text.setText(partnerLinkValue);
		} else {
			this.text.setText("");
		}
	}
	
	// @vonstepk update method which rereads the partnerLink value and updates
	// the textbox with the new value. Should be called whenever the partner links'
	// value changed in order to make the view reflect this change.
	@Override
	public void update() {
		if (!text.isDisposed()) {
			text.getDisplay().asyncExec(new Runnable() {
				
				public void run() {
					
					if (!text.isDisposed()) {
						String partnerLinkValue = partnerLink.getValue();
						if (partnerLinkValue != null){
							text.setText(partnerLinkValue);
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
