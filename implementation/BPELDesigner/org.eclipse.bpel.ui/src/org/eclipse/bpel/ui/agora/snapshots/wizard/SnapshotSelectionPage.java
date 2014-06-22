package org.eclipse.bpel.ui.agora.snapshots.wizard;

import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.agora.communication.ManagementAPIHandler;
import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotInfo;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotContentProvider;
import org.eclipse.bpel.ui.agora.snapshots.wizard.table.SnapshotLabelProvider;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SnapshotSelectionPage extends WizardPage {

	private TableViewer viewer;
	private ToolBar toolBar = null;
	private Composite composite = null;

	private boolean enableNext = true;

	protected SnapshotSelectionPage(String pageName, String title,
			String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		composite.setLayout(gridLayout);

		createToolBar();

		final Button loadSnapshot = new Button(composite, SWT.CHECK);
		loadSnapshot.setSelection(true);
		loadSnapshot.setText("Disable snapshot selection");

		loadSnapshot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (loadSnapshot.getSelection()) {
					loadSnapshot.setText("Disable snapshot selection");
					viewer.getTable().setEnabled(true);
					enableNext = true;
					setPageComplete(false);
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				} else {
					loadSnapshot.setText("Enable snapshot selection");
					viewer.getTable().setEnabled(false);
					enableNext = false;
					setPageComplete(true);
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				}
			}
		});

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.LEFT;
		loadSnapshot.setLayoutData(gridData);

		createViewer(composite);
		viewer.setInput(getSnapshotWizard().snapInfos);

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
		refresh.setToolTipText("Refresh the list of snapshots.");
		refresh.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (getSnapshotWizard().snapInfo != null) {
					viewer.setInput(ManagementAPIHandler.getSnapshots(
							getSnapshotWizard().instanceID,
							getSnapshotWizard().activity.getXPath())
							.getSnapshotInfo());

					viewer.refresh();
				}
			}
		});
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		createColumns(viewer);
		viewer.setContentProvider(new SnapshotContentProvider());
		viewer.setLabelProvider(new SnapshotLabelProvider());
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {
		String[] titles = { "Activity XPath", "Number of versions" };
		int[] bounds = { 300, 150 };

		for (int i = 0; i < titles.length; i++) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(
					viewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(true);
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

					TSnapshotInfo info = (TSnapshotInfo) sel.getFirstElement();

					// Update the snapshot at the wizard
					getSnapshotWizard().snapInfo = info;

					// Initialize the next page
					((SnapshotVersionSelectionPage) getNextPage()).init();

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

	public boolean isLoadSnapshot() {
		return enableNext;
	}

	@Override
	public boolean canFlipToNextPage() {
		return enableNext;
	}
}
