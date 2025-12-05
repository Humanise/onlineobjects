package dk.in2isoft.onlineobjects.modules.networking;

import java.io.File;
import java.net.URI;

public class NetworkResponse {

	public enum State {SUCCESS,ERROR}

	private String mimeType;
	private String encoding;
	private File file;
	private State state;
	private URI uri;

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public boolean isSuccess() {
		return State.SUCCESS.equals(state);
	}

	public void cleanUp() {
		file.delete();
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}
}
