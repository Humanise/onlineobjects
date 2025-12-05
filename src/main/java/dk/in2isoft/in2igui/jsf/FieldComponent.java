package dk.in2isoft.in2igui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.ClassBuilder;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Strings;

@FacesComponent(FieldComponent.TYPE)
public class FieldComponent extends AbstractComponent {

	public static final String TYPE = "hui.field";

	private String label;
	private boolean large;

	public FieldComponent() {
		super(TYPE);
	}

	@Override
	public void restoreState(Object[] state) {
		label = (String) state[0];
		large = (Boolean) state[1];
	}

	@Override
	public Object[] saveState() {
		return new Object[] {
			label, large
		};
	}

	private boolean isAbove() {
		UIComponent parent = this.getParent();
		if (parent!=null && parent instanceof FieldsComponent) {
			return ((FieldsComponent) parent).isLabelsAbove();
		}
		return true;
	}

	@Override
	public void encodeBegin(FacesContext context, TagWriter writer) throws IOException {
		String label = getLabel(context);
		ClassBuilder cls = new ClassBuilder("hui_form_field");
		if (large) {
			cls.add("hui-large");
		}
		if (isAbove()) {
			writer.startDiv(cls);
			if (Strings.isNotBlank(label)) {
				writer.startElement("label").withClass("hui_form_field_label").text(label).endElement("label");
			}
		} else {
			writer.startElement("tr").withClass(cls);
			writer.startElement("th");
			writer.startElement("label").withClass("hui_form_field_label").text(label).endElement("label");
			writer.endElement("th");
			writer.startElement("td");
		}
	}

	@Override
	protected void encodeEnd(FacesContext context, TagWriter writer) throws IOException {
		if (isAbove()) {
			writer.endDiv();
		} else {
			writer.endElement("td").endElement("tr");
		}
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	private String getLabel(FacesContext context) {
		return Components.getBindingAsString(this, "label", label, context);
	}

	public boolean isLarge() {
		return large;
	}

	public void setLarge(boolean large) {
		this.large = large;
	}
}
