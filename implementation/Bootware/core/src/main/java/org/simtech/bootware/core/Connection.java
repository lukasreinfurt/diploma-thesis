package org.simtech.bootware.core;

import java.util.Map;

import org.simtech.bootware.core.exceptions.ConnectConnectionException;
import org.simtech.bootware.core.exceptions.DisconnectConnectionException;
import org.simtech.bootware.core.exceptions.ExecuteCommandException;
import org.simtech.bootware.core.exceptions.UploadFileException;

public interface Connection {

	void connect(Map<String, String> settings) throws ConnectConnectionException;
	void disconnect() throws DisconnectConnectionException;
	void execute(String command) throws ExecuteCommandException;
	void upload(String localFile, String remotePath) throws UploadFileException;

}
