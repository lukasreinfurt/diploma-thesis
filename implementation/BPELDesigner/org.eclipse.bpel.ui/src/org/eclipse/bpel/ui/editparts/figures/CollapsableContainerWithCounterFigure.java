package org.eclipse.bpel.ui.editparts.figures;

import org.eclipse.bpel.ui.editparts.borders.ContainerWithCounterBorder;
import org.eclipse.swt.graphics.Image;

/**
 * A collapsable container with counter figure has everything a collapsable
 * container figure has, plus a label for a counter for loop activities.
 * 
 * @author hahnml
 * 
 */
public class CollapsableContainerWithCounterFigure extends CollapsableContainerFigure {

	protected String borderCounter = "0";

	public CollapsableContainerWithCounterFigure(Object modelObject,
			Image image, String text) {
		super(modelObject, image, text);
	}

	@Override
	protected void initializeBorder() {
		this.border = new ContainerWithCounterBorder(this, borderImage,
				borderText, borderCounter);
	}

	public void setCounter(String counter) {
		((ContainerWithCounterBorder)border).setCounter(counter);
		invalidate();
	}
}
