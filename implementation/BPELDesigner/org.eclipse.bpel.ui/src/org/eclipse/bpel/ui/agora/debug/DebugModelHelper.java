package org.eclipse.bpel.ui.agora.debug;

import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.bpel.debug.debugmodel.Breakpoint;
import org.eclipse.bpel.debug.debugmodel.Breakpoints;
import org.eclipse.bpel.debug.debugmodel.DebugFactory;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Compensate;
import org.eclipse.bpel.model.CompensateScope;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.Else;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.Exit;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.ExtensionActivity;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.OpaqueActivity;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Rethrow;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.TerminationHandler;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.Validate;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.While;
import org.eclipse.bpel.model.resource.BPELWriter;
import org.eclipse.bpel.model.util.BPELConstants;
import org.eclipse.bpel.model.util.BPELUtils;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class DebugModelHelper {

	/**
	 * Resolves the Breakpoint of a given activity xpath.
	 * 
	 * @param activityXPath
	 * @param breakpoints
	 *            the list of breakpoints in which should be searched (local or
	 *            global)
	 * @return the correct breakpoint for the given activity xPath. If none
	 *         exists a new empty Breakpoint with the given data will be
	 *         returned.
	 */
	public static Breakpoint getBreakpoint(String activityXPath,
			Breakpoints breakpoints) {
		Breakpoint result = null;

		for (Breakpoint breakpoint : breakpoints.getBreakpoint()) {
			if (breakpoint.getTargetXPath().equals(activityXPath)) {
				result = breakpoint;
				break;
			}
		}

		return result;
	}

	public static Breakpoint createNewDefaultBreakpoint(String activityXPath,
			String targetName) {
		Breakpoint breakpoint = DebugFactory.eINSTANCE.createBreakpoint();

		breakpoint.setName("");
		breakpoint.setEnabled(false);
		breakpoint.setTargetName(targetName);
		breakpoint.setTargetXPath(activityXPath);

		return breakpoint;
	}

	// TODO: Check all activity implementations which have a "getActivity"
	// method, if we need the contained activity or not.
	public static BPELExtensibleElement getElement(Object context) {
		if (context instanceof ElseIf)
			return ((ElseIf) context).getActivity();
		if (context instanceof Else)
			return ((Else) context).getActivity();
		if (context instanceof Catch)
			return ((Catch) context).getActivity();
		if (context instanceof CatchAll)
			return ((CatchAll) context).getActivity();
		if (context instanceof OnAlarm)
			return ((OnAlarm) context).getActivity();
		if (context instanceof OnMessage)
			return ((OnMessage) context).getActivity();
		if (context instanceof OnEvent)
			return ((OnEvent) context).getActivity();
		if (context instanceof FaultHandler)
			return ModelHelper.getCatchAll((FaultHandler) context);
		if (context instanceof CompensationHandler)
			return ((CompensationHandler) context).getActivity();
		if (context instanceof TerminationHandler)
			return ((TerminationHandler) context).getActivity();

		if (context instanceof ForEach)
			return ((ForEach) context);
		if (context instanceof Scope)
			return ((Scope) context);
		if (context instanceof If)
			return ((If) context);
		if (context instanceof While)
			return ((While) context);
		if (context instanceof RepeatUntil)
			return ((RepeatUntil) context);
		if (context instanceof Wait)
			return ((Wait) context);
		if (context instanceof Receive)
			return ((Receive) context);
		if (context instanceof Invoke)
			return ((Invoke) context);
		if (context instanceof Reply)
			return ((Reply) context);
		if (context instanceof Assign)
			return ((Assign) context);
		if (context instanceof Compensate)
			return ((Compensate) context);
		if (context instanceof CompensateScope)
			return ((CompensateScope) context);
		if (context instanceof Empty)
			return ((Empty) context);
		if (context instanceof Exit)
			return ((Exit) context);
		if (context instanceof Flow)
			return ((Flow) context);
		if (context instanceof Pick)
			return ((Pick) context);
		if (context instanceof OpaqueActivity)
			return ((OpaqueActivity) context);
		if (context instanceof Rethrow)
			return ((Rethrow) context);
		if (context instanceof Throw)
			return ((Throw) context);
		if (context instanceof Sequence)
			return ((Sequence) context);
		if (context instanceof Validate)
			return ((Validate) context);
		if (context instanceof ExtensionActivity)
			return ((ExtensionActivity) context);

		if (context instanceof Link)
			return ((Link) context);

		throw new IllegalArgumentException(
				"DebugModelHelper() - no activity for this context");
	}

	public static Element expression2XML(Expression expression,
			String elementName) {
		Element expressionElement = null;

		try {
			DocumentBuilderFactory documentBuilderFactory = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();

			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setValidating(false);
			DocumentBuilder builder = documentBuilderFactory
					.newDocumentBuilder();

			Document doc = builder.newDocument();

			ExpressionBPELWriter writer = new DebugModelHelper.ExpressionBPELWriter(
					doc);
			expressionElement = writer.expression2XML(expression, elementName);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return expressionElement;
	}

	private static class ExpressionBPELWriter extends BPELWriter {

		public ExpressionBPELWriter(Document doc) {
			super(null, doc);
		}

		@Override
		public Element expression2XML(Expression expression, String elementName) {
			Element expressionElement = createBPELElement(elementName);

			if (expression.getExpressionLanguage() != null) {
				expressionElement.setAttribute("expressionLanguage",
						expression.getExpressionLanguage());
			} else {
				// Set the expression language of the process
				expressionElement.setAttribute("expressionLanguage",
						MonitoringProvider.getInstance().getActiveEditor()
								.getProcess().getExpressionLanguage());
			}
			if (expression.getOpaque() != null) {
				expressionElement.setAttribute("opaque",
						BPELUtils.boolean2XML(expression.getOpaque()));
			}
			if (expression.getBody() != null) {
				Object body = expression.getBody();
				if (body instanceof ExtensibilityElement) {
					ExtensibilityElement extensibilityElement = (ExtensibilityElement) body;
					Element child = extensibilityElement2XML(extensibilityElement);
					if (child != null) {
						expressionElement.appendChild(child);
					}
				} else {
					Text text = this.document.createTextNode(expression.getBody().toString());
							
					expressionElement.appendChild(text);
				}
			}

			return expressionElement;
		}

		@Override
		public Element createBPELElement(String tagName) {

			String namespaceURI = null;

			if (getResource() != null) {
				namespaceURI = getResource().getNamespaceURI();
			} else {
				namespaceURI = BPELConstants.NAMESPACE;
			}

			if (namespaceURI != null) {
				List<String> prefixes = BPELUtils.getNamespaceMap(
						MonitoringProvider.getInstance().getActiveEditor()
								.getProcess()).getReverse(namespaceURI);
				if (!prefixes.isEmpty() && !prefixes.get(0).equals("")) {
					return this.document.createElementNS(namespaceURI,
							prefixes.get(0) + ":" + tagName);
				}
			}

			return this.document.createElement(tagName);
		}
	}
}
