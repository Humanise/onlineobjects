package dk.in2isoft.onlineobjects.test.compress;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mozilla.javascript.ErrorReporter;
import org.springframework.beans.factory.annotation.Autowired;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;
import dk.in2isoft.onlineobjects.ui.UglifyScriptCompressor;

@Category(EssentialTests.class)
public class TestCompression extends AbstractSpringTestCase {
	
	@Autowired
	UglifyScriptCompressor uglifyScriptCompressor;
	
	@Test
	public void testCompressCSS() throws EndUserException, IOException {
		Reader reader = new FileReader(getTestFile("compress/style.css"));
		CssCompressor compressor = new CssCompressor(reader);
		StringWriter writer = new StringWriter();
		compressor.compress(writer, 1);
		reader.close();
		assertEquals("body{background:red}", writer.toString());
	}

	@Test
	public void testCompressJS() throws EndUserException, IOException {
		Reader reader = new FileReader(getTestFile("compress/script.js"));
		ErrorReporter reporter = null;
		JavaScriptCompressor compressor = new JavaScriptCompressor(reader, reporter);
		StringWriter writer = new StringWriter();
		int linebreak = -1;
		boolean munge = true;
		boolean warn = false;
		boolean preserveAllSemiColons = false;
		boolean preserveStringLiterals = false;
		compressor.compress(writer, linebreak, munge, warn, preserveAllSemiColons, preserveStringLiterals);
		reader.close();
		assertEquals("var i=1;function x(a){var b=0;return b*2};", writer.toString());
	}

	@Test
	public void testMinifyFile() throws EndUserException, IOException {
		Reader reader = new FileReader(getTestFile("compress/script.js"));
		StringWriter writer = new StringWriter();
		uglifyScriptCompressor.compress(reader, writer);
		reader.close();
		assertEquals("var i=1;function x(n){return 0}", writer.toString());
	}

	@Test
	public void testMinifyString() throws EndUserException, IOException {
		String minified = uglifyScriptCompressor.compress("var i,c,d; function x(fjkasfjsdadljla) { return fjkasfjsdadljla * 0;};");
		assertEquals("var i,c,d;function x(n){return 0*n}\n", minified);
	}

	@Test
	public void testMinifyStringWithNoEffect() throws EndUserException, IOException {
		String js = "(function() {\n"
				+ "  var abe = 10.0000;\n"
				+ "  console.log(abe * 1000);\n"
				+ "});";
		String minified = uglifyScriptCompressor.compress(js);
		assertEquals("\n", minified);
	}
}