package org.eclipse.bpel.ui.agora.views;

import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Realizes the ability to filter all events in the auditing view.
 * 
 * @author hahnml
 *
 */
public class EventFilter extends ViewerFilter{

	private String searchString;

    public void setSearchText(String s) {
            // Search must be a substring of the existing value
            this.searchString = ".*" + s + ".*";
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (searchString == null || searchString.length() == 0) {
                    return true;
            }
            EventMessage evt = (EventMessage) element;
            try {
                    if (evt.getEventType().matches(searchString)) {
                            return true;
                    }
                    if (evt.getSource().matches(searchString)) {
                            return true;
                    }
                    if (evt.getTimestamp().toString().matches(searchString)) {
                            return true;
                    }
                    if (evt.getState() != null ? evt.getState().name().matches(searchString) : false) {
                            return true;
                    }
            }catch (PatternSyntaxException e) {
                    e.printStackTrace();
            }
            return false;
    }

}
