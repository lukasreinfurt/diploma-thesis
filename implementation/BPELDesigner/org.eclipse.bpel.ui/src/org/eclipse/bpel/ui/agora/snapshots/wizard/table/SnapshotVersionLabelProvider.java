package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import java.text.SimpleDateFormat;

import org.eclipse.bpel.ui.agora.ode135.client.TSnapshotVersion;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * This class is used to display {@link TSnapshotVersion} objects and their
 * data in a table.
 * 
 * @author hahnml
 * 
 */
public class SnapshotVersionLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		TSnapshotVersion snapshot = (TSnapshotVersion) element;
		
		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );

		switch (columnIndex) {
		case 0:
			return snapshot.getSnapshotId();
		case 1:
			return snapshot.getVersion();
		case 2:
			return df.format(snapshot.getCreated().toGregorianCalendar().getTime());
		default:
			throw new RuntimeException("Should not happen");
		}
	}

}
