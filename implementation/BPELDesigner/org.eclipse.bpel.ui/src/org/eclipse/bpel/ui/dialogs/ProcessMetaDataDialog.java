package org.eclipse.bpel.ui.dialogs;

import java.util.HashMap;

import org.eclipse.bpel.apache.ode.deploy.model.dd.TMdProperty;
import org.eclipse.bpel.apache.ode.deploy.model.dd.TMetaData;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ProcessMetaDataDialog {

	public ProcessMetaDataDialog(HashMap<String, TMetaData> metaData) {
		this.metaData = metaData;

		createSShell();
		sShell.open();
	}

	private Shell sShell = null;
	private Button closeButton = null;

	private HashMap<String, TMetaData> metaData = null;

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.END;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.heightHint = 25;
		gridData2.widthHint = 60;
		gridData2.verticalAlignment = GridData.CENTER;

		sShell = new Shell();
		sShell.setText("Process Meta-Data");
		sShell.setImage(BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_NEW_REVISION));
		sShell.setLayout(new GridLayout());
		sShell.setSize(new Point(500, 400));

		// Center the shell on the monitor
		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = sShell.getBounds();

		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		sShell.setLocation(x, y);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		final ScrolledComposite sc1 = new ScrolledComposite(sShell,
				SWT.H_SCROLL | SWT.V_SCROLL);
		final Composite c1 = new Composite(sc1, SWT.NONE);
		sc1.setContent(c1);
		sc1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		c1.setLayout(gridLayout);

		for (String processPath : this.metaData.keySet()) {
			Label processLabel = new Label(c1, SWT.NONE);
			processLabel.setText(processPath);

			GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			gridData.grabExcessHorizontalSpace = true;

			processLabel.setLayoutData(gridData);

			if (this.metaData.get(processPath) != null) {
				TableViewer viewer = createViewer(c1);
				viewer.setInput(this.metaData.get(processPath).getMdProperty());
			}
		}
		
		c1.setSize(c1.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		closeButton = new Button(sShell, SWT.NONE);
		closeButton.setText("Close");
		closeButton.setLayoutData(gridData2);
		closeButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				sShell.close();
			}
		});
		
		sShell.pack();
		sShell.setSize(new Point(500, 400));
	}

	private TableViewer createViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		createColumns(viewer);
		viewer.setContentProvider(new MetaDataContentProvider());
		viewer.setLabelProvider(new MetaDataLabelProvider());

		return viewer;
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {
		String[] titles = { "Name", "Value" };
		int[] bounds = { 150, 300 };

		for (int i = 0; i < titles.length; i++) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(
					viewer, SWT.NONE);
			final TableColumn column = viewerColumn.getColumn();
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
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
	}

	class MetaDataContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			@SuppressWarnings("unchecked")
			EList<TMdProperty> properties = (EList<TMdProperty>) inputElement;
			return properties.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	class MetaDataLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public String getColumnText(Object element, int columnIndex) {
			TMdProperty prop = (TMdProperty) element;

			switch (columnIndex) {
			case 0:
				return prop.getName();
			case 1:
				return prop.getValue();
			default:
				throw new RuntimeException("Should not happen");
			}
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
