package org.simtech.bootware.core;

import java.util.Map;

import org.simtech.bootware.core.exceptions.ConnectConnectionException;

public interface Connection {

	void connect(Map<String, String> settings) throws ConnectConnectionException;
	void disconnect();
	void execute(String command);
	void upload(String localFile, String remotePath);

}
