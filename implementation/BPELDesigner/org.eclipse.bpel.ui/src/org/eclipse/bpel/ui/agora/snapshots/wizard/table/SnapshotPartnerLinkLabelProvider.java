package org.eclipse.bpel.ui.agora.snapshots.wizard.table;

import org.eclipse.bpel.ui.agora.ode135.client.TPartnerLinkInfo;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This class is used to display {@link TPartnerLinkInfo} objects and their
 * data in a table.
 * 
 * @author hahnml
 * 
 */
public class SnapshotPartnerLinkLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	// Names of images used to represent checkboxes
	public static final String CHECKED_IMAGE = "checked";
	public static final String UNCHECKED_IMAGE = "unchecked";

	// For the checkbox images
	private static ImageRegistry imageRegistry = new ImageRegistry();

	/**
	 * Note: An image registry owns all of the image objects registered with it,
	 * and automatically disposes of them the SWT Display is disposed.
	 */
	static {
		String iconPath = "icons/";
		imageRegistry.put(CHECKED_IMAGE, AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclipse.bpel.ui", iconPath
						+ CHECKED_IMAGE + ".gif"));
		imageRegistry.put(UNCHECKED_IMAGE, AbstractUIPlugin
				.imageDescriptorFromPlugin("org.eclipse.bpel.ui", iconPath
						+ UNCHECKED_IMAGE + ".gif"));
	}

	/**
	 * Returns the image with the given key, or <code>null</code> if not found.
	 */
	private Image getImage(boolean isSelected) {
		String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
		return imageRegistry.get(key);
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return (columnIndex == 0) ? getImage(((PartnerLinkInfo) element).isSelected())
						: null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		PartnerLinkInfo partnerLink = (PartnerLinkInfo) element;
		
		switch (columnIndex) {
		case 0:
			return "";
		case 1:
			return partnerLink.getInfo().getSelf().getName();
		case 2:
			return partnerLink.getInfo().getSelf().getSiid();
		default:
			throw new RuntimeException("Should not happen");
		}
	}

}
