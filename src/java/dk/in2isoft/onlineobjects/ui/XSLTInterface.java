package dk.in2isoft.onlineobjects.ui;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;

import dk.in2isoft.commons.xml.XSLTUtil;
import dk.in2isoft.onlineobjects.core.EndUserException;

public abstract class XSLTInterface {

	protected static final String NAMESPACE_PAGE = "http://uri.onlineobjects.com/page/";

	public abstract File getStylesheet();
	
	public abstract Document getData();
	
	public void display(Request request) throws IOException, EndUserException {
		XSLTUtil.applyXSLT(this, request);
	}
}