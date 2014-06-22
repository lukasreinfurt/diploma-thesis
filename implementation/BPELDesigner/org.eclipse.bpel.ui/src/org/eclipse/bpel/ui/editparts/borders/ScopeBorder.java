/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.bpel.ui.editparts.borders;

import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.core.resources.IMarker;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * This is the border for scope.
 */
public class ScopeBorder extends CollapsableBorder {

	// @hahnml: Copied from ContainerBorder
	// The horizontal margin between the border and the image/text
	private static final int leftMargin = 6;
	private static final int rightMargin = 10;
	// The vertical margin between the border and the image/text
	private static final int topMargin = 5;
	private static final int bottomMargin = 4;

	// space between image and label
	static final int spacing = 5;

	// TODO: Move these or remove them
	static final int borderWidth = 1;
	static final int margin = 11;
	// space between the inside of the border and the contents
	static final int hBorderInsets = 11;
	static final int vBorderInsets = 20;
	// extra horizontal space to use when we have no children.
	static final int extraHorizontalSpace = 50;

	// @hahnml: Copied from ContainerBorder
	// We keep this round rect around to paint.
	private RoundedRectangle roundRect;
	// The calculated bounds of the label and the image when expanded
	private Rectangle rectLabel, rectImageLabel;
	// The bounds of the round rectangle surrounding the label and image
	// when expanded
	private Rectangle rectLabelBorder;

	// Location of the "-" icons when the border is expanded
	private Rectangle rectExpandedTop, rectExpandedBottom;

	// The bounds of the border of the scope when expanded. Takes
	// into account space for the drawer.
	private Rectangle expandedBounds;

	// Fault handler, Compensation handler and Event handler support
	private int faultImageWidth, faultImageHeight;
	private int compensationImageWidth, compensationImageHeight;
	private int terminationImageWidth, terminationImageHeight;
	private int eventImageWidth, eventImageHeight;
	private Image compensationImage;
	private Image terminationImage;
	private Image faultImage;
	private Image eventImage;
	private boolean showFault;
	private boolean showCompensation;
	private boolean showTermination;
	private boolean showEvent;
	private Rectangle rectFault;
	private Rectangle rectCompensation;
	private Rectangle rectTermination;
	private Rectangle rectEvent;

	private IFigure faultImageFigure;
	private IFigure compensationImageFigure;
	private IFigure terminationImageFigure;
	private IFigure eventImageFigure;

	public ScopeBorder(IFigure parentFigure, String labelText, Image image) {
		super(true, IBPELUIConstants.ARC_WIDTH, parentFigure, labelText, image);

		this.roundRect = new RoundedRectangle();
		this.roundRect.setOpaque(true);
		this.roundRect.setCornerDimensions(new Dimension(
				IBPELUIConstants.ARC_WIDTH, IBPELUIConstants.ARC_WIDTH));

		// Initialize images for fault, compensation and event handler
		// decorations
		this.faultImage = BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_FAULT_INDICATOR);
		org.eclipse.swt.graphics.Rectangle r = faultImage.getBounds();
		this.faultImageWidth = r.width;
		this.faultImageHeight = r.height;

