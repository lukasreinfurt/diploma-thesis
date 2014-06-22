/**
 * 
 */
package org.eclipse.bpel.ui.agora.communication;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.ode.bpel.extensions.comm.messages.engineOut.RegisterResponseMessage;
import org.apache.ode.bpel.extensions.comm.messages.engineOut.Variable_Read;

/**
 * @author aeichel
 *
 * Unused at the moment!
 */
public class ControllerInputEventHandler implements MessageListener {

	public ControllerInputEventHandler(){
		JMSCommunication.getInstance().setControllerInputReceiver(this);
	}
	
	@Override
	public void onMessage(Message msg) {
		if (!(msg instanceof ObjectMessage)) {
            System.out.println("No ObjectMessage");
            return;
        }

        /**
         * Objekt aus Message holen
         */
        ObjectMessage oMsg = (ObjectMessage)msg;
        Serializable obj = null;
        try {
            obj = oMsg.getObject();
        } catch (JMSException ex) {
            Logger.getLogger(ControllerInputEventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (obj == null) {
            System.out.println("obj == null");
            return;
        }


        if (obj instanceof Variable_Read) {
        	// TODO What to do with that?
        } else if (obj instanceof RegisterResponseMessage){
        	// TODO What to do with that?
        }

		
	}

}
