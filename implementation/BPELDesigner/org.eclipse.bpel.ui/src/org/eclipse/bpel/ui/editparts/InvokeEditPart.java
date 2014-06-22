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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.common.ui.layouts.AlignedFlowLayout;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Documentation;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.IBPELUIConstants;
import org.eclipse.bpel.ui.adapters.ICompensationHandlerHolder;
import org.eclipse.bpel.ui.adapters.IFaultHandlerHolder;
import org.eclipse.bpel.ui.adapters.ILabeledElement;
import org.eclipse.bpel.ui.editparts.borders.LeafBorder;
import org.eclipse.bpel.ui.editparts.figures.GradientFigure;
import org.eclipse.bpel.ui.figures.CenteredConnectionAnchor;
import org.eclipse.bpel.ui.figures.ILayoutAware;
import org.eclipse.bpel.ui.figures.InvokeHandlerLinker;
import org.eclipse.bpel.ui.util.BPELDragEditPartsTracker;
import org.eclipse.bpel.ui.util.BPELUtil;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.bpel.ui.util.marker.BPELEditPartMarkerDecorator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Element;

/**
 * There is a lot of code here copied from ScopeEditPart, but there was no
 * obvious way to share this code in a meaningful way.
 */
public class InvokeEditPart extends LeafEditPart implements ILayoutAware {

	// The horizontal margin between the border and the image/text
	private static final int leftMargin = 6;
	private static final int rightMargin = 13;
	// The vertical margin between the border and the image/text
	private static final int topMargin = 4;
	private static final int bottomMargin = 3;

	// Whether to show each of the handlers.
	// TODO: Initialize these from the preferences store
	private boolean showFH = false, showCH = false;

	// The figure which holds contentFigure and auxilaryFigure as children
	private IFigure parentFigure;

	// The figure which holds fault handler, compensation handler and event
	// handler
	private IFigure auxilaryFigure;

	// The Handler which handles the drawing of links to the handlers.
	private InvokeHandlerLinker handlerLinker;