		this.compensationImage = BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_COMPENSATION_INDICATOR);
		r = compensationImage.getBounds();
		this.compensationImageWidth = r.width;
		this.compensationImageHeight = r.height;

		this.terminationImage = BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_TERMINATION_INDICATOR);
		r = terminationImage.getBounds();
		this.terminationImageWidth = r.width;
		this.terminationImageHeight = r.height;

		this.eventImage = BPELUIPlugin.INSTANCE
				.getImage(IBPELUIConstants.ICON_EVENT_INDICATOR);
		r = eventImage.getBounds();
		this.eventImageWidth = r.width;
		this.eventImageHeight = r.height;

		faultImageFigure = new ImageFigure(faultImage);
		faultImageFigure.setParent(parentFigure);

		compensationImageFigure = new ImageFigure(compensationImage);
		compensationImageFigure.setParent(parentFigure);

		terminationImageFigure = new ImageFigure(terminationImage);
		terminationImageFigure.setParent(parentFigure);

		eventImageFigure = new ImageFigure(eventImage);
		eventImageFigure.setParent(parentFigure);
	}

	@Override
	public Dimension getPreferredSize(IFigure f) {
		calculate(f);
		Dimension d = new Dimension(rectLabelBorder.getSize().width,
				rectLabelBorder.getSize().height + expandedHeight);
		d.width += DRAWER_WIDTH * 2;
		if (!isCollapsed()) {
			d.width += extraHorizontalSpace;
		}
		return d;
	}

	@Override
	protected void doPaint(IFigure figure, Graphics graphics, Insets insets) {
		super.doPaint(figure, graphics, insets);

		ColorRegistry registry = BPELUIPlugin.INSTANCE.getColorRegistry();

		Color old = graphics.getForegroundColor();
		graphics.setForegroundColor(registry
				.get(IBPELUIConstants.COLOR_SCOPE_BORDER));
		graphics.drawRoundRectangle(expandedBounds, IBPELUIConstants.ARC_WIDTH,
				IBPELUIConstants.ARC_WIDTH);
		graphics.drawRectangle(expandedBounds.x, expandedBounds.y,
				expandedBounds.width, 3);
		graphics.setForegroundColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		graphics.drawRectangle(expandedBounds.x + 1, expandedBounds.y + 1,
				expandedBounds.width - 2, 1);
		graphics.setForegroundColor(old);

		// Paint the round rectangle at the top.
		// First determine whether or not square corners are needed on the left
		// edge.
		boolean needSquareCorners = (getTopMarker() != null)
				|| (getBottomMarker() != null);
		if (isCollapsed() && needSquareCorners) {
			// Remember the clipping rectangle
			Rectangle oldClip = new Rectangle();
			oldClip = graphics.getClip(oldClip);

			Rectangle clippingRect = new Rectangle(rectLabelBorder.x
					+ rectLabelBorder.width / 2, rectLabelBorder.y,
					rectLabelBorder.width / 2 + 2, rectLabelBorder.height + 1);
			graphics.setClip(clippingRect);
			graphics.drawRoundRectangle(rectLabelBorder,
					IBPELUIConstants.ARC_WIDTH, IBPELUIConstants.ARC_WIDTH);
			clippingRect = new Rectangle(rectLabelBorder.x, rectLabelBorder.y,
					rectLabelBorder.width / 2 + 1, rectLabelBorder.height + 1);
			graphics.setClip(clippingRect);
			graphics.drawRectangle(rectLabelBorder);

			// Restore the clipping
			graphics.setClip(oldClip);
		} else {
			roundRect.setBounds(rectLabelBorder);
			roundRect.setForegroundColor(BPELUIPlugin.INSTANCE
					.getColorRegistry().get(
							IBPELUIConstants.COLOR_ACTIVITY_BORDER));
			roundRect.paint(graphics);
		}

		old = graphics.getForegroundColor();
		graphics.setForegroundColor(registry.get(IBPELUIConstants.COLOR_BLACK));
		collapsedNameLabel.setBounds(rectLabel);
		collapsedNameLabel.paint(graphics);
		imageLabel.setBounds(rectImageLabel);
		imageLabel.paint(graphics);
		graphics.setForegroundColor(old);

		if (isCollapsed()) {
			graphics.drawImage(collapsedImage, rectCollapsed.getLocation());
		} else {
			graphics.drawImage(expandedImage, rectExpandedTop.getLocation());
			graphics.drawImage(expandedImage, rectExpandedBottom.getLocation());
		}

		Rectangle oldClip = new Rectangle();
		oldClip = graphics.getClip(oldClip);

		// Draw the fault image in the upper right hand corner of the round
		// rectangle
		if (showFault) {
			graphics.setClip(faultImageFigure.getBounds().getCopy());
			faultImageFigure.paint(graphics);
			graphics.setClip(oldClip);
		}
		// Draw the compensation image in the upper right hand corner of the
		// round rectangle,
		// leaving room for the fault image.
		if (showCompensation) {
			graphics.setClip(compensationImageFigure.getBounds().getCopy());
			compensationImageFigure.paint(graphics);
			graphics.setClip(oldClip);
		}
		// Draw the termination image in the upper right hand corner of the
		// round rectangle,
		// leaving room for the fault image.
		if (showTermination) {
			graphics.setClip(terminationImageFigure.getBounds().getCopy());
			terminationImageFigure.paint(graphics);
			graphics.setClip(oldClip);
		}
		// Draw the event image in the upper right hand corner of the round
		// rectangle,
		// leaving room for fault and compensation.
		if (showEvent) {
			graphics.setClip(eventImageFigure.getBounds().getCopy());
			eventImageFigure.paint(graphics);
			graphics.setClip(oldClip);
		}
	}

	@Override
	protected void calculate(IFigure figure) {
		super.calculate(figure);

		// Bounds of the figure that we are given
		Rectangle figureBounds = figure.getBounds().getCopy();

		// preferred size of the image
		Dimension imageLabelSize = imageLabel.getPreferredSize().getCopy();
		// preferred size of the text label
		Dimension labelSize = collapsedNameLabel.getPreferredSize().getCopy();

		// calculate the label border for the round rectangle
		// surrounding the label and image
		int w = labelSize.width + spacing + imageLabelSize.width;
		int h = Math.max(labelSize.height, imageLabelSize.height);
		int x = figureBounds.x + figureBounds.width / 2 - (w / 2);
		int y = figureBounds.y;
		rectLabelBorder = new Rectangle(x, y, w, h);

		// expand the border for aesthetics and to account for arc size.
		// note we don't use the entire arc size to conserve space.
		// also remember we can't expand in the upwards direction.
		int verticalMargin = topMargin + bottomMargin;
		if (isCollapsed())
			verticalMargin--;
		rectLabelBorder.expand(new Insets(0, leftMargin, verticalMargin,
				rightMargin));

		// rectangle for image label
		x = rectLabelBorder.x + leftMargin;
		y = rectLabelBorder.y + topMargin;
		w = imageLabelSize.width;
		h = imageLabelSize.height;
		rectImageLabel = new Rectangle(x, y, w, h);

		// rectangle for text label
		x = rectLabelBorder.x + leftMargin + imageLabelSize.width + spacing;
		y = rectLabelBorder.y + topMargin;
		w = labelSize.width;
		h = labelSize.height;
		rectLabel = new Rectangle(x, y, w, h);

		// calculate the size of the round rectangle surrounding the children,
		// taking into account arc size and drawer width
		this.expandedBounds = figureBounds.getCopy();
		if (isCollapsed()) {
			// Leave room for the drawers
			expandedBounds.x = rectLabelBorder.x - 10;
			expandedBounds.width = rectLabelBorder.width + 20;
			// Leave room on the top for part of the top expanded image
			expandedBounds.y += rectLabelBorder.height / 2;
			expandedBounds.height = rectLabelBorder.height;
		} else {
			// Leave room for the drawers
			expandedBounds.x += DRAWER_WIDTH;
			expandedBounds.width -= DRAWER_WIDTH * 2;
			// Leave room on the top for part of the top expanded image
			expandedBounds.y += rectLabelBorder.height / 2;
			expandedBounds.height -= rectLabelBorder.height / 2;
			// Room for the bottom
			expandedBounds.height -= expandedHeight / 2;
		}

		// area for plus/minus buttons
		rectExpandedTop = new Rectangle(rectLabelBorder.x
				+ rectLabelBorder.width / 2 - expandedWidth / 2,
				rectLabelBorder.y + rectLabelBorder.height - 1, expandedWidth,
				expandedHeight);
		rectExpandedBottom = new Rectangle(figureBounds.x + figureBounds.width
				/ 2 - expandedWidth / 2, figureBounds.y + figureBounds.height
				- expandedHeight, expandedWidth, expandedHeight);

		// Calculate the positions of the handler icons
		computeHandlerIconPositions(ModelHelper
				.isHorizontalLayout(getEditPart().getModel()), figure);

		// Top drawer
		IMarker topMarker = getTopMarker();
		if (topMarker != null) {
			// Draw the image for the top drawer
			if (isCollapsed()) {
				topDrawerLocation.x = collapsedRectangle.x - DRAWER_WIDTH
						+ DRAWER_INSET + 1;
				topDrawerLocation.y = collapsedRectangle.y;
			} else {
				topDrawerLocation.x = expandedBounds.x - DRAWER_WIDTH;
				topDrawerLocation.y = expandedBounds.y
						+ IBPELUIConstants.ARC_WIDTH;
			}
		}
		// Bottom drawer
		IMarker bottomMarker = getBottomMarker();
		if (bottomMarker != null) {
			// Draw the image for the bottom drawer
			if (isCollapsed()) {
				bottomDrawerLocation.x = collapsedRectangle.x - DRAWER_WIDTH
						+ DRAWER_INSET + 1;
				bottomDrawerLocation.y = collapsedRectangle.y
						+ DRAWER_HALF_HEIGHT;
			} else {
				bottomDrawerLocation.x = expandedBounds.x - DRAWER_WIDTH;
				bottomDrawerLocation.y = expandedBounds.y + DRAWER_HALF_HEIGHT
						+ IBPELUIConstants.ARC_WIDTH;
			}
		}
		// Top drawer marker image
		Image topImage = getTopImage();
		if (topImage != null) {
			if (isCollapsed()) {
				topImageLocation.x = collapsedRectangle.x - DRAWER_WIDTH
						+ DRAWER_INSET + 2;
				topImageLocation.y = collapsedRectangle.y + DRAWER_INSET;
			} else {
				topImageLocation.x = expandedBounds.x - DRAWER_WIDTH
						+ DRAWER_INSET;
				topImageLocation.y = expandedBounds.y
						+ IBPELUIConstants.ARC_WIDTH + DRAWER_INSET;
			}
		}
		// Bottom drawer marker image
		Image bottomImage = getBottomImage();
		if (bottomImage != null) {
			if (isCollapsed()) {
				bottomImageLocation.x = collapsedRectangle.x - DRAWER_WIDTH
						+ DRAWER_INSET + 2;
				bottomImageLocation.y = collapsedRectangle.y + DRAWER_INSET
						+ DRAWER_HALF_HEIGHT;
			} else {
				bottomImageLocation.x = expandedBounds.x - DRAWER_WIDTH
						+ DRAWER_INSET;
				bottomImageLocation.y = expandedBounds.y
						+ IBPELUIConstants.ARC_WIDTH + DRAWER_HALF_HEIGHT
						+ DRAWER_INSET;
			}
		}
	}

	@Override
	public Insets getInsets(IFigure figure) {
		if (isCollapsed())
			return super.getInsets(figure);
		calculate(figure);
		Insets result = new Insets(vBorderInsets + rectLabelBorder.height,
				hBorderInsets, vBorderInsets + 5, hBorderInsets);
		result.left += DRAWER_WIDTH;
		result.right += DRAWER_WIDTH;
		return result;
	}

	/**
	 * Throw away values that determine the layout
	 */
	@Override
	public void invalidate() {
		super.invalidate();
		rectLabelBorder = null;
		rectLabel = null;
		rectExpandedTop = null;
		rectExpandedBottom = null;
	}

	public boolean isPointInFaultImage(int x, int y) {
		if (showFault) {
			Point p = new Point(x, y);
			parentFigure.translateToRelative(p);
			return rectFault.contains(p);
		}
		return false;
	}

	public boolean isPointInCompensationImage(int x, int y) {
		if (showCompensation) {
			Point p = new Point(x, y);
			parentFigure.translateToRelative(p);
			return rectCompensation.contains(p);
		}
		return false;
	}

	public boolean isPointInTerminationImage(int x, int y) {
		if (showTermination) {
			Point p = new Point(x, y);
			parentFigure.translateToRelative(p);
			return rectTermination.contains(p);
		}
		return false;
	}

	public boolean isPointInEventImage(int x, int y) {
		if (showEvent) {
			Point p = new Point(x, y);
			parentFigure.translateToRelative(p);
			return rectEvent.contains(p);
		}
		return false;
	}

	public void setShowEvent(boolean showEvent) {
		this.showEvent = showEvent;
	}

	public void setShowCompensation(boolean showCompensation) {
		this.showCompensation = showCompensation;
	}

	public void setShowTermination(boolean showTermination) {
		this.showTermination = showTermination;
	}

	public void setShowFault(boolean showFault) {
		this.showFault = showFault;
	}

	/**
	 * Tests whether the given point is inside the collapse image. The
	 * superclass does not know where the collapse image(s) is located.
	 */
	@Override
	public boolean isPointInCollapseImage(int x, int y) {
		if (isCollapsed())
			return super.isPointInCollapseImage(x, y);
		Point p = new Point(x, y);
		parentFigure.translateToRelative(p);
		Rectangle rect = rectExpandedTop.getCopy();
		rect.expand(new Insets(1, 1, 1, 1));
		if (rect.contains(p))
			return true;
		if (!isCollapsed()) {
			rect = rectExpandedBottom.getCopy();
			rect.expand(new Insets(1, 1, 1, 1));
			return rect.contains(p);
		}
		return false;
	}

	/**
	 * Provide gradient rectangle.
	 */
	@Override
	protected Rectangle getGradientRect() {
		if (isCollapsed())
			return super.getGradientRect();
		invalidate();
		calculate(parentFigure);
		return expandedBounds;
	}

	private void computeHandlerIconPositions(boolean horizontal, IFigure figure) {
		if (horizontal) {
			// Calculate the location of the fault/event/compensation
			// decorations
			if (showFault) {
				int x = expandedBounds.x + expandedBounds.width
						- faultImageWidth;
				if (showCompensation)
					x -= compensationImageWidth;
				if (showTermination)
					x -= terminationImageWidth;
				if (showEvent)
					x -= eventImageWidth;
				this.rectFault = new Rectangle(x, figure.getBounds().y
						+ figure.getBounds().height - faultImageHeight,
						faultImageWidth, faultImageHeight);
			}
			if (showCompensation) {
				int x = expandedBounds.x + expandedBounds.width
						- compensationImageWidth;
				if (showTermination)
					x -= terminationImageWidth;
				if (showEvent)
					x -= eventImageWidth;
				this.rectCompensation = new Rectangle(x, figure.getBounds().y
						+ figure.getBounds().height - compensationImageHeight,
						compensationImageWidth, compensationImageHeight);
			}
			if (showTermination) {
				int x = expandedBounds.x + expandedBounds.width
						- terminationImageWidth;
				if (showEvent)
					x -= eventImageWidth;
				this.rectTermination = new Rectangle(x, figure.getBounds().y
						+ figure.getBounds().height - terminationImageHeight,
						terminationImageWidth, terminationImageHeight);
			}
			if (showEvent) {
				int x = expandedBounds.x + expandedBounds.width
						- eventImageWidth;
				this.rectEvent = new Rectangle(x, figure.getBounds().y
						+ figure.getBounds().height - eventImageHeight,
						eventImageWidth, eventImageHeight);
			}
		} else {
			// Calculate the location of the fault/event/compensation
			// decorations
			// Draw the fault image in the upper right hand corner of the round
			// rectangle
			if (showFault) {
				int x = expandedBounds.x + expandedBounds.width
						- faultImageWidth + 1;
				this.rectFault = new Rectangle(x, expandedBounds.y,
						faultImageWidth, faultImageHeight);
			}
			// Draw the compensation image in the upper right hand corner of the
			// round rectangle,
			// leaving room for the fault image.
			if (showCompensation) {
				int x = expandedBounds.x + expandedBounds.width
						- compensationImageWidth + 1;
				int y = expandedBounds.y;
				if (showFault) {
					y += faultImageHeight;
				}
				this.rectCompensation = new Rectangle(x, y,
						compensationImageWidth, compensationImageHeight);
			}
			// Draw the termination image in the upper right hand corner of the
			// round rectangle,
			// leaving room for fault and compensation.
			if (showTermination) {
				int x = expandedBounds.x + expandedBounds.width
						- terminationImageWidth + 1;
				int y = expandedBounds.y;
				if (showFault) {
					y += faultImageWidth;
				}
				if (showCompensation) {
					y += compensationImageWidth;
				}
				this.rectTermination = new Rectangle(x, y,
						terminationImageWidth, terminationImageHeight);
			}
			// Draw the event image in the upper right hand corner of the round
			// rectangle,
			// leaving room for fault, termination and compensation.
			if (showEvent) {
				int x = expandedBounds.x + expandedBounds.width
						- eventImageWidth + 1;
				int y = expandedBounds.y;
				if (showFault) {
					y += faultImageHeight;
				}
				if (showCompensation) {
					y += compensationImageHeight;
				}
				if (showTermination) {
					y += terminationImageWidth;
				}
				this.rectEvent = new Rectangle(x, y, eventImageWidth,
						eventImageHeight);
			}
		}

		// Apply the bounds to the figures
		if (showFault)
			this.faultImageFigure.setBounds(rectFault);
		if (showCompensation)
			this.compensationImageFigure.setBounds(rectCompensation);
		if (showTermination)
			this.terminationImageFigure.setBounds(rectTermination);
		if (showEvent)
			this.eventImageFigure.setBounds(rectEvent);
	}

	public IFigure getFaultImageFigure() {
		return faultImageFigure;
	}

	public IFigure getCompensationImageFigure() {
		return compensationImageFigure;
	}

	public IFigure getTerminationImageFigure() {
		return terminationImageFigure;
	}

	public IFigure getEventImageFigure() {
		return eventImageFigure;
	}

	/**
	 * Edit parts may like to know what the top inset is - this is the distance
	 * between the top of the border and the top of the container round
	 * rectangle. This is useful because this is where selection handles are
	 * often located.
	 */
	public int getTopInset() {
		// HACK! This is necessary to prevent certain NPEs.
		if (rectLabelBorder == null || expandedBounds == null) {
			calculate(parentFigure);
		}

		return expandedBounds.y - rectLabelBorder.y;
	}
}