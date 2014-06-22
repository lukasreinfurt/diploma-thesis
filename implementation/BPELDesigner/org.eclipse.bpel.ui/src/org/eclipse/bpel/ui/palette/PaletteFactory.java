package org.eclipse.bpel.ui.palette;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpel.common.ui.CommonUIPlugin;
import org.eclipse.bpel.common.ui.ICommonUIConstants;
import org.eclipse.bpel.common.ui.palette.GraphicalEditorWithPalette;
import org.eclipse.bpel.common.ui.palette.IPaletteProvider;
import org.eclipse.bpel.common.ui.palette.OrderedPaletteGroup;
import org.eclipse.bpel.common.ui.palette.PaletteCategory;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.terms.BPELTerms;
import org.eclipse.bpel.ui.BPELEditor;
import org.eclipse.bpel.ui.Messages;
import org.eclipse.bpel.ui.factories.UIObjectFactoryProvider;
import org.eclipse.bpel.ui.palette.simtech.FragmentUIObjectFactory;
import org.eclipse.bpel.ui.simtech.gateway.FragmentImpl;
import org.eclipse.bpel.ui.simtech.gateway.Gateway;
import org.eclipse.bpel.ui.util.BPELCreationToolEntry;
import org.eclipse.bpel.ui.util.ZoominToolEntry;
import org.eclipse.bpel.ui.util.ZoomoutToolEntry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import fragmentorcp.FragmentoPlugIn;

/**
 * Palette Factory ...
 * 
 * @author Michal Chmielewski (michal.chmielewski@oracle.com)
 * @date May 21, 2007
 */

@SuppressWarnings("nls")
public class PaletteFactory implements IPaletteProvider {

	private BPELEditor editor;
	
	
	/**
	 * Brand new shiny palette factory
	 */

