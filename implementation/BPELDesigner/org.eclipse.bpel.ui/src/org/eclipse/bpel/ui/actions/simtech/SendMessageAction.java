package org.eclipse.bpel.ui.actions.simtech;

import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Types;

import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.util.XSD2XMLGenerator;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.wsdl.XSDSchemaExtensibilityElement;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDParticleContent;
import org.eclipse.xsd.XSDTypeDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Action to send a message to the selected activity of the process instance
 * 
 * @author sonntamo
 */
public class SendMessageAction implements IObjectActionDelegate {

	private Receive activity;

	@Override
	public void run(IAction action) {
		if (this.activity != null) {
			MessageDialog dialog = new MessageDialog(Display.getDefault()
					.getActiveShell());
			dialog.open();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		activity = null;
		if (selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof Receive) {
				activity = (Receive) ((StructuredSelection) selection)
						.getFirstElement();

			}
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public class MessageDialog extends Dialog {

		Shell sShell = null;
		Text message = null;
		Definition wsdl = null;
		String operation = "";

		public MessageDialog(Shell parent, int style) {
			super(parent, style);
		}

		public MessageDialog(Shell parent) {
			super(parent, 0);
		}

		public void open() {
			if (activity == null)
				return;

			setText("Send message to " + activity.getName());

			Shell parent = getParent();
			Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 2;
			shell.setLayout(gridLayout);
			shell.setText(getText());
			shell.setImage(BPELUIPlugin.INSTANCE
					.getImage(IBPELUIConstants.ICON_MESSAGE_16));

			// Center the shell on the monitor
			Monitor primary = Display.getCurrent().getPrimaryMonitor();
			Rectangle bounds = primary.getBounds();
			Rectangle rect = shell.getBounds();

			int x = bounds.x + (bounds.width - rect.width) / 2;
			int y = bounds.y + (bounds.height - rect.height) / 2;

			shell.setLocation(x, y);
			shell.setSize(bounds.width / 2 + 150, bounds.height / 2);

			operation = activity.getOperation().getName();
			wsdl = activity.getOperation().getEnclosingDefinition();
			createContents(shell);
			createDefaultMsg();

			shell.layout(true);

			shell.open();

			sShell = shell;

			Display display = parent.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		}

		private boolean isTwoWay() {
			return activity.getOperation().getOutput() != null;
		}

		private void createDefaultMsg() {
			String literal = "";
			try {
				literal = initTargetVariable();
			} catch (Exception e) {
			}
			this.message.setText(literal);
		}

		private void createContents(Shell parent) {

			GridData layData = new GridData();
			layData.grabExcessHorizontalSpace = true;
			layData.grabExcessVerticalSpace = true;
			layData.horizontalAlignment = SWT.FILL;
			layData.verticalAlignment = SWT.FILL;
			layData.horizontalSpan = 2;

			Label label = new Label(parent, SWT.NONE);
			label.setText("Message:");

			message = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL
					| SWT.V_SCROLL);
			message.setLayoutData(layData);

			GridData buttonData = new GridData();
			buttonData.horizontalAlignment = SWT.RIGHT;
			buttonData.horizontalSpan = 2;

			Button send = new Button(parent, SWT.PUSH);
			send.setText("Send");
			send.setLayoutData(buttonData);

			send.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					ManagementAPIHandler.invokeWS(wsdl, message.getText(),
							operation, activity.getName(), isTwoWay());
					sShell.close();
				}
			});

			Button cancel = new Button(parent, SWT.PUSH);
			cancel.setText("Cancel");
			cancel.setLayoutData(buttonData);

