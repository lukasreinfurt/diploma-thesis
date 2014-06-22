package org.eclipse.bpel.ui.agora.instances;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.simtech.workflow.ode.auditing.communication.messages.InstanceInformation;

/**
 * This class is used to display InstanceInformation objects and their
 * data in a table.
 * 
 * @author hahnml
 * 
 */
public class InstanceLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 3) {
			switch (((InstanceInformation) element).getState()) {
			case Terminated:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_STOP_16);
			case Executing:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_RUN_16);
			case Suspended:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_SUSPEND_16);
			case Completed:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_STOP_16);
			case Faulted:
				return BPELUIPlugin.INSTANCE
						.getImage(IBPELUIConstants.ICON_FAULT_16);
			default:
				throw new RuntimeException("Should not happen");
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		InstanceInformation instance = (InstanceInformation) element;
		
		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
		
		switch (columnIndex) {
		case 0:
			return instance.getInstanceID().toString();
		case 1:
			return instance.getProcessName().toString();
		case 2:
			return instance.getProcessVersion().toString();
		case 3:
			return instance.getState().name();
		case 4:
			return df.format(new Date(instance.getTimestamp()));
		default:
			throw new RuntimeException("Should not happen");
		}
	}

}
