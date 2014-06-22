package org.eclipse.bpel.ui.agora.snapshots.wizard;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotVersion;
import org.eclipse.bpel.ui.agora.ode135.client.TVariableInfo;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotVariableContentProvider;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotVariableEditingSupport;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotVariableLabelProvider;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.VariableInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.w3c.dom.Node;

public class SnapshotVariableSelectionPage extends WizardPage {

	private TableViewer viewer;
	private ToolBar toolBar = null;
	private Composite composite = null;
	private Text variableText = null;

	private List<VariableInfo> infoList = new ArrayList<VariableInfo>();

	protected SnapshotVariableSelectionPage(String pageName, String title,
			String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
	}

	public void init(TSnapshotVersion version) {
		if (version != null) {
			wrapInVariableInfo(ManagementAPIHandler.getSnapshotVariables(
					getSnapshotWizard().instanceID,
					Long.parseLong(version.getSnapshotId())));
			viewer.setInput(this.infoList);
		}
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);

		createToolBar();

		createViewer(composite);
		createVariableTextArea(composite);

		// Required to avoid an error in the system
		setControl(composite);
		setPageComplete(true);
	}

	private void createVariableTextArea(Composite parent) {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.minimumWidth = 400;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;

		variableText = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		variableText.setLayoutData(gridData);
	}

	/**
	 * This method initializes toolBar
	 * 
	 */
	private void createToolBar() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.horizontalSpan = 2;
		toolBar = new ToolBar(composite, SWT.HORIZONTAL);
		toolBar.setLayoutData(gridData);

		ToolItem refresh = new ToolItem(toolBar, SWT.PUSH);
		refresh.setImage(BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_REFRESH_16));
		refresh.setToolTipText("Refresh the list of snapshot variables.");
		refresh.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (getSnapshotWizard().version != null) {
					wrapInVariableInfo(ManagementAPIHandler
							.getSnapshotVariables(
									getSnapshotWizard().instanceID,
									Long.parseLong(getSnapshotWizard().version
											.getSnapshotId())));

					viewer.refresh();
				}
			}
		});
	}

	private void wrapInVariableInfo(List<TVariableInfo> snapshotVariables) {
		// Buffer the selection values
		HashMap<TVariableInfo, Boolean> variableMap = new HashMap<TVariableInfo, Boolean>();
		for (VariableInfo vInf : this.infoList) {
			variableMap.put(vInf.getInfo(), vInf.isSelected());
		}

		// Clear the original list
		this.infoList.clear();

		// Add all queried variable infos to the list and restore the buffered
		// selection values.
		for (TVariableInfo var : snapshotVariables) {
			Boolean selected = null;
			if (!variableMap.isEmpty()) {
				selected = variableMap.get(var);
			}

			// Default value is "true" (reload all variables)
			VariableInfo inf = new VariableInfo(selected != null ? selected
					: true, var);
			this.infoList.add(inf);
		}
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		createColumns(viewer);
		viewer.setContentProvider(new SnapshotVariableContentProvider());
		viewer.setLabelProvider(new SnapshotVariableLabelProvider());
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {
		String[] titles = { "Reload", "Variable name", "Scope Id" };
		int[] bounds = { 150, 150, 150 };

		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column = viewerColumn.getColumn();
		column.setText(titles[0]);
		column.setWidth(bounds[0]);
		column.setResizable(true);
		column.setMoveable(false);
		viewerColumn.setEditingSupport(new SnapshotVariableEditingSupport(
				viewer));

		TableViewerColumn viewerColumn1 = new TableViewerColumn(viewer,
				SWT.NONE);
		TableColumn column1 = viewerColumn1.getColumn();
		column1.setText(titles[1]);
		column1.setWidth(bounds[1]);
		column1.setResizable(true);
		column1.setMoveable(false);

		TableViewerColumn viewerColumn2 = new TableViewerColumn(viewer,
				SWT.NONE);
		TableColumn column2 = viewerColumn2.getColumn();
		column2.setText(titles[2]);
		column2.setWidth(bounds[2]);
		column2.setResizable(true);
		column2.setMoveable(false);

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.minimumWidth = 400;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(gridData);

		table.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = viewer.getSelection();
				if (selection != null && !selection.isEmpty()
						&& selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					VariableInfo info = (VariableInfo) sel.getFirstElement();

					String text = "";
					TVariableInfo.Value value = info.getInfo().getValue();
					if (value != null && !value.getAny().isEmpty()) {
						Node node = (Node) value.getAny().get(0);
						text = prettyFormat(node, 4);
					}

					variableText.setText(text);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

	private SnapshotWizard getSnapshotWizard() {
		return (SnapshotWizard) getWizard();
	}

	private String prettyFormat(Node input, int indent) {
		String val = "";
		String value = "";
		val = convertToString(input, indent);
		int index = val.indexOf(">");
		value = val.substring(index + 1);

		return value;
	}

	private String convertToString(Node d, int indent) {
		Source source = new DOMSource(d);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		Result result = new StreamResult(outStream);
		Transformer xformer;
		String tmp = "";
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount",
					String.valueOf(indent));
			xformer.transform(source, result);
			tmp = outStream.toString();
			return tmp;
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return tmp;
	}

	public List<VariableInfo> getInfoList() {
		return infoList;
	}
}
