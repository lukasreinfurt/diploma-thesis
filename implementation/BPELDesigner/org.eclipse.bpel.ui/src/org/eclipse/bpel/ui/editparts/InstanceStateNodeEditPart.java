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
package org.eclipse.bpel.ui.editparts;

import org.eclipse.bpel.common.ui.layouts.AlignedFlowLayout;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.adapters.ILabeledElement;
import org.eclipse.bpel.ui.editparts.borders.LeafBorder;
import org.eclipse.bpel.ui.editparts.figures.GradientFigure;
import org.eclipse.bpel.ui.editparts.policies.BPELSelectionEditPolicy;
import org.eclipse.bpel.ui.figures.ILayoutAware;
import org.eclipse.bpel.ui.uiextensionmodel.BPELStates;
import org.eclipse.bpel.ui.uiextensionmodel.InstanceState;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Realizes the figure in the upper left corner which shows the state of the
 * actually opened instance.
 * 
 * @author hahnml
 * 
 */
public class InstanceStateNodeEditPart extends BPELEditPart implements
		NodeEditPart, ILayoutAware {

	protected Image image;
	protected int imageWidth;
	protected int imageHeight;
	private Label imageLabel;
	private Label nameLabel;
	private ILabeledElement element;
	private IFigure contentFigure;

	// The figure which holds contentFigure and auxilaryFigure as children
	private IFigure parentFigure;

	// The figure which holds fault handler, compensation handler and event
	// handler
	private IFigure auxilaryFigure;

	// Determines the border which will be added to both sides (left/right)
	// As a result this value is the distance to any attached handlers.
	public static final int BORDER_WIDTH = 8;

	public InstanceStateNodeEditPart() {

	}

	@Override
	protected void addAllAdapters() {
		super.addAllAdapters();
		adapter.addToObject(getInstanceNode());
	}

	@Override
	protected void removeAllAdapters() {
		adapter.removedFrom(getInstanceNode());
		super.removeAllAdapters();
	}

	@Override
	protected IFigure createFigure() {

		if (image == null) {
			element = BPELUtil.adapt(getInstanceNode(), ILabeledElement.class);
			image = element.getSmallImage(getInstanceNode());
			Rectangle rect = image.getBounds();
			imageWidth = rect.width;
			imageHeight = rect.height;
		}

		this.imageLabel = new Label(image);
		this.nameLabel = new Label(getLabel());

		this.nameLabel.setFont(new Font(Display.getDefault(), "Arial", 14,
				SWT.BOLD));

		this.parentFigure = new Figure();
		AlignedFlowLayout layout = new AlignedFlowLayout();
		layout.setHorizontal(true);
		parentFigure.setLayoutManager(layout);

		contentFigure = new GradientFigure(getModel());
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
		contentFigure.setLayoutManager(flowLayout);
		contentFigure.setForegroundColor(BPELUIPlugin.INSTANCE
				.getColorRegistry().get(IBPELUIConstants.COLOR_BLACK));
		contentFigure.add(imageLabel);
		contentFigure.add(nameLabel);

		parentFigure.add(contentFigure);

		this.auxilaryFigure = new Figure();
		layout = new AlignedFlowLayout();
		layout.setHorizontalAlignment(AlignedFlowLayout.ALIGN_CENTER);
		layout.setHorizontal(!ModelHelper.isHorizontalLayout(getModel()));
		auxilaryFigure.setBorder(new MarginBorder(0, 0, 0, 0));
		auxilaryFigure.setLayoutManager(layout);

		parentFigure.add(auxilaryFigure);

		LeafBorder border = new LeafBorder(contentFigure, true);
		border.setEditPart(this);
		contentFigure.setBorder(border);

		setFigure(parentFigure);

		return parentFigure;
	}

	/**
	 * @see org.eclipse.bpel.ui.editparts.BPELEditPart#refreshVisuals()
	 */
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();

		this.contentFigure.remove(this.imageLabel);
		this.contentFigure.remove(this.nameLabel);

		this.imageLabel = new Label(this.element
				.getSmallImage(getInstanceNode()));
		this.nameLabel = new Label(getLabel());

		this.nameLabel.setFont(new Font(Display.getDefault(), "Arial", 14,
				SWT.BOLD));
		this.contentFigure.add(this.imageLabel);
		this.contentFigure.add(this.nameLabel);

		getFigure().revalidate();

		// Force a repaint, as the decorations.
		getFigure().repaint();
	}

	// @Override
	// protected void handleModelChanged() {
	// super.handleModelChanged();
	//
	// super.refresh();
	// }

	/**
	 * Return the start node.
	 * 
	 * @return return the start node.
	 */
	public InstanceState getInstanceNode() {
		return (InstanceState) getModel();
	}

	/**
	 * Return the process.
	 * 
	 * @return the process
	 */

	public BPELStates getState() {
		return getInstanceNode().getState();
	}

	@Override
	protected void createEditPolicies() {
		// Don't call super because we don't want a component edit policy
		// or a direct edit policy.

		// Show the selection rectangle
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
				new BPELSelectionEditPolicy(false, false) {
					@Override
					protected int getWestInset() {
						Insets ins = ((InstanceStateNodeEditPart) getHost())
								.getFigure().getInsets();
						if (ins != null) {
							return ins.left;
						}
						return super.getWestInset();
					}

					@Override
					protected int getEastInset() {
						Insets ins = ((InstanceStateNodeEditPart) getHost())
								.getFigure().getInsets();
						if (ins != null) {
							return ins.right;
						}
						return super.getEastInset();
					}
				});
	}

	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connEditPart) {
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connEditPart) {
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return null;
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return null;
	}

	public void switchLayout(boolean horizontal) {
		if (horizontal) {
			getFigure().setBorder(
					new MarginBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH,
							BORDER_WIDTH));
		} else {
			getFigure().setBorder(
					new MarginBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH,
							BORDER_WIDTH));
		}
	}

	protected String getLabel() {
		ILabeledElement element = BPELUtil.adapt(getInstanceNode(),
				ILabeledElement.class);
		return element.getLabel(getInstanceNode());
	}
}
