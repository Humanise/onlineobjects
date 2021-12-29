package dk.in2isoft.onlineobjects.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface ScriptCompressor {

	String compress(String js);

	void compress(Reader in, Writer out) throws IOException;

}