	public PaletteFactory() {

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.bpel.common.ui.palette.IPaletteProvider#contributeItems(org.eclipse.gef.palette.PaletteRoot)
	 */
	public void contributeItems(PaletteRoot paletteRoot) {

		createTopControlPaletteEntries(paletteRoot);
		createBPELPaletteEntries(paletteRoot);
		createBottomControlPaletteEntries(paletteRoot);
	}

	void createBPELPaletteEntries(PaletteContainer palette) {

		BPELPackage bpelPackage = BPELPackage.eINSTANCE;
		UIObjectFactoryProvider provider = UIObjectFactoryProvider
				.getInstance();

		PaletteCategory basicActionsCategory = new PaletteCategory("Actions"); //$NON-NLS-1$
		basicActionsCategory.setOrder(10);
		basicActionsCategory.setCategoryId("bpel.actions");

		String term = BPELTerms.getString("Empty"); //$NON-NLS-1$

		basicActionsCategory.add(new BPELCreationToolEntry(term, term, provider
				.getFactoryFor(bpelPackage.getEmpty())));
		basicActionsCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Invoke_35, Messages.BPELEditor_Invoke_36,
				provider.getFactoryFor(bpelPackage.getInvoke())));
		basicActionsCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Receive_4, Messages.BPELEditor_Receive_5,
				provider.getFactoryFor(bpelPackage.getReceive())));
		basicActionsCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Reply_6, Messages.BPELEditor_Reply_7,
				provider.getFactoryFor(bpelPackage.getReply())));
		basicActionsCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_OpaqueActivity_1,
				Messages.BPELEditor_OpaqueActivity_2, provider
						.getFactoryFor(bpelPackage.getOpaqueActivity())));
		basicActionsCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Assign_10, Messages.BPELEditor_Assign_11,
				provider.getFactoryFor(bpelPackage.getAssign())));
		basicActionsCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Validate_XML_12,
				Messages.BPELEditor_Validate_XML_13, provider
						.getFactoryFor(bpelPackage.getValidate())));
		palette.add(basicActionsCategory);

		PaletteCategory controlCategory = new PaletteCategory("Control2"); //$NON-NLS-1$
		controlCategory.setOrder(20);
		controlCategory.setCategoryId("bpel.control");

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_If_17, Messages.BPELEditor_If_18, provider
						.getFactoryFor(bpelPackage.getIf())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Pick_8, Messages.BPELEditor_Pick_9,
				provider.getFactoryFor(bpelPackage.getPick())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_While_19, Messages.BPELEditor_While_20,
				provider.getFactoryFor(bpelPackage.getWhile())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_ForEach_19, Messages.BPELEditor_ForEach_20,
				provider.getFactoryFor(bpelPackage.getForEach())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Repeat_Until_42,
				Messages.BPELEditor_Repeat_Until_43, provider
						.getFactoryFor(bpelPackage.getRepeatUntil())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Wait_21, Messages.BPELEditor_Wait_22,
				provider.getFactoryFor(bpelPackage.getWait())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Sequence_23,
				Messages.BPELEditor_Sequence_24, provider
						.getFactoryFor(bpelPackage.getSequence())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Scope_1, Messages.BPELEditor_Scope_2,
				provider.getFactoryFor(bpelPackage.getScope())));

		controlCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Flow_1, Messages.BPELEditor_Flow_2,
				provider.getFactoryFor(bpelPackage.getFlow())));

		palette.add(controlCategory);

		PaletteCategory faultCategory = new PaletteCategory("Faults"); //$NON-NLS-1$
		faultCategory.setOrder(30);
		faultCategory.setCategoryId("bpel.faults");

		faultCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Exit_26, Messages.BPELEditor_Exit_27,
				provider.getFactoryFor(bpelPackage.getExit())));
		faultCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Throw_28, Messages.BPELEditor_Throw_29,
				provider.getFactoryFor(bpelPackage.getThrow())));
		faultCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_ReThrow_1, Messages.BPELEditor_ReThrow_2,
				provider.getFactoryFor(bpelPackage.getRethrow())));
		faultCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_Compensate_1,
				Messages.BPELEditor_Compensate_2, provider
						.getFactoryFor(bpelPackage.getCompensate())));
		faultCategory.add(new BPELCreationToolEntry(
				Messages.BPELEditor_CompensateScope_1,
				Messages.BPELEditor_CompensateScope_2, provider
						.getFactoryFor(bpelPackage.getCompensateScope())));

		palette.add(faultCategory);

		/*
		 * Insert new category "Fragments" to the palette and load 
		 * the fragments from the fragment export directory. 
		 * 
		 * @sonntamo
		 */
		PaletteCategory fragmentsCategory = new PaletteCategory("Fragments"); //$NON-NLS-1$
		fragmentsCategory.setOrder(40);
		fragmentsCategory.setCategoryId("bpel.fragments");
		palette.add(fragmentsCategory);
		registerFragment(fragmentsCategory);

	}

	void createTopControlPaletteEntries(PaletteRoot root) {
		OrderedPaletteGroup controlGroup = new OrderedPaletteGroup(
				Messages.BPELEditor_Top_Control_Group_37);
		controlGroup.setOrder(5);
		controlGroup.setCategoryId("bpel.top.control");

		SelectionToolEntry selectionTool = new SelectionToolEntry(
				Messages.BPELEditor_Selection_Tool_38);
		controlGroup.add(selectionTool);

		MarqueeToolEntry marqueeTool = new MarqueeToolEntry(
				Messages.BPELEditor_Marquee_Tool);
		controlGroup.add(marqueeTool);

		root.add(controlGroup);
		root.setDefaultEntry(selectionTool);
	}

	void createBottomControlPaletteEntries(PaletteContainer palette) {
		PaletteSeparator separator = new PaletteSeparator("separator");
		palette.add(separator);

		OrderedPaletteGroup controlGroup = new OrderedPaletteGroup(
				Messages.BPELEditor_Bottom_Control_Group_39);

		controlGroup.setOrder(200);
		controlGroup.setCategoryId("bpel.bottom.control");

		ZoominToolEntry zoomIn = new ZoominToolEntry(
				Messages.BPELEditor_Zoom_In_40);
		zoomIn.setSmallIcon(CommonUIPlugin.getDefault().getImageRegistry()
				.getDescriptor(ICommonUIConstants.ICON_ZOOM_IN_TOOL));
		controlGroup.add(zoomIn);
		ZoomoutToolEntry zoomOut = new ZoomoutToolEntry(
				Messages.BPELEditor_Zoom_Out_41);
		zoomOut.setSmallIcon(CommonUIPlugin.getDefault().getImageRegistry()
				.getDescriptor(ICommonUIConstants.ICON_ZOOM_OUT_TOOL));
		controlGroup.add(zoomOut);

		palette.add(controlGroup);
	}

	/**
	 * Default path of the fragment repository folder.
	 */
	String repositoryPath = System.getProperty("java.io.tmpdir");

	/**
	 * Fetches registered fragments from the local fragment repository and hooks
	 * all found fragments into the palette under "Fragments" category.
	 * 
	 * @author sonntamo
	 */
	@SuppressWarnings("rawtypes")
	private void registerFragment(PaletteCategory fragmentsCat) {

		repositoryPath = FragmentoPlugIn.getDefault().getPreferenceStore()
				.getString("FRAGMENTO_EXPORT_PATH");

		try {

			ArrayList<FragmentImpl> fragmentList = Gateway
					.getRegisteredFragments(repositoryPath);
			
			// remove all entries from the palette
			List list = fragmentsCat.getChildren();
			while (list.size() > 0) {
				list.remove(0);
			}

			// iterate over all registered fragments
			for (FragmentImpl fragment : fragmentList) {

				// the object factory is used to create a new
				// fragment instance when the fragment is dragged and dropped 
				// into a BPEL process			
				FragmentUIObjectFactory factory = new FragmentUIObjectFactory(
						editor, fragment);

				// We add the fragment to the "Fragments" category
				// of the palette
				if ("bpel.fragments".equals(fragmentsCat.getCategoryId())) {
					fragmentsCat.add(new BPELCreationToolEntry(fragment.getName(),
							fragment.getDescription(), factory));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

			Status status = new Status(IStatus.ERROR, "My Plug-in ID", 0,
					e.getMessage(), e);

			// Display the dialog
			ErrorDialog.openError(Display.getCurrent().getActiveShell(),
					"JFace Error",
					"Invocation of fragments registry went wrong.", status);
		}

	}

	@Override
	public void setEditor(GraphicalEditorWithPalette editor) {
		if (editor instanceof BPELEditor) {
			this.editor = (BPELEditor)editor;
		}
		
	}
}
