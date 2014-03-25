package org.simtech.bootware.webservice;

import javax.jws.WebService;

@WebService(serviceName  = "Hello",
            portName     = "HelloPort"/*,
            wsdlLocation = "META-INF/version.wsdl"*/)
public class Hello {
	private String message = new String("Hello, ");

	public String sayHello(String name) {
		return message + name + ".";
	}
}
