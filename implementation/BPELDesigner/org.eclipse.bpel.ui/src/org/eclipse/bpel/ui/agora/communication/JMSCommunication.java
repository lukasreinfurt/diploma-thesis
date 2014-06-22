package org.eclipse.bpel.ui.agora.communication;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.bpel.ui.agora.provider.MonitoringProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Realizes all the JMS Functionality
 * 
 * 
 * @author aeichel
 * @author hahnml
 * 
 */
public class JMSCommunication {
	/**
	 * Singleton
	 */
	private static JMSCommunication instance;

	private String url = BPELUIPlugin.INSTANCE.getPreferenceStore().getString(
			"ACTIVE_MQ_URL");
	private boolean initialized;
	
	private Long msgID = 0L;

	TopicSubscriber subscriber = null;
	TopicConnectionFactory connectionFactory = null;
	TopicConnection connection = null;

	private Session inSession;
	private MessageProducer producer;
	private Destination tempDest;
	private MessageConsumer responseConsumer;
	private Connection inConnection;

	private Session requestSession;
	private MessageProducer requestProducer;
	private Destination requestTempDest;
	private MessageConsumer requestResponseConsumer;
	private Connection requestConnection;

	private String exceptionMessage = "";

	public void startup() {
		if (!initialized) {
			// Set up a simple configuration that logs on the console.
			BasicConfigurator.configure();

			url = BPELUIPlugin.INSTANCE.getPreferenceStore().getString(
					"ACTIVE_MQ_URL");

			try {
				initialized = false;
				/**
				 * Engine Out
				 */

				connectionFactory = new ActiveMQConnectionFactory(url);
				connection = connectionFactory.createTopicConnection();
				TopicSession session = connection.createTopicSession(false,
						TopicSession.AUTO_ACKNOWLEDGE);
				Topic topic = session.createTopic("org.apache.ode.events");
				subscriber = session.createSubscriber(topic);
				
				//@hahnml: Register the {@link EngineOutputMessageDispatcher} instance with the subscriber
				this.setEngineOutputReceiver(MonitoringProvider.getInstance().getDispatcher());

				/**
				 * Engine Input
				 */

				ActiveMQConnectionFactory inConnectionFactory = new ActiveMQConnectionFactory(
						url);
				inConnection = inConnectionFactory.createConnection();
				inSession = inConnection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				Destination queue = inSession.createQueue("org.apache.ode.in");
				producer = inSession.createProducer(queue);

				tempDest = inSession.createTemporaryQueue();
				responseConsumer = inSession.createConsumer(tempDest);

				/**
				 * Request Execution History and send information from/to
				 * SimTech Auditing Application
				 */

				ActiveMQConnectionFactory requestConFactory = new ActiveMQConnectionFactory(
						url);
				requestConnection = requestConFactory.createConnection();
				requestSession = requestConnection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				Destination requestQueue = requestSession
						.createQueue("org.simTech.ode.eclipse");
				requestProducer = requestSession.createProducer(requestQueue);

				requestTempDest = requestSession.createTemporaryQueue();
				requestResponseConsumer = requestSession
						.createConsumer(requestTempDest);

				connection.start();
				inConnection.start();
				requestConnection.start();
				initialized = true;

				System.out.println("Connection established");

				// Register the RequestEventHandler
				setRequestResponseReceiver(new RequestEventHandler());
			} catch (Exception ex) {

				exceptionMessage = ex.getMessage();

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog
								.openError(
										Display.getDefault().getActiveShell(),
										"Error during connection on ActiveMQ",
										exceptionMessage
												+ "\n\n"
												+ "Perhaps the specified URL ("
												+ url
												+ ") is wrong or ActiveMQ is not running.");
					}

				});

				Logger.getLogger(JMSCommunication.class.getName()).log(
						Level.SEVERE, exceptionMessage);
			}
		}
	}

	public void shutdown() {
		try {
			if (connection != null) {
				connection.stop();
				connection.close();
				connection = null;

			}
			if (inConnection != null) {
				inConnection.stop();
				inConnection.close();
				inConnection = null;
			}
			if (requestConnection != null) {
				requestConnection.stop();
				requestConnection.close();
				requestConnection = null;
			}

			System.out.println("Connection closed");
			initialized = false;
		} catch (JMSException ex) {
			Logger.getLogger(JMSCommunication.class.getName()).log(
					Level.SEVERE, ex.getMessage());
		}
		connectionFactory = null;
	}

	public void restart() {
		shutdown();
		startup();
	}

	public static JMSCommunication getInstance() {
		if (instance == null) {
			instance = new JMSCommunication();
		}
		return instance;
	}

	public void send(Serializable s) {
		if (!initialized) {
			return;
		}
		try {
			ObjectMessage om = inSession.createObjectMessage(s);
			om.setJMSReplyTo(tempDest);
			producer.send(om);
		} catch (JMSException ex) {
			Logger.getLogger(JMSCommunication.class.getName()).log(
					Level.SEVERE, ex.getMessage());
		}
	}

	/**
	 * Sends a request to a listening SimTech Auditing Application.
	 */
	public void sendRequest(Serializable s) {
		if (!initialized) {
			return;
		}
		try {
			if (BPELUIPlugin.INSTANCE.getPreferenceStore().getBoolean(
					"SEND_REQUESTS")) {
				ObjectMessage om = requestSession.createObjectMessage(s);
				om.setJMSReplyTo(requestTempDest);
				requestProducer.send(om);
			}
		} catch (JMSException ex) {
			Logger.getLogger(JMSCommunication.class.getName()).log(
					Level.SEVERE, ex.getMessage());
		}
	}

	public void setEngineOutputReceiver(MessageListener ml) {
		if (ml != null && subscriber != null) {
			try {
				subscriber.setMessageListener(ml);
			} catch (JMSException ex) {
				Logger.getLogger(JMSCommunication.class.getName()).log(
						Level.SEVERE, ex.getMessage());
			}
		}
	}

	public void setControllerInputReceiver(MessageListener ml) {
		if (ml != null && responseConsumer != null) {
			try {
				responseConsumer.setMessageListener(ml);
			} catch (JMSException ex) {
				Logger.getLogger(JMSCommunication.class.getName()).log(
						Level.SEVERE, ex.getMessage());
			}
		}
	}

	public void setRequestResponseReceiver(MessageListener ml) {
		if (ml != null && requestResponseConsumer != null) {
			try {
				requestResponseConsumer.setMessageListener(ml);
			} catch (JMSException ex) {
				Logger.getLogger(JMSCommunication.class.getName()).log(
						Level.SEVERE, ex.getMessage());
			}
		}
	}
	
	/**
	 * @return Whether or not the connection to ActiveMQ is initialized.
	 */
	public boolean isInitialized() {
		return this.initialized;
	}
	
	public synchronized Long getMessageID() {
		msgID++;
		return msgID;
	}
}
