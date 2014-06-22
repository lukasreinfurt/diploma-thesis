package org.eclipse.bpel.ui.agora.instances;

import javax.xml.ws.WebServiceException;

import org.eclipse.apache.ode.processmanagement.IProcessManagementConstants;
import org.eclipse.apache.ode.processmanagement.ProcessManagementUI;
import org.eclipse.apache.ode.processmanagement.view.ProcessModelProvider;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * Realizes a dialog to display all available instances of the selected process
 * model and to open one of them.
 * 
 * @author hahnml
 * 
 */
public class InstanceSelectionDialog {

	private Shell sShell = null;
	private ToolBar toolBar = null;

	private TableViewer viewer;
	private TableSorter tableSorter;

	private Button cancelButton = null;
	private Button okayButton = null;

	private String processPath = null;

	public InstanceSelectionDialog(String processPath) {
		this.processPath = processPath;

		createSShell();
		sShell.open();
		viewer.refresh();
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.END;
		gridData3.heightHint = 25;
		gridData3.widthHint = 60;
		gridData3.verticalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.END;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.heightHint = 25;
		gridData2.widthHint = 60;
		gridData2.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		sShell = new Shell();
		sShell.setText("Select an instance");
		sShell.setImage(BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_PROPERTY_16));
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(500, 400));

		// Center the shell on the monitor
		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = sShell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		sShell.setLocation(x, y);

		createToolBar();

		createViewer(sShell);
		viewer.setInput(ModelProvider.getInstance().getInstances().values());
		// Set the sorter for the table
		tableSorter = new TableSorter();
		viewer.setSorter(tableSorter);

		okayButton = new Button(sShell, SWT.NONE);
		okayButton.setText("OK");
		okayButton.setLayoutData(gridData2);
		okayButton.setEnabled(false);
		okayButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// Get selection, open the BPEL Editor with the correct
				// file and request the instance history over the queue
				TableItem item = viewer.getTable().getSelection()[0];

				InstanceInformation instanceInformation = ModelProvider
						.getInstance().getInstances()
						.get(Long.valueOf(item.getText(0)));

				// Get the package name from ODE and set it to the
				// instanceInformation object
				try {
					String packageName = ProcessModelProvider.getInstance()
							.getPackageName(
									instanceInformation.getProcessName(),
									instanceInformation.getProcessVersion());
					instanceInformation.setPackageName(packageName);

					InstanceHelper.openInstance(processPath,
							instanceInformation, null);
				} catch (WebServiceException e) {
					MessageDialog
							.openError(
									Display.getCurrent().getActiveShell(),
									"No connection to Apache ODE could established",
									"Please make sure that an Apache ODE instance is running at the specified URL ("
											+ ProcessManagementUI
													.getDefault()
													.getPreferenceStore()
													.getString(
															IProcessManagementConstants.PREF_ODE_URL)
											+ ").");
				}

				sShell.close();
			}
		});

		cancelButton = new Button(sShell, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(gridData3);
		cancelButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				sShell.close();
			}
		});
	}

	/**
	 * This method initializes toolBar
	 * 
	 */
	private void createToolBar() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.RIGHT;
		toolBar = new ToolBar(sShell, SWT.HORIZONTAL);
		toolBar.setLayoutData(gridData);

		ToolItem refresh = new ToolItem(toolBar, SWT.PUSH);
		refresh.setImage(BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_REFRESH_16));
		refresh.setToolTipText("Refresh the list of instances.");
		refresh.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// Synchronize the table with the ModelProvider

				viewer.refresh();
			}
		});
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		createColumns(viewer);
		viewer.setContentProvider(new InstanceContentProvider());
		viewer.setLabelProvider(new InstanceLabelProvider());
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {
		String[] titles = { "Instance ID", "Process name", "Process version",
				"State", "Timestamp" };
		int[] bounds = { 100, 120, 40, 120, 130 };

		for (int i = 0; i < titles.length; i++) {
			final int index = i;
			final TableViewerColumn viewerColumn = new TableViewerColumn(
					viewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(true);
			// Setting the right sorter
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableSorter.setColumn(index);
					int dir = viewer.getTable().getSortDirection();
					if (viewer.getTable().getSortColumn() == column) {
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					} else {

						dir = SWT.DOWN;
					}
					viewer.getTable().setSortDirection(dir);
					viewer.getTable().setSortColumn(column);
					viewer.refresh();
				}
			});
		}
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(gridData);

		table.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				okayButton.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

}
