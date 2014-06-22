package org.eclipse.bpel.ui.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ode.bpel.modelChange.ChangeDocument;
import org.apache.ode.bpel.modelChange.ChangeType;
import org.apache.ode.bpel.modelChange.ReasonType;
import org.apache.ode.bpel.modelChange.TProcessChange;
import org.apache.ode.bpel.modelChange.UpdateChangeType;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELPackage;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.agora.manager.XPathMapper;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.compare.diff.metamodel.AttributeChange;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
import org.eclipse.emf.compare.diff.metamodel.UpdateModelElement;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.ecore.EObject;

public class ModelDiffHelper {

	public static void calculateModelDifferences(Process originalModel,
			Process currentModel, String folderPath, long instanceID) {

		HashMap<String, ChangeType> buffer = new HashMap<String, ChangeType>();
		HashMap<String, String> movedXPathes = new HashMap<String, String>();

		ChangeDocument changeDoc = ChangeDocument.Factory.newInstance();
		TProcessChange changes = changeDoc.addNewChange();

		changes.setInstanceID(instanceID);

		try {
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(MatchOptions.OPTION_DISTINCT_METAMODELS, false);

			MatchModel match = MatchService.doContentMatch(currentModel,
					originalModel, options);

			DiffModel diff = DiffService.doDiff(match, false);

			EList<DiffElement> differences = diff.getDifferences();

			for (DiffElement diffElement : differences) {

				switch (diffElement.getKind()) {
				case ADDITION:

					if (diffElement instanceof ModelElementChangeLeftTarget) {
						ModelElementChangeLeftTarget modelChange = (ModelElementChangeLeftTarget) diffElement;

						ChangeType change = changes.addNewChange();
						change.setChangeReason(ReasonType.ADDED);

						if (modelChange.getLeftElement() instanceof BPELExtensibleElement) {
							BPELExtensibleElement element = (BPELExtensibleElement) modelChange
									.getLeftElement();

							String name = "";
							String xPath = element.getXPath();
							String data = "";

							if (element instanceof Activity) {
								name = ((Activity) element).getName();
							} else if (element instanceof Variable) {
								name = ((Variable) element).getName();
							} else if (element instanceof PartnerLink) {
								name = ((PartnerLink) element).getName();
							} else if (element instanceof MessageExchange) {
								name = ((MessageExchange) element).getName();
							} else if (element instanceof Link) {
								name = ((Link) element).getName();
							} else if (element instanceof CorrelationSet) {
								name = ((CorrelationSet) element).getName();
							}

							change.setElementName(name);
							change.setElementXPath(xPath);
							change.setElementData(data);

							// Check if we got a dangling change
							if (buffer.containsKey(xPath)
									&& buffer.get(xPath).getChangeReason() == ReasonType.DELETED) {
								// Remove both changes from the document
								changes.getChangeList().remove(change);
								changes.getChangeList().remove(
										buffer.get(xPath));
							} else {
								buffer.put(xPath, change);
							}

						} else {
							changes.getChangeList().remove(change);
						}
					}

					break;
				case DELETION:

					if (diffElement instanceof ModelElementChangeRightTarget) {
						ModelElementChangeRightTarget modelChange = (ModelElementChangeRightTarget) diffElement;

						ChangeType change = changes.addNewChange();
						change.setChangeReason(ReasonType.DELETED);

						if (modelChange.getRightElement() instanceof BPELExtensibleElement) {
							BPELExtensibleElement element = (BPELExtensibleElement) modelChange
									.getRightElement();

							String name = "";
							String xPath = element.getXPath();
							String data = "";

							if (element instanceof Activity) {
								name = ((Activity) element).getName();
							} else if (element instanceof Variable) {
								name = ((Variable) element).getName();
							} else if (element instanceof PartnerLink) {
								name = ((PartnerLink) element).getName();
							} else if (element instanceof MessageExchange) {
								name = ((MessageExchange) element).getName();
							} else if (element instanceof Link) {
								name = ((Link) element).getName();
							} else if (element instanceof CorrelationSet) {
								name = ((CorrelationSet) element).getName();
							}

							change.setElementName(name);
							change.setElementXPath(xPath);
							change.setElementData(data);

							// Check if we got a dangling change
							if (buffer.containsKey(xPath)
									&& buffer.get(xPath).getChangeReason() == ReasonType.ADDED) {
								// Remove both changes from the document
								changes.getChangeList().remove(change);
								changes.getChangeList().remove(
										buffer.get(xPath));
							} else {
								buffer.put(xPath, change);
							}

						} else {
							changes.getChangeList().remove(change);
						}
					}

					break;
				case CHANGE:

					if (diffElement instanceof AttributeChange) {
						AttributeChange attribChange = (AttributeChange) diffElement;

						// Check if both elements are from type
						// BPELExtensibleElement and if the changed attribute is
						// the
						// xPath attribute.
						if (attribChange.getRightElement() instanceof BPELExtensibleElement
								&& attribChange.getLeftElement() instanceof BPELExtensibleElement
								&& attribChange.getAttribute() == BPELPackage.eINSTANCE
										.getBPELExtensibleElement_XPath()) {

							UpdateChangeType change = UpdateChangeType.Factory
									.newInstance();
							change.setChangeReason(ReasonType.XPATH_CHANGED);

							BPELExtensibleElement originalElement = (BPELExtensibleElement) attribChange
									.getRightElement();
							BPELExtensibleElement changedElement = (BPELExtensibleElement) attribChange
									.getLeftElement();

							String originalName = "";
							String changedName = "";
							String originalXPath = originalElement.getXPath();
							String changedXPath = changedElement.getXPath();
							String originalData = "";
							String changedData = "";

							if (originalElement instanceof Activity) {
								originalName = ((Activity) originalElement)
										.getName();
							} else if (originalElement instanceof Variable) {
								originalName = ((Variable) originalElement)
										.getName();
							} else if (originalElement instanceof PartnerLink) {
								originalName = ((PartnerLink) originalElement)
										.getName();
							} else if (originalElement instanceof MessageExchange) {
								originalName = ((MessageExchange) originalElement)
										.getName();
							} else if (originalElement instanceof Link) {
								originalName = ((Link) originalElement)
										.getName();
							} else if (originalElement instanceof CorrelationSet) {
								originalName = ((CorrelationSet) originalElement)
										.getName();
							}

							if (changedElement instanceof Activity) {
								changedName = ((Activity) changedElement)
										.getName();
							} else if (changedElement instanceof Variable) {
								changedName = ((Variable) changedElement)
										.getName();
							} else if (changedElement instanceof PartnerLink) {
								changedName = ((PartnerLink) changedElement)
										.getName();
							} else if (changedElement instanceof MessageExchange) {
								changedName = ((MessageExchange) changedElement)
										.getName();
							} else if (changedElement instanceof Link) {
								changedName = ((Link) changedElement).getName();
							} else if (changedElement instanceof CorrelationSet) {
								changedName = ((CorrelationSet) changedElement)
										.getName();
							}

							change.setUpdatedElementName(changedName);
							change.setUpdatedElementXPath(changedXPath);
							change.setUpdatedElementData(changedData);

							change.setElementName(originalName);
							change.setElementXPath(originalXPath);
							change.setElementData(originalData);

							changes.addNewChange().set(change);
						}
					}

					break;

				case MOVE:
					if (diffElement instanceof UpdateModelElement) {
						UpdateModelElement modelChange = (UpdateModelElement) diffElement;

						String deletedXPath = "";
						String addedXPath = "";

						// Register the deleted element
						ChangeType change1 = changes.addNewChange();
						change1.setChangeReason(ReasonType.DELETED);

						if (modelChange.getRightElement() instanceof BPELExtensibleElement) {
							BPELExtensibleElement element = (BPELExtensibleElement) modelChange
									.getRightElement();

							String name = "";
							String xPath = element.getXPath();
							String data = "";

							deletedXPath = xPath;

							if (element instanceof Activity) {
								name = ((Activity) element).getName();
							} else if (element instanceof Variable) {
								name = ((Variable) element).getName();
							} else if (element instanceof PartnerLink) {
								name = ((PartnerLink) element).getName();
							} else if (element instanceof MessageExchange) {
								name = ((MessageExchange) element).getName();
							} else if (element instanceof Link) {
								name = ((Link) element).getName();
							} else if (element instanceof CorrelationSet) {
								name = ((CorrelationSet) element).getName();
							}

							change1.setElementName(name);
							change1.setElementXPath(xPath);
							change1.setElementData(data);

						} else {
							changes.getChangeList().remove(change1);
						}

						// Register the added element
						ChangeType change2 = changes.addNewChange();
						change2.setChangeReason(ReasonType.ADDED);

						if (modelChange.getLeftElement() instanceof BPELExtensibleElement) {
							BPELExtensibleElement element = (BPELExtensibleElement) modelChange
									.getLeftElement();

							String name = "";
							String xPath = element.getXPath();
							String data = "";

							addedXPath = xPath;

							if (element instanceof Activity) {
								name = ((Activity) element).getName();
							} else if (element instanceof Variable) {
								name = ((Variable) element).getName();
							} else if (element instanceof PartnerLink) {
								name = ((PartnerLink) element).getName();
							} else if (element instanceof MessageExchange) {
								name = ((MessageExchange) element).getName();
							} else if (element instanceof Link) {
								name = ((Link) element).getName();
							} else if (element instanceof CorrelationSet) {
								name = ((CorrelationSet) element).getName();
							}

							change2.setElementName(name);
							change2.setElementXPath(xPath);
							change2.setElementData(data);

						} else {
							changes.getChangeList().remove(change2);
						}

						if (!deletedXPath.isEmpty() && !addedXPath.isEmpty()) {
							movedXPathes.put(deletedXPath, addedXPath);
						}
					}

					break;

				default:
					break;
				}
			}

			// Remove all xPath changes from the list of changes which are only
			// caused by "moving" an element
			List<ChangeType> elementsToRemove = new ArrayList<ChangeType>();
			if (!movedXPathes.isEmpty()) {
				for (ChangeType change : changes.getChangeList()) {
					// Check if we have the correct change type
					if (change.getChangeReason() == ReasonType.XPATH_CHANGED) {
						// Cast the change to the specialized class
						UpdateChangeType updateChange = (UpdateChangeType) change;
						// Check if the original xpath is in the movedXPathes
						// map
						if (movedXPathes.containsKey(updateChange
								.getElementXPath())) {
							// Check if the updated xpath corresponds to the one
							// of the map
							if (movedXPathes
									.get(updateChange.getElementXPath())
									.equals(updateChange
											.getUpdatedElementXPath())) {
								// Add the change to the list to remove it
								elementsToRemove.add(updateChange);
							}
						}
					}
				}
			}

			// Remove the invalid registered changes from the list
			changes.getChangeList().removeAll(elementsToRemove);

			// Add the XPath values of all elements contained by a changed
			// element
			for (ChangeType entry : changes.getChangeList()) {
				BPELExtensibleElement element = XPathMapper.handleXPath(
						entry.getElementXPath(), currentModel);
				if (element != null) {
					handleChildElements(element, entry.getChangeReason(),
							changes);
				}
			}

			File changeFile = new File(folderPath + Path.SEPARATOR
					+ "modelChanges.xml");

			if (changeFile.exists()) {
				changeFile.delete();
			}

			changeDoc.save(changeFile);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void handleChildElements(BPELExtensibleElement startObject,
			ReasonType.Enum type, TProcessChange changes) {
		TreeIterator<EObject> iter = startObject.eAllContents();

		while (iter.hasNext()) {
			EObject object = iter.next();

			if (object instanceof BPELExtensibleElement) {
				BPELExtensibleElement extObject = (BPELExtensibleElement) object;

				ChangeType change = changes.addNewChange();
				change.setChangeReason(type);

				String name = "";
				String xPath = extObject.getXPath();
				String data = "";

				if (extObject instanceof Activity) {
					name = ((Activity) extObject).getName();
				} else if (extObject instanceof Variable) {
					name = ((Variable) extObject).getName();
				} else if (extObject instanceof PartnerLink) {
					name = ((PartnerLink) extObject).getName();
				} else if (extObject instanceof MessageExchange) {
					name = ((MessageExchange) extObject).getName();
				} else if (extObject instanceof Link) {
					name = ((Link) extObject).getName();
				} else if (extObject instanceof CorrelationSet) {
					name = ((CorrelationSet) extObject).getName();
				}

				change.setElementName(name);
				change.setElementXPath(xPath);
				change.setElementData(data);
			}
		}

	}
}