			cancel.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					sShell.close();
				}
			});
		}

		private String initTargetVariable() {

			String rootElement = null;
			String uriWSDL = wsdl.getDocumentBaseURI();

			// Variable is defined using "messageType"
			Message msg = activity.getVariable().getMessageType();

			// we assume there is only one part
			Part part = (Part) msg.getParts().values().iterator().next();

			// only element typed part is checked
			if (part.getElementName() != null) {
				rootElement = part.getElementName().getLocalPart();

				// use the new and improved XSD -> XML generator
				// this was an internal class that was moved to
				XSD2XMLGenerator generator = new XSD2XMLGenerator(uriWSDL,
						rootElement);

				// now generate the message
				try {
					return generator.createXML();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			String xml = "";
			loadXSDFromWSDL(wsdl);

			@SuppressWarnings("rawtypes")
			Map parts = msg.getParts();
			for (Object obj : parts.values()) {
				if (obj instanceof Part) {
					Part partObj = (Part) obj;
					if (partObj.getTypeName() != null) {
						xml += "<" + partObj.getTypeName().getLocalPart();
						xml += " " + "xmlns=\""
								+ partObj.getTypeName().getNamespaceURI()
								+ "\">";
						xml += "</" + partObj.getName() + ">";
					} else {
						xml += instantiateElement(wsdl, partObj
								.getElementName().getLocalPart(), partObj
								.getElementName().getNamespaceURI(), 0, null);
					}
				}
			}
			return xml;
		}

		private void loadXSDFromWSDL(Definition wsdl) {
			Types types = wsdl.getTypes();
			Object schema = types.getExtensibilityElements().iterator().next();
			if (schema instanceof XSDSchemaExtensibilityElement) {
				XSDSchemaExtensibilityElement xsd = (XSDSchemaExtensibilityElement) schema;
				elementList = xsd.getSchema().getElementDeclarations();
				typeList = xsd.getSchema().getTypeDefinitions();
			}
		}

		EList<XSDElementDeclaration> elementList;
		EList<XSDTypeDefinition> typeList;

		private XSDElementDeclaration getXsdElement(String elementName,
				String elementNamespace) {
			Iterator<XSDElementDeclaration> it = elementList.iterator();
			while (it.hasNext()) {
				XSDElementDeclaration elem = it.next();
				if (elem.hasNameAndTargetNamespace(elementName,
						elementNamespace)) {
					return elem;
				}
			}
			return null;
		}

		private String instantiateElement(Definition wsdl, String elementName,
				String elementNamespace, int level, XSDElementDeclaration elem) {

			String tab = "";
			for (int i = 0; i < level; i++) {
				tab += "\t";
			}
			String result = tab;
			result += "<" + elementName + " " + "xmlns=\"" + elementNamespace
					+ "\">\n";

			if (elem == null) {
				elem = getXsdElement(elementName, elementNamespace);
			}
			boolean closingTab = true;
			if (elem.getType().getComplexType() != null) {

				XSDParticleContent cont = elem.getType().getComplexType()
						.getContent();
				if (cont instanceof XSDModelGroup) {
					XSDModelGroup contModel = (XSDModelGroup) cont;
					if ("sequence".equals(contModel.getElement().getNodeName())
							|| "all".equals(contModel.getElement()
									.getNodeName())) {
						Iterator<XSDParticle> it = contModel.getContents()
								.iterator();
						while (it.hasNext()) {
							XSDParticle xsdElemDec = it.next();
							XSDParticleContent xsdElem = xsdElemDec
									.getContent();
							if (xsdElem instanceof XSDElementDeclaration) {
								XSDElementDeclaration xsdElemD = (XSDElementDeclaration) xsdElem;
								result += instantiateElement(wsdl,
										xsdElemD.getName(),
										xsdElemD.getTargetNamespace(),
										level + 1, xsdElemD);
							}
						}
					}
				}
			} else if (elem.getType().getSimpleType() != null) {
				closingTab = false;
				XSDTypeDefinition xsdType = elem.getTypeDefinition();
				if (xsdType instanceof XSDComplexTypeDefinition) {
					XSDComplexTypeDefinition cType = (XSDComplexTypeDefinition) xsdType;
					Element xmlElem = cType.getContent().getElement();
					NodeList list = xmlElem.getChildNodes();
					String type = "content";
					for (int i = 0; i < list.getLength(); i++) {
						Node node = list.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							if ("extension".equals(node.getNodeName())) {
								Node nameAttr = node.getAttributes()
										.getNamedItem("base");
								type = nameAttr.getTextContent() + "_extension";

								NodeList children = node.getChildNodes();
								for (int j = 0; j < children.getLength(); j++) {
									Node child = children.item(j);
									if (child.getNodeType() == Node.ELEMENT_NODE) {
										if ("attribute".equals(child
												.getNodeName())) {
											String attrName = child
													.getAttributes()
													.getNamedItem("name").getNodeValue();
											String attrType = child
													.getAttributes()
													.getNamedItem("type")
													.getNodeValue();
											result = result.substring(0,
													result.length() - 2);

											result += " xmlns:ns1=\"" + elementNamespace
													+ "\" ns1:" + attrName
													+ "=\"" + attrType + "\">";
										}
									}
								}
							}
						}
					}
					result += type;
				} else {
					result = result.substring(0, result.length() - 1);
					result += "content";
				}
			}
			if (closingTab)
				result += tab;
			result += "</" + elementName + ">\n";
			return result;
		}
	}
}
