package org.eclipse.bpel.ui.agora.debug.views.handlers;

import java.util.Iterator;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.BreakpointStateEnum;
import org.eclipse.bpel.ui.agora.debug.views.BreakpointManagementView;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class StepBreakpointHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof BreakpointManagementView) {
			BreakpointManagementView view = (BreakpointManagementView) page
					.findView(BreakpointManagementView.ID);
			ISelection selection = view.getSite().getSelectionProvider()
					.getSelection();

			if (selection != null && selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;

				for (Iterator<Breakpoint> iterator = sel.iterator(); iterator
						.hasNext();) {
					Breakpoint currentBreakpoint = iterator.next();

					if (currentBreakpoint.getState() == BreakpointStateEnum.BLOCKING) {
						MonitoringProvider.getInstance()
								.getActiveProcessManager().getDebugManager()
								.releaseBlockingEvent(currentBreakpoint);
					}
				}
			}
		}

		return null;
	}

}
