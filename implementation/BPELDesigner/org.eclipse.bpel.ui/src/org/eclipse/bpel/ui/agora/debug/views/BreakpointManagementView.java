package org.eclipse.bpel.ui.agora.debug.views;

import org.eclipse.bpel.ui.agora.debug.DebugManager;
import org.eclipse.bpel.ui.agora.debug.views.edit.BreakpointTypeEditingSupport;
import org.eclipse.bpel.ui.agora.debug.views.edit.EnabledEditingSupport;
import org.eclipse.bpel.ui.agora.debug.views.edit.NameEditingSupport;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.agora.views.IViewListener;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class BreakpointManagementView extends ViewPart implements IViewListener {

	public static final String ID = "org.eclipse.bpel.ui.agora.debug.views.breakpointManagement";

	/**
	 * Constructor
	 */
	public BreakpointManagementView() {
		super();
	}

	private TableViewer viewer;
	private BreakpointTableSorter tableSorter;
	private BreakpointTableFilter filter;
	
	private ProcessManager manager = null;

	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		Label searchLabel = new Label(parent, SWT.NONE);
		searchLabel.setText("Search: ");
		final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				filter.setSearchText(searchText.getText());
				viewer.refresh();
			}

		});
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(viewer);
		viewer.setContentProvider(new BreakpointContentProvider());
		viewer.setLabelProvider(new BreakpointLabelProvider());

		// Create the context menu
		createContextMenu();

		// Make the selection available
		getSite().setSelectionProvider(viewer);
		// Set the sorter for the table
		tableSorter = new BreakpointTableSorter();
		viewer.setSorter(tableSorter);
		filter = new BreakpointTableFilter();
		viewer.addFilter(filter);

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		MonitoringProvider.getInstance().registerBreakpointView(this);
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {

		String[] titles = { "", "Name", "Target Location", "Target Name",
				"Breakpoint Types", "Breakpoint State" };
		int[] bounds = { 25, 100, 300, 100, 400, 100 };

		// Create the columns
		TableViewerColumn viewerColumn0 = new TableViewerColumn(viewer,
				SWT.CENTER);
		viewerColumn0.setEditingSupport(new EnabledEditingSupport(viewer, this));

		final TableColumn column0 = viewerColumn0.getColumn();

		column0.setText(titles[0]);
		column0.setWidth(bounds[0]);
		column0.setResizable(true);
		column0.setMoveable(true);
		column0.setAlignment(SWT.CENTER);
		// Setting the right sorter
		column0.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumn(0);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column0) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column0);
				viewer.refresh();
			}
		});

		TableViewerColumn viewerColumn1 = new TableViewerColumn(viewer,
				SWT.LEFT);
		viewerColumn1.setEditingSupport(new NameEditingSupport(viewer, this));

		final TableColumn column1 = viewerColumn1.getColumn();

		column1.setText(titles[1]);
		column1.setWidth(bounds[1]);
		column1.setResizable(true);
		column1.setMoveable(true);
		// Setting the right sorter
		column1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumn(1);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column1) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column1);
				viewer.refresh();
			}
		});

		TableViewerColumn viewerColumn2 = new TableViewerColumn(viewer,
				SWT.LEFT);

		final TableColumn column2 = viewerColumn2.getColumn();

		column2.setText(titles[2]);
		column2.setWidth(bounds[2]);
		column2.setResizable(true);
		column2.setMoveable(true);
		// Setting the right sorter
		column2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumn(2);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column2) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column2);
				viewer.refresh();
			}
		});

		TableViewerColumn viewerColumn3 = new TableViewerColumn(viewer,
				SWT.LEFT);

		final TableColumn column3 = viewerColumn3.getColumn();

		column3.setText(titles[3]);
		column3.setWidth(bounds[3]);
		column3.setResizable(true);
		column3.setMoveable(true);
		// Setting the right sorter
		column3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumn(3);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column3) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column3);
				viewer.refresh();
			}
		});

		TableViewerColumn viewerColumn4 = new TableViewerColumn(viewer,
				SWT.LEFT);
		viewerColumn4
				.setEditingSupport(new BreakpointTypeEditingSupport(viewer, this));

		final TableColumn column4 = viewerColumn4.getColumn();

		column4.setText(titles[4]);
		column4.setWidth(bounds[4]);
		column4.setResizable(true);
		column4.setMoveable(true);
		// Setting the right sorter
		column4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumn(4);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column4) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column4);
				viewer.refresh();
			}
		});
		
		TableViewerColumn viewerColumn5 = new TableViewerColumn(viewer,
				SWT.LEFT);

		final TableColumn column5 = viewerColumn5.getColumn();

		column5.setText(titles[5]);
		column5.setWidth(bounds[5]);
		column5.setResizable(true);
		column5.setMoveable(true);
		// Setting the right sorter
		column5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumn(5);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column5) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column5);
				viewer.refresh();
			}
		});

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	private void createContextMenu() {
		// Create menu manager.
		MenuManager menuManager = new MenuManager();
		menuManager
				.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		viewer.getControl().setMenu(
				menuManager.createContextMenu(viewer.getControl()));

		// Register menu for extension.
		getSite().registerContextMenu(menuManager, null);
	}

	public TableViewer getViewer() {
		return this.viewer;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
		
		if (manager != null) {
			manager.getDebugManager().refresh();
		}
	}

	@Override
	public void update() {
		if (!viewer.getTable().getDisplay().isDisposed()) {
			viewer.getTable().getDisplay().asyncExec(new Runnable() {

				public void run() {
					if (!viewer.getControl().isDisposed()) {
						viewer.refresh();
					}
				}
			});
		}
	}

	public ProcessManager getProcessManager() {
		return this.manager;
	}
	
	public void setInputMonitorManager(ProcessManager manager) {
		this.manager = manager;
		
		DebugManager debug = manager.getDebugManager();

		// @hahnml: Activate the listener for the DebugManager and load
		// the breakpoints in the view
		debug.setViewListener(this);
		this.viewer.setInput(debug.getAllBreakpoints());
	}
}
