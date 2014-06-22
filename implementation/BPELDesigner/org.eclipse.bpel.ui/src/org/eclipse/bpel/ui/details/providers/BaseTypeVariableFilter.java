package org.eclipse.bpel.ui.details.providers;

import org.eclipse.bpel.model.ReferenceVariable;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.details.tree.ITreeNode;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.wst.wsdl.Message;
import org.eclipse.wst.wsdl.Part;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;

public class BaseTypeVariableFilter extends ViewerFilter implements IFilter {

  Message fMessage;
  XSDElementDeclaration fElementDeclaration;
  XSDTypeDefinition fTypeDefinition;

  /**
   * Brand new shiny variable filter.
   */
  public BaseTypeVariableFilter() {

  }

  /**
   * Clear the filter
   */

  public void clear() {
    fMessage = null;
    fElementDeclaration = null;
    fTypeDefinition = null;
  }

  /**
   * Set type of filter to be the message msg
   * 
   * @param msg
   *          the message
   */
  public void setType(Message msg) {
    fMessage = msg;
  }

  /**
   * Set the type of filter to be this element declaration.
   * 
   * @param decl
   */
  public void setType(XSDElementDeclaration decl) {
    fElementDeclaration = decl;
  }

  /**
   * Set this type of filter to be type definition.
   * 
   * @param typeDef
   */
  public void setType(XSDTypeDefinition typeDef) {
    fTypeDefinition = typeDef;
  }

  /**
   * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
   *      java.lang.Object, java.lang.Object)
   */
  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {

    if (element instanceof ITreeNode) {
      return select(((ITreeNode) element).getModelObject());
    }
    return select(element);
  }

  /**
   * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
   */

  public boolean select(Object toTest) {

    if (toTest == null) {
      return false;
    }

    if (Variable.class.isInstance(toTest)) {

      Variable v = (Variable) toTest;

      if (fMessage == null && fElementDeclaration == null
          && fTypeDefinition == null) {
        return true;
      }

      if (fMessage != null) {

        if (fMessage.equals(v.getMessageType())) {
          return true;
        }

        if (fMessage.getEParts().size() == 1) {
          Part part = (Part) fMessage.getEParts().get(0);
          XSDElementDeclaration decl = part.getElementDeclaration();
          if (decl != null && decl.equals(v.getXSDElement())) {
            return true;
          }
        }

      }

      if (fElementDeclaration != null) {
        if (fElementDeclaration.equals(v.getXSDElement())) {
          return true;
        }
      }

      if (fTypeDefinition != null) {
        XSDTypeDefinition xsdTypeDefinition = null;
        try {
          xsdTypeDefinition = v.getType().getBaseType();
        } catch (NullPointerException e) {
          // TODO Überprüfen, ob Variable überhaupt einen Basistyp hat
        }
        if (fTypeDefinition.equals(xsdTypeDefinition)) {
          return true;
        }
      }

      return false;
    }

    if (ReferenceVariable.class.isInstance(toTest)) {
      ReferenceVariable ref = (ReferenceVariable) toTest;

      if (fTypeDefinition == null) {
        return true;
      }

      if (fTypeDefinition != null) {
        if (fTypeDefinition.equals(ref.getValueType())) {
          return true;
        }
      }

      return false;
    }

    return true;
  }

}
