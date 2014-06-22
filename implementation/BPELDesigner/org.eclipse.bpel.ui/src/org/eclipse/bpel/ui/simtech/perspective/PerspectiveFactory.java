package org.eclipse.bpel.ui.simtech.perspective;

import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.apache.ode.processmanagement.view.ProcessManagementView;
import org.eclipse.bpel.ui.agora.views.AuditingView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;

import fragmentorcp.views.RepositoryView;

/**
 * @author hahnml
 *
 */
public class PerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout left = layout.createFolder("bpel.left", IPageLayout.LEFT,
				0.18f, editorArea);
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);
		left.addView(IPageLayout.ID_OUTLINE);

		if (PlatformUI.getWorkbench().getViewRegistry().find(RepositoryView.ID) != null) {
			layout.addView(RepositoryView.ID, IPageLayout.BOTTOM, 0.5f,
					"bpel.left");
		}

		IFolderLayout bottom = layout.createFolder("bpel.bottom",
				IPageLayout.BOTTOM, 0.68f, editorArea);
		bottom.addView(IPageLayout.ID_PROP_SHEET);
		bottom.addView(AuditingView.ID);
		bottom.addView(BreakpointManagementView.ID);
		
		// Add the process management view
		if (PlatformUI.getWorkbench().getViewRegistry().find(ProcessManagementView.ID) != null) {
			bottom.addView(ProcessManagementView.ID);
		}
	}

}
