package org.eclipse.bpel.ui.agora.views;

import org.eclipse.bpel.ui.agora.manager.MonitorManager;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * Realizes an auditing view to show all propagated events of a running process
 * instance.
 * 
 * @author hahnml
 * 
 */
public class AuditingView extends ViewPart implements IViewListener {

	public static final String ID = "org.eclipse.bpel.ui.simtech.auditingView";

	/**
	 * Constructor
	 */
	public AuditingView() {
		super();
	}

	private TableViewer viewer;
	private EventFilter filter;

	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		Label searchLabel = new Label(parent, SWT.NONE);
		searchLabel.setText("Filter: ");
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
		viewer.setContentProvider(new EventContentProvider());
		viewer.setLabelProvider(new EventLabelProvider());
		// Make the selection available
		getSite().setSelectionProvider(viewer);
		filter = new EventFilter();
		viewer.addFilter(filter);

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		MonitoringProvider.getInstance().registerAuditingView(this);
	}

	public TableViewer getViewer() {
		return viewer;
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {

		String[] titles = { "EventType", "Source", "Element name", "Timestamp", "State" };
		int[] bounds = { 200, 250, 200, 150, 200 };

		for (int i = 0; i < titles.length; i++) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(
					viewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();

			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(true);
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent evt) {
				Table table = (Table) evt.getSource();
				EventMessage message = (EventMessage) viewer.getElementAt(table
						.getSelectionIndex());

				EventDetailViewer.showDialog(message.getMessageObject());
			}

			public void widgetSelected(SelectionEvent evt) {
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void update() {
		if (!viewer.getTable().isDisposed()
				&& !viewer.getTable().getDisplay().isDisposed()) {
			viewer.getTable().getDisplay().asyncExec(new Runnable() {

				public void run() {
					// @hahnml: Check again if the table and the display is not
					// disposed because we asynchronous execute the refresh
					// operation
					if (!viewer.getTable().isDisposed()
							&& !viewer.getTable().getDisplay().isDisposed()) {
						viewer.refresh();
					}
				}
			});
		}
	}

	public void setInputMonitorManager(MonitorManager manager) {
		EventModelProvider eventProvider = manager.getEventModelProvider();

		// @hahnml: Activate the listener for the EventModelProvider and load
		// its event data in the view
		eventProvider.setViewListener(this);
		this.getViewer().setInput(eventProvider.getEvents());
	}
}
