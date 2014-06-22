package org.eclipse.bpel.ui.agora.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Displays the event information in the auditing view table.
 * 
 * @author hahnml
 *
 */
public class EventLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		EventMessage event = (EventMessage) element;
		
		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
		
		switch (columnIndex) {
		case 0:
			return event.getEventType();
		case 1:
			return event.getSource();
		case 2:
			return event.getElementName();
		case 3:
			return event.getTimestamp() != null ? df.format(new Date(event.getTimestamp())) : "";
		case 4:
			return event.getState() != null ? event.getState().name() : "";
		default:
			throw new RuntimeException("Too much columns");
		}

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

}
