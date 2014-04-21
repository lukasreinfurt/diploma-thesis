package org.simtech.bootware.webserviceclient;

public class Main {

	public static void main(String[] args) {
		try {
			HelloClient client = new HelloClient();
			client.callService("Test");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
