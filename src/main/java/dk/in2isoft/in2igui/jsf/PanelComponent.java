package dk.in2isoft.in2igui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.ClassBuilder;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.ScriptWriter;
import dk.in2isoft.commons.jsf.TagWriter;

@FacesComponent(value=PanelComponent.TYPE)
@Dependencies(js = { "/hui/js/hui_animation.js", "/hui/js/Panel.js" }, css = { "/hui/css/panel.css" }, requires = { HUIComponent.class, SymbolComponent.class })
public class PanelComponent extends AbstractComponent {

	public static final String TYPE = "hui.panel";

	private String name;

	public PanelComponent() {
		super(TYPE);
	}
	
	@Override
	public void restoreState(Object[] state) {
		name = (String) state[0];
	}

	@Override
	public Object[] saveState() {
		return new Object[] {
			name
		};
	}
	
	@Override
	public void encodeBegin(FacesContext context, TagWriter out) throws IOException {
		String id = getClientId();
		ClassBuilder cls = ClassBuilder.with("hui_panel");
		out.startDiv(cls).withId(id);
		out.startDiv("hui_panel_body");
	}
	
	@Override
	protected void encodeEnd(FacesContext context, TagWriter out) throws IOException {
		out.endDiv();
		out.endDiv();
		ScriptWriter js = out.getScriptWriter();
		js.startScript();
		js.startNewObject("hui.ui.Panel");
		js.property("element", getClientId());
		String name = getName(context);
		if (name!=null) {
			js.comma().property("name",name);
		}
		js.endNewObject();
		js.endScript();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getName(FacesContext context) {
		return Components.getBindingAsString(this, "name", name, context);
	}

}
