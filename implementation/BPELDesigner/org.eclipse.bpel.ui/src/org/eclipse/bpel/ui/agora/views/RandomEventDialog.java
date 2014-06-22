package org.eclipse.bpel.ui.agora.views;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.ActivityEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Activity_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Activity_Join_Failure;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.CorrelationSet_Modification;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Evaluating_TransitionCondition_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.InstanceEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Instance_Faulted;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.LinkEventMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.PartnerLink_Modification;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Scope_Handling_Fault;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Variable_Modification;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Realizes a dialog which could be opened from a table row to display detailed
 * information of an event.
 * 
 * @author hahnml
 * 
 */
public class RandomEventDialog extends Dialog {

	Object result;

	InstanceEventMessage message = null;

	HashMap<String, String> data = new HashMap<String, String>();

	Shell sShell = null;

	public RandomEventDialog(Shell parent, int style,
			InstanceEventMessage messageObject) {
		super(parent, style);
		this.message = messageObject;
	}

	public RandomEventDialog(Shell parent, InstanceEventMessage messageObject) {
		this(parent, 0, messageObject);
	}

	public Object open() {
		setText(message != null ? message.getClass().getName().substring(
				message.getClass().getName().lastIndexOf(".") + 1)
				: "Event Details");

		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText(getText());
		shell.setImage(BPELUIPlugin.INSTANCE.getImage(IBPELUIConstants.ICON_PROPERTY_16));

		// Center the shell on the monitor
		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
		shell.setSize(bounds.width / 2 + 150, bounds.height / 2);

		createContents(shell);

		shell.layout(true);

		shell.open();

		sShell = shell;

		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	private void createContents(Shell parent) {
		// Get the data of the messageObject
		readData();

		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		String[] titles = { "Attribute", "Value" };
		int[] bounds = { 200, 650 };

		for (int i = 0; i < titles.length; i++) {
			final TableColumn column = new TableColumn(table, SWT.LEFT);

			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(true);

		}

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		for (String key : data.keySet()) {
			TableItem item = new TableItem(table, SWT.NONE);
			//Change the font to bold for the first column
			item.setFont(0, new Font(Display.getDefault(), new FontData("Arial", 11, SWT.BOLD))); 
			item.setText(0, key);
			String value = data.get(key);
			if (value.contains("<")) {
				String format = prettyFormat(value, 2);
				String[] lines = format.split("\n");
				
				for (int i = 0; i<lines.length; i++) {
					if (i == 0){
						item.setText(1, lines[0]);
					} else {
						TableItem innerItem = new TableItem(table, SWT.NONE);
						innerItem.setText(1, lines[i]);
					}
				}
			} else {
				item.setText(1, value);
			}
		}

		GridData layData = new GridData();
		layData.grabExcessHorizontalSpace = true;
		layData.grabExcessVerticalSpace = true;
		layData.horizontalAlignment = SWT.FILL;
		layData.verticalAlignment = SWT.FILL;

		table.setLayoutData(layData);

		GridData closeData = new GridData();
		closeData.horizontalAlignment = SWT.RIGHT;

		Button close = new Button(parent, SWT.PUSH);
		close.setText("Close");
		close.setLayoutData(closeData);

		close.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				sShell.close();
			}
		});
	}

	/**
	 * Fills the dialog with the detailed data of an event.
	 */
	private void readData() {
		data.clear();

		if (this.message instanceof Variable_Modification) {
			Variable_Modification var = (Variable_Modification) this.message;

			data.put("variableName", checkNull(var.getVariableName()));
			data.put("variableXPath", checkNull(var.getVariableXPath()));
			data.put("value", checkNull(var.getValue()));
			data.put("changedFromOutside", checkNull(var
					.getChanged_from_outside()));
			data.put("activityXPath", checkNull(var.getActivityXPath()));
			data.put("scopeID", checkNull(var.getScopeID()));
			data.put("scopeXPath", checkNull(var.getScopeXPath()));

		//@author: sonntamo
		} else if (this.message instanceof PartnerLink_Modification) {
			PartnerLink_Modification pl = (PartnerLink_Modification) this.message;

			data.put("partnerLinkName", checkNull(pl.getPlName()));
			data.put("partnerLinkXPath", checkNull(pl.getPlXPath()));
			data.put("value", checkNull(pl.getPlValue()));
			data.put("changedFromOutside", checkNull(pl
					.getChanged_from_outside()));
			data.put("activityXPath", checkNull(pl.getActivityXPath()));
			data.put("scopeID", checkNull(pl.getScopeID()));
			data.put("scopeXPath", checkNull(pl.getScopeXPath()));

			//@author: sonntamo
		} else if (this.message instanceof CorrelationSet_Modification) {
			CorrelationSet_Modification cs = (CorrelationSet_Modification) this.message;

//			data.put("correlationSetName", checkNull(cs.getpl.getPlName()));
			data.put("correlationSetXPath", checkNull(cs.getCSet_xpath()));
			data.put("changedFromOutside", checkNull(cs.getChanged_from_outside()));
			data.put("activityXPath", checkNull(cs.getActivityXPath()));
			data.put("scopeID", checkNull(cs.getScopeID()));
			int i = 0;
			for (String val: cs.getValues()) {
				i++;
				data.put("value " + 1, checkNull(val));
			}
			
		} else if (this.message instanceof Instance_Faulted) {
			Instance_Faulted fault = (Instance_Faulted) this.message;

			data.put("faultName", checkNull(fault.getFaultName()));
			data.put("faultMessage", checkNull(fault.getFaultMsg()));
			data.put("messageType", checkNull(fault.getMessageType()));
			data.put("elementType", checkNull(fault.getElementType()));

		} else if (this.message instanceof ActivityEventMessage) {
			ActivityEventMessage act = (ActivityEventMessage) this.message;

			data.put("activityID", checkNull(act.getActivityID()));
			data.put("activityName", checkNull(act.getActivityName()));
			data.put("activityXPath", checkNull(act.getActivityXPath()));
			data.put("scopeID", checkNull(act.getScopeID()));
			data.put("scopeXPath", checkNull(act.getScopeXPath()));

			if (this.message instanceof Activity_Faulted) {
				Activity_Faulted fault = (Activity_Faulted) this.message;

				data.put("faultName", checkNull(fault.getFaultName()));
				data.put("faultMessage", checkNull(fault.getFaultMsg()));
				data.put("explanation", checkNull(fault.getExplanation()));
				data.put("messageType", checkNull(fault.getMessageType()));
				data.put("elementType", checkNull(fault.getElementType()));

			} else if (this.message instanceof Scope_Handling_Fault) {
				Scope_Handling_Fault fault = (Scope_Handling_Fault) this.message;

				data.put("faultName", checkNull(fault.getFaultName()));
				data.put("faultMessage", checkNull(fault.getFaultMsg()));
				data.put("messageType", checkNull(fault.getMessageType()));
				data.put("elementType", checkNull(fault.getElementType()));

			} else if (this.message instanceof Activity_Join_Failure) {
				Activity_Join_Failure fault = (Activity_Join_Failure) this.message;

				data.put("faultName", checkNull(fault.getFaultName()));
				data.put("faultMessage", checkNull(fault.getFaultMsg()));
				data.put("messageType", checkNull(fault.getMessageType()));
				data.put("elementType", checkNull(fault.getElementType()));
				data.put("suppressJoinFailure", checkNull(fault
						.getSuppressJoinFailure()));

			} else if (this.message instanceof Evaluating_TransitionCondition_Faulted) {
				Evaluating_TransitionCondition_Faulted fault = (Evaluating_TransitionCondition_Faulted) this.message;

				data.put("faultName", checkNull(fault.getFaultName()));
				data.put("faultMessage", checkNull(fault.getFaultMsg()));
				data.put("messageType", checkNull(fault.getMessageType()));
				data.put("elementType", checkNull(fault.getElementType()));
				data.put("linkXPath", checkNull(fault.getLinkXPath()));
			}
		} else if (this.message instanceof LinkEventMessage) {
			LinkEventMessage link = (LinkEventMessage) this.message;
			
			data.put("linkName", checkNull(link.getLinkName()));
			data.put("linkXPath", checkNull(link.getLinkXPath()));
			data.put("scopeID", checkNull(link.getScopeID()));
			data.put("scopeXPath", checkNull(link.getScopeXPath()));
		}
	}

	private String checkNull(Object input) {
		return input != null ? input.toString() : "";
	}

	private String prettyFormat(String input, int indent) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        Transformer transformer = TransformerFactory.newInstance().newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}

}
