package dk.in2isoft.in2igui;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Interface {

	public void render(HttpServletRequest request, HttpServletResponse response) throws IOException;

	public File getFile();

	public void setParameter(String name, Object value);
}