	/**
	 * Override getDragTracker to supply double-click behaviour to expand the
	 * fault handler if one exists
	 */
	@Override
	public DragTracker getDragTracker(Request request) {
		return new BPELDragEditPartsTracker(this) {
			@Override
			protected boolean handleButtonDown(int button) {
				Point point = getLocation();
				if (border.isPointInFaultImage(point.x, point.y)) {
					setShowFaultHandler(!getShowFaultHandler());
				}
				if (border.isPointInCompensationImage(point.x, point.y)) {
					setShowCompensationHandler(!getShowCompensationHandler());
				}
				return super.handleButtonDown(button);
			}
		};
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	@Override
	public void refreshVisuals() {
		super.refreshVisuals();
		border.setShowFault(getFaultHandler() != null);
		border.setShowCompensation(getCompensationHandler() != null);

		if (ModelHelper.isHorizontalLayout(getModel())) {
			auxilaryFigure.getInsets().top = contentFigure.getBounds().height + 10;
		} else
			auxilaryFigure.getInsets().top = 0;

		getFigure().repaint();
	}


	/**
	 * Gets the current icon of the invoke activity.
	 * 
	 * @return current icon of the invoke activity
	 * 
	 * @author sonntamo
	 */
	public Image getImage() {
		return image;
	}
	
	/**
	 * Sets a new icon for this invoke activity.
	 * 
	 * @param op The WSDL operation of this invoke activity where we search for a registered icon.
	 * 
	 * @author sonntamo
	 */
	public void setNewIcon(org.eclipse.wst.wsdl.Operation op) {
		
		// The default icon is the usual icon for this activity.
		ILabeledElement element = BPELUtil.adapt(getActivity(),
				ILabeledElement.class);
		image = element.getSmallImage(getActivity());
		
		// now we check the WSDL operation for a registered icon
		if (op != null) {
			Element opElem = op.getElement();
			if (opElem != null) {
				String iconURL = op.getElement().getAttributeNS("http://iaas.uni-stuttgart.de/wsdlIconExtension", "icon");
				if (iconURL != null && !"".equals(iconURL)) {
					ImageDescriptor x = null;
					try {
						URL url = new URL(iconURL);
						try {
							// this is just to test if the URL is valid
							Object obj = url.getContent();
							
							x = ImageDescriptor.createFromURL(url);
							image = x.createImage();
						} catch (IOException e) {
							// Something went wrong. Nothing to do. We will take the default icon.
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				} 
			}
		}
		
		// Now we refresh the figure of the activity. The second child is the text
		if (colorFigure != null) {
			if (colorFigure.getChildren().size() > 1) {
				Label text = (Label)colorFigure.getChildren().get(1);
				colorFigure.removeAll();
				colorFigure.add(new Label(image));
				colorFigure.add(text);
				parentFigure.repaint();
			}
		}

		// if the invoke activity is part of an aggregate, then the corresponding sequence activity
		// gets the icon of the invoke activity. Every subsequent invoke activity in the aggregate
		// is going to override the icon. The last invoke activity icon "wins".
		EditPart ep = this.getParent();
		if (ep != null && ep instanceof SequenceEditPart) {
			SequenceEditPart sep = (SequenceEditPart)ep;
			Documentation doc = sep.getActivity().getDocumentation();
			if (doc != null && doc.getValue().equals("aggregate")) {
				sep.setNewIcon(image);
			}
		}
	}
	
	// here's the one from scope edit part
	@Override
	protected IFigure createFigure() {
		
		if (image == null) {

			//@sonntamo: commented old way of setting the icon
//			ILabeledElement element = BPELUtil.adapt(getActivity(),
//					ILabeledElement.class);
//			image = element.getSmallImage(getActivity());
			
			//@sonntamo: new way: if it is an invoke activity, the we check the WSDL operation
			// for a registered icon.
			Activity act = getActivity();
			if (act instanceof Invoke) {
				Invoke inv = (Invoke)act;
				org.eclipse.wst.wsdl.Operation op = inv.getOperation();
				this.setNewIcon(op);
			}
			
		}
		this.imageLabel = new Label(image);
		this.nameLabel = new Label(getLabel());

		editPartMarkerDecorator = new BPELEditPartMarkerDecorator(
				(EObject) getModel(), new LeafDecorationLayout());
		editPartMarkerDecorator
				.addMarkerMotionListener(getMarkerMotionListener());

		this.parentFigure = new Figure();
		AlignedFlowLayout layout = new AlignedFlowLayout();
		layout.setHorizontal(true);
		layout.setHorizontalSpacing(0);
		layout.setVerticalSpacing(0);
		parentFigure.setLayoutManager(layout);

		IFigure gradient = new GradientFigure(getModel());
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setMinorAlignment(FlowLayout.ALIGN_CENTER);
		gradient.setLayoutManager(flowLayout);
		gradient.setForegroundColor(BPELUIPlugin.INSTANCE.getColorRegistry()
				.get(IBPELUIConstants.COLOR_BLACK));

		/*
		 * @hahnml: The problem with the color is that we don't have ONE figure
		 * to set it, because the label and image are two different figures. So
		 * we have to create a new figure which holds the background color and
		 * add the image and label figure to it.
		 * 
		 * We have to create the colorFigure here again because the creation of the
		 * InvokeFigure will be done here but the other methods like
		 * refreshVisuals() will be called from the parent class LeafEditPart.
		 */

		Panel colorPanel = new Panel();
		colorPanel.setLayoutManager(layout);
		colorPanel.setForegroundColor(ColorConstants.black);
		// colorPanel.setBackgroundColor(ColorConstants.yellow);
		colorPanel.setOpaque(true);
		colorPanel.add(imageLabel);
		colorPanel.add(nameLabel);
		// The margins where copied from the LeafBorder class, where they are
		// deleted.
		colorPanel.setBorder(new MarginBorder(topMargin, leftMargin,
				bottomMargin, rightMargin));
		gradient.add(colorPanel);
		this.colorFigure = colorPanel;

		gradient.addMouseMotionListener(getMouseMotionListener());
		this.contentFigure = gradient;
		parentFigure.add(contentFigure);

		this.auxilaryFigure = new Figure();
		layout = new AlignedFlowLayout();
		layout.setHorizontal(!ModelHelper.isHorizontalLayout(getModel()));
		auxilaryFigure.setBorder(new MarginBorder(0, 0, 0, 0));
		auxilaryFigure.setLayoutManager(layout);
		parentFigure.add(auxilaryFigure);

		this.border = new LeafBorder(gradient, false);
		this.border.setEditPart(this);
		border.setShowFault(getFaultHandler() != null);
		border.setShowCompensation(getCompensationHandler() != null);
		gradient.setBorder(border);

		return editPartMarkerDecorator.createFigure(parentFigure);
	}

	public void setShowFaultHandler(boolean showFaultHandler) {
		this.showFH = showFaultHandler;
		// Call refresh so that both refreshVisuals and refreshChildren will be
		// called.
		refresh();
	}

	public void setShowCompensationHandler(boolean showCompensationHandler) {
		this.showCH = showCompensationHandler;
		// Call refresh so that both refreshVisuals and refreshChildren will be
		// called.
		refresh();
	}

	public boolean getShowFaultHandler() {
		return showFH;
	}

	public boolean getShowCompensationHandler() {
		return showCH;
	}

	public FaultHandler getFaultHandler() {
		IFaultHandlerHolder holder = BPELUtil.adapt(getActivity(),
				IFaultHandlerHolder.class);
		if (holder != null) {
			return holder.getFaultHandler(getActivity());
		}
		return null;
	}

	public CompensationHandler getCompensationHandler() {
		ICompensationHandlerHolder holder = BPELUtil.adapt(getActivity(),
				ICompensationHandlerHolder.class);
		if (holder != null) {
			return holder.getCompensationHandler(getActivity());
		}
		return null;
	}

	/**
	 * The top inner implicit connection needs an offset of eight to compensate
	 * for the expansion icon. All other connections are centered on the
	 * contentFigure with an offset of 0.
	 */
	@Override
	public ConnectionAnchor getConnectionAnchor(int location) {
		switch (location) {
		case CenteredConnectionAnchor.TOP_INNER:
			return new CenteredConnectionAnchor(contentFigure, location, 8);
		case CenteredConnectionAnchor.LEFT:
			return new CenteredConnectionAnchor(contentFigure,
					CenteredConnectionAnchor.LEFT_INNER, 0);
		case CenteredConnectionAnchor.RIGHT:
			return new CenteredConnectionAnchor(contentFigure,
					CenteredConnectionAnchor.RIGHT_INNER, 0);
		default:
			return new CenteredConnectionAnchor(contentFigure, location, 0);
		}
	}

	/**
	 * Override addChildVisual so that it adds the childEditPart to the correct
	 * figure. FH/EH/CH go in a different figure than the activity does.
	 */
	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
		IFigure content = getContentPane(childEditPart);
		if (content != null) {
			if (content == auxilaryFigure && content.getChildren().size() > 0) {
				// The handlers have to get sorted to properly display the
				// handlerlinks
				content.add(child, getIndexForChild(childEditPart));
			} else
				content.add(child);
		}
	}

	private int getIndexForChild(EditPart editPart) {
		return editPart.getModel() instanceof CompensationHandler ? 0 : 1;
	}

	/**
	 * Override removeChildVisual so that it removes the childEditPart from the
	 * correct figure. FH/EH/CH live in a different figure than the activity
	 * does.
	 */
	@Override
	protected void removeChildVisual(EditPart childEditPart) {
		IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
		getContentPane(childEditPart).remove(child);
	}

	/**
	 * Also overridden to call getContentPane(child) in the appropriate place.
	 */
	@Override
	protected void reorderChild(EditPart child, int index) {
		// Save the constraint of the child so that it does not
		// get lost during the remove and re-add.
		IFigure childFigure = ((GraphicalEditPart) child).getFigure();
		LayoutManager layout = getContentPane(child).getLayoutManager();
		Object constraint = null;
		if (layout != null)
			constraint = layout.getConstraint(childFigure);

		removeChildVisual(child);
		List<EditPart> children = getChildren();
		children.remove(child);
		children.add(index, child);
		addChildVisual(child, index);

		setLayoutConstraint(child, childFigure, constraint);
	}

	/**
	 * Yet Another Overridden Method.
	 */
	@Override
	public void setLayoutConstraint(EditPart child, IFigure childFigure,
			Object constraint) {
		getContentPane(child).setConstraint(childFigure, constraint);
	}

	/**
	 * This method hopefully shouldn't be called, in favour of
	 * getContentPane(EditPart).
	 */
	@Override
	public IFigure getContentPane() {
		return contentFigure;
	}

	/**
	 * Return the appropriate content pane into which the given child should be
	 * inserted. For faultHandler, compensationHandler and eventHandler, the
	 * answer is auxilaryFigure; for activities it is the contentFigure.
	 */
	protected IFigure getContentPane(EditPart childEditPart) {
		Object model = childEditPart.getModel();
		if (model instanceof FaultHandler) {
			return auxilaryFigure;
		} else if (model instanceof CompensationHandler) {
			return auxilaryFigure;
		}
		return contentFigure;
	}

	/**
	 * Return a list of the model children that should be displayed. This
	 * includes fault handlers and compensation handlers.
	 */
	@Override
	protected List<BPELExtensibleElement> getModelChildren() {
    	ArrayList<BPELExtensibleElement> children = new ArrayList<BPELExtensibleElement>();

		if (showFH) {
			FaultHandler faultHandler = this.getFaultHandler();
			if (faultHandler != null)
				children.add(children.size(), faultHandler);
		}

		if (showCH) {
			CompensationHandler compensationHandler = this
					.getCompensationHandler();
			if (compensationHandler != null)
				children.add(children.size(), compensationHandler);
		}

		return children;
	}

	@Override
	public IFigure getMainActivityFigure() {
		return contentFigure;
	}

	@Override
	public void refresh() {
		super.refresh();
		getHandlerLinker().refreshHandlerLinks();
	}

	private InvokeHandlerLinker getHandlerLinker() {
		if (handlerLinker == null) {
			handlerLinker = new InvokeHandlerLinker(this);
		}

		return handlerLinker;
	}

	@Override
	protected void clearConnections() {
		super.clearConnections();
		getHandlerLinker().clearHandlerConnections();
	}

	@Override
	protected void handleModelChanged() {
		super.handleModelChanged();

		this.showCH = getCompensationHandler() != null ? true : false;
		this.showFH = getFaultHandler() != null ? true : false;

		refresh();
	}

	public void switchLayout(boolean horizontal) {
		((AlignedFlowLayout) auxilaryFigure.getLayoutManager())
				.setHorizontal(!horizontal);

	}

}
