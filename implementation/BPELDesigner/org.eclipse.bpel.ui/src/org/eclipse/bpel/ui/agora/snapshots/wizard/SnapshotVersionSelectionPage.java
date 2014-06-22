package org.eclipse.bpel.ui.agora.snapshots.wizard;

import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotVersion;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotVersionContentProvider;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotVersionLabelProvider;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotVersionTableSorter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SnapshotVersionSelectionPage extends WizardPage {

	private TableViewer viewer;
	private SnapshotVersionTableSorter tableSorter;
	private ToolBar toolBar = null;
	private Composite composite = null;

	protected SnapshotVersionSelectionPage(String pageName, String title,
			String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
	}

	public void init() {
		if (getSnapshotWizard().snapInfo != null) {
			viewer.setInput(getSnapshotWizard().snapInfo.getSnapshotVersion());
		}
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		composite.setLayout(gridLayout);

		createToolBar();

		createViewer(composite);

		// Set the sorter for the table
		tableSorter = new SnapshotVersionTableSorter();
		viewer.setSorter(tableSorter);

		// Required to avoid an error in the system
		setControl(composite);
		setPageComplete(false);
	}

	/**
	 * This method initializes toolBar
	 * 
	 */
	private void createToolBar() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.RIGHT;
		toolBar = new ToolBar(composite, SWT.HORIZONTAL);
		toolBar.setLayoutData(gridData);

		ToolItem refresh = new ToolItem(toolBar, SWT.PUSH);
		refresh.setImage(BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_REFRESH_16));
		refresh.setToolTipText("Refresh the list of snapshot versions.");
		refresh.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (getSnapshotWizard().snapInfo != null) {
					viewer.setInput(ManagementAPIHandler.getSnapshotVersion(
							Long.parseLong(getSnapshotWizard().snapInfo
									.getIid()),
							getSnapshotWizard().snapInfo.getActivityXPath())
							.getSnapshotVersion());

					viewer.refresh();
				}
			}
		});
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		createColumns(viewer);
		viewer.setContentProvider(new SnapshotVersionContentProvider());
		viewer.setLabelProvider(new SnapshotVersionLabelProvider());
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {
		String[] titles = { "Snapshot ID", "Snapshot version", "Created" };
		int[] bounds = { 150, 150, 150 };

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

					TSnapshotVersion version = (TSnapshotVersion) sel
							.getFirstElement();

					// Update the version at the wizard
					getSnapshotWizard().version = version;

					// Initialize the next pages
					((SnapshotVariableSelectionPage) getNextPage())
							.init(version);
					((SnapshotPartnerLinkSelectionPage) getNextPage()
							.getNextPage()).init(version);

					setPageComplete(true);
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
}
