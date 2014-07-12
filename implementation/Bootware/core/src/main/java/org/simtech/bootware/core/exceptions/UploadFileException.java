package org.simtech.bootware.core.exceptions;

@SuppressWarnings("serial")
public class UploadFileException extends CommunicationException {
	public UploadFileException() { super(); }
	public UploadFileException(final String message) { super(message); }
	public UploadFileException(final String message, final Throwable cause) { super(message, cause); }
	public UploadFileException(final Throwable cause) { super(cause); }
}
