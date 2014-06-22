package org.eclipse.bpel.ui.util;

import java.util.List;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.ContainerReferenceVariable;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.DataSourceReferenceVariable;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.impl.BPELFactoryImpl;
import org.eclipse.bpel.ui.commands.InsertInContainerCommand;
import org.eclipse.emf.ecore.EObject;

/**
 * Creates a new assign activity (if needed) and assigns subsequently a fixed
 * value to a data container/data source reference variable.
 * 
 */
public class InitializeReferencesHelper {

  public static void initializeReference(Process process,
      ContainerReferenceVariable containerReferenceVariable) {
    if (process.getActivity() instanceof Sequence) {
      Sequence sequence = (Sequence) process.getActivity();
      List<Activity> activities = sequence.getActivities();
      Assign initializeReferences = null;
      Receive receive = null;
      for (Activity activity : activities) {
        if (activity instanceof Receive && receive != null){
          receive = (Receive) activity;
        }
        if (activity instanceof Assign && activity != null
            && activity.getName().equals("initializeReferences")) {
          initializeReferences = (Assign) activity;
          break;
        }
      }
      if (initializeReferences == null) {
        initializeReferences = BPELFactoryImpl.init().createAssign();
        initializeReferences.setName("initializeReferences");
        InsertInContainerCommand command = new InsertInContainerCommand(
            (EObject) process, (EObject) initializeReferences,
            (EObject) sequence);
        command.execute();
        sequence.getActivities().move(0, 1);    
      }
      List<Copy> copies = initializeReferences.getCopy();
      Copy bCopy = null;
      for (Copy copy : copies) {
        if (copy.getTo() != null && copy.getTo().getVariable().getName().equals(
            containerReferenceVariable.getName())) {
          bCopy = copy;
          break;
        }
      }
      if (bCopy == null) {
        bCopy = BPELFactoryImpl.init().createCopy();
        Variable bVariable = null;
        for (Variable variable : process.getVariables().getChildren()) {
          if (variable.getName().equals(containerReferenceVariable.getName())) {
            bVariable = variable;
          }
        }
        To to = BPELFactoryImpl.init().createTo();
        to.setVariable(bVariable);
        From from = BPELFactoryImpl.init().createFrom();
        from.setLiteral(containerReferenceVariable.getFrom().getLiteral());
        bCopy.setTo(to);
        bCopy.setFrom(from);
        initializeReferences.getCopy().add(bCopy);
      } else {
        bCopy.getFrom().setLiteral(
            containerReferenceVariable.getFrom().getLiteral());
      }
    }
  }
  
  public static void initializeReference(Process process,
      DataSourceReferenceVariable dataSourceReferenceVariable) {
    if (process.getActivity() instanceof Sequence) {
      Sequence sequence = (Sequence) process.getActivity();
      List<Activity> activities = sequence.getActivities();
      Assign initializeReferences = null;
      Receive receive = null;
      for (Activity activity : activities) {
        if (activity instanceof Receive && receive != null){
          receive = (Receive) activity;
        }
        if (activity instanceof Assign && activity != null
            && activity.getName().equals("initializeReferences")) {
          initializeReferences = (Assign) activity;
          break;
        }
      }
      if (initializeReferences == null) {
        initializeReferences = BPELFactoryImpl.init().createAssign();
        initializeReferences.setName("initializeReferences");
        InsertInContainerCommand command = new InsertInContainerCommand(
            (EObject) process, (EObject) initializeReferences,
            (EObject) sequence);
        command.execute();
        sequence.getActivities().move(0, 1);    
      }
      List<Copy> copies = initializeReferences.getCopy();
      Copy bCopy = null;
      for (Copy copy : copies) {
        if (copy.getTo() != null && copy.getTo().getVariable().getName().equals(
            dataSourceReferenceVariable.getName())) {
          bCopy = copy;
          break;
        }
      }
      if (bCopy == null) {
        bCopy = BPELFactoryImpl.init().createCopy();
        Variable bVariable = null;
        for (Variable variable : process.getVariables().getChildren()) {
          if (variable.getName().equals(dataSourceReferenceVariable.getName())) {
            bVariable = variable;
          }
        }
        To to = BPELFactoryImpl.init().createTo();
        to.setVariable(bVariable);
        From from = BPELFactoryImpl.init().createFrom();
        from.setLiteral(dataSourceReferenceVariable.getFrom().getLiteral());
        bCopy.setTo(to);
        bCopy.setFrom(from);
        initializeReferences.getCopy().add(bCopy);
      } else {
        bCopy.getFrom().setLiteral(
            dataSourceReferenceVariable.getFrom().getLiteral());
      }
    }
  }
}
