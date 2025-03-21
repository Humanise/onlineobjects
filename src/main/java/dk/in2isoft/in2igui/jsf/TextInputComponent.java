package dk.in2isoft.in2igui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.ClassBuilder;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.ScriptWriter;
import dk.in2isoft.commons.jsf.TagWriter;

@Dependencies(js = { "/hui/js/TextInput.js" }, css = { "/hui/css/textinput.css" }, requires = { HUIComponent.class })
@FacesComponent(value = TextInputComponent.TYPE)
public class TextInputComponent extends AbstractComponent {

	static final String TYPE = "hui.textinput";

	private String name;
	private String key;
	private String inputName;
	private String autocomplete;
	private boolean secret;
	private String placeholder;
	private int width;
	private Integer maxHeight;
	private String value;
	private boolean adaptive = true;
	private boolean multiline;
	private boolean large;

	public TextInputComponent() {
		super(TYPE);
	}

	@Override
	public Object[] saveState() {
		return new Object[] { name, secret, placeholder, width, value, inputName, adaptive, multiline, maxHeight, large, autocomplete };
	}

	@Override
	public void restoreState(Object[] state) {
		name = (String) state[0];
		secret = (Boolean) state[1];
		placeholder = (String) state[2];
		width = (Integer) state[3];
		value = (String) state[4];
		inputName = (String) state[5];
		adaptive = (Boolean) state[6];
		multiline = (Boolean) state[7];
		maxHeight = (Integer) state[8];
		large = (Boolean) state[9];
		autocomplete = (String) state[10];
	}

	@Override
	public void encodeBegin(FacesContext context, TagWriter writer) throws IOException {
		String id = getClientId();
		String value = Components.getBindingAsString(this, "value", this.value, context);
		ClassBuilder cls = new ClassBuilder("hui_textinput");
		if (large) {
			cls.add("hui-large");
		}
		if (multiline) {
			writer.startElement("textarea").withClass(cls).withTestName(testName);
			writer.withId(id);
			if (width > 0) {
				writer.withStyle("width: " + width + "px;");
			}
			if (StringUtils.isNotBlank(placeholder)) {
				writer.withAttribute("placeholder", placeholder);
			}
			writer.write(value).endElement("textarea");
		} else {
			writer.startElement("input").withClass(cls).withTestName(testName);
			writer.withId(id);
			if (width > 0) {
				writer.withStyle("width: " + width + "px;");
			}
			if (StringUtils.isNotBlank(placeholder)) {
				writer.withAttribute("placeholder", placeholder);
			}
			if (secret) {
				writer.withAttribute("type", "password");
			}
			if (value != null) {
				writer.withAttribute("value", value);
			}
			if (inputName != null) {
				writer.withAttribute("name", inputName);
			}
			if (autocomplete != null) {
				writer.withAttribute("autocomplete", autocomplete);
			}
			writer.endElement("input");
		}
		ScriptWriter js = writer.getScriptWriter().startScript();
		js.startNewObject("hui.ui.TextInput").property("element", id);
		if (name != null) {
			js.comma().property("name", name);
		}
		if (key != null) {
			js.comma().property("key", key);
		}
		if (maxHeight != null) {
			js.comma().property("maxHeight", maxHeight);
		}
		js.endNewObject().endScript();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	public boolean isSecret() {
		return secret;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public String getInputName() {
		return inputName;
	}

	public void setAdaptive(boolean adaptive) {
		this.adaptive = adaptive;
	}

	public boolean isAdaptive() {
		return adaptive;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public boolean isMultiline() {
		return multiline;
	}

	public void setMultiline(boolean multiline) {
		this.multiline = multiline;
	}

	public void setMaxHeight(Integer maxHeight) {
		this.maxHeight = maxHeight;
	}

	public Integer getMaxHeight() {
		return maxHeight;
	}
	
	public boolean isLarge() {
		return large;
	}
	
	public void setLarge(boolean large) {
		this.large = large;
	}

	public String getAutocomplete() {
		return autocomplete;
	}

	public void setAutocomplete(String autocomplete) {
		this.autocomplete = autocomplete;
	}
}
