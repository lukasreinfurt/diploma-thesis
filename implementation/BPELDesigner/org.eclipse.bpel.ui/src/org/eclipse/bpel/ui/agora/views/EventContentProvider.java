package org.eclipse.bpel.ui.agora.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Realizes the IStructuredContentProvider implementation for the auditing view table.
 * 
 * @author hahnml
 *
 */
public class EventContentProvider implements IStructuredContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
            @SuppressWarnings("unchecked")
            List<EventMessage> events = (List<EventMessage>) inputElement;
            return events.toArray();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

}
