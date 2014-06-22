package org.eclipse.bpel.ui.wizards.simtech;

import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Variable;
import org.eclipse.wst.wsdl.Message;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;

/**
 * @author hahnml
 * 
 */
public class CompareUtils {

	public static boolean areVariablesEqual(Variable var1, Variable var2) {
		boolean equal = true;

		if (!isXSDTypeEqual(var1.getType(), var2.getType())) {
			equal = false;
		}

		if (!isMessageEqual(var1.getMessageType(), var2.getMessageType())) {
			equal = false;
		}

		if (!isXMLElementEqual(var1.getXSDElement(), var2.getXSDElement())) {
			equal = false;
		}

		if (var1.getType() != null && var2.getType() == null
				|| var1.getMessageType() != null
				&& var2.getMessageType() == null
				|| var1.getXSDElement() != null && var2.getXSDElement() == null) {
			// Variables do not have the same type mode,
			// e.g. one variable is declared with a
			// XSDTypeDefinition and the other variable with
			// a XMLElement.
			equal = false;
		}

		return equal;
	}

	public static boolean arePartnerLinksEqual(PartnerLink pLink1,
			PartnerLink pLink2) {
		boolean equal = true;

		if (pLink1.getMyRole() != null && pLink2.getMyRole() != null) {
			if (!pLink1.getMyRole().getName().equals(pLink2.getMyRole().getName())) {
				equal = false;
			}
		}
		
		if (pLink1.getPartnerRole() != null && pLink2.getPartnerRole() != null) {
			if (!pLink1.getPartnerRole().getName().equals(pLink2.getPartnerRole().getName())) {
				equal = false;
			}
		}
		
		return equal;
	}

	private static boolean isMessageEqual(Message mes1, Message mes2) {
		boolean equal = true;

		if (mes1 != null && mes2 != null) {
			if (!mes1.equals(mes2)) {
				equal = false;
				if (mes1.getQName().getLocalPart().equals(mes2.getQName().getLocalPart())) {
					//TODO: Check if it works for any scenario that only the local parts are equal
					equal = true;
				}
			}
		}

		return equal;
	}

	private static boolean isXMLElementEqual(XSDElementDeclaration xsd1,
			XSDElementDeclaration xsd2) {
		boolean equal = true;

		if (xsd1 != null && xsd2 != null) {
			if (!xsd1.equals(xsd2)) {
				equal = false;
			}
		}

		return equal;
	}

	private static boolean isXSDTypeEqual(XSDTypeDefinition type1,
			XSDTypeDefinition type2) {
		boolean equal = true;

		if (type1 != null && type2 != null && type1.getQName() != null) {
			if (!type1.getQName().equals(type2.getQName())) {
				equal = false;
			}
		}

		return equal;
	}
}
