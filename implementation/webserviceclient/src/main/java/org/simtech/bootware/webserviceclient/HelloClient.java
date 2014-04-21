package org.simtech.bootware.webserviceclient;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import org.simtech.bootware.webservice.Hello;

public class HelloClient {
	public void callService(String arg) {
		try {
			URL url         = new URL("http://localhost:8080/axis2/services/Hello?wsdl");
			QName qname     = new QName("http://webservice.bootware.simtech.org/", "HelloImplService");

			Service service = Service.create(url, qname);
			Hello hello     = service.getPort(Hello.class);
			String response = hello.sayHello(arg);

			System.out.println(response);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
