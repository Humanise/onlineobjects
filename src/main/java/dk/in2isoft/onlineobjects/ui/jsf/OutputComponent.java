package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.TagWriter;

@FacesComponent(value = OutputComponent.FAMILY)
public class OutputComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.output";

	private String emptyText;
	private boolean lower;
	private boolean escape = true;
	private boolean breaks;
	private Object value;

	public OutputComponent() {
		super(FAMILY);
	}

	@Override
	public Object[] saveState() {
		return new Object[] { value, emptyText, lower, escape, breaks };
	}

	@Override
	public void restoreState(Object[] state) {
		value = state[0];
		emptyText = (String) state[1];
		lower = (Boolean) state[2];
		escape = (Boolean) state[3];
		breaks = (Boolean) state[4];
	}

	@Override
	protected void encodeBegin(FacesContext context, TagWriter writer) throws IOException {
		Object value = getValue(context);
		String text = value==null ? null : value.toString();
		if (emptyText!=null && StringUtils.isBlank(text)) {
			text = emptyText;
		}
		if (text!=null) {
			if (lower) {
				text = text.toLowerCase();
			}
			if (escape) {
				if (breaks) {
					String[] lines = text.split("\n");
					for (int i = 0; i < lines.length; i++) {
						String line = lines[i];
						if (i > 0) {
							writer.write("<br/>");
						}
						writer.text(line);
					}
				} else {
					writer.text(text);
				}
			} else {
				writer.write(text);
			}
		}
	}

	public void setEmptyText(String emptyText) {
		this.emptyText = emptyText;
	}

	public String getEmptyText() {
		return emptyText;
	}

	public boolean isLower() {
		return lower;
	}

	public void setLower(boolean lower) {
		this.lower = lower;
	}

	public void setBreaks(boolean breaks) {
		this.breaks = breaks;
	}

	public boolean isBreaks() {
		return breaks;
	}

	public boolean isEscape() {
		return escape;
	}

	public void setEscape(boolean escape) {
		this.escape = escape;
	}

	public Object getValue() {
		return value;
	}

	public Object getValue(FacesContext context) {
		return Components.getExpressionValue(this, "value", value, context);
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
