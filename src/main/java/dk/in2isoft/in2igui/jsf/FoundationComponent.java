package dk.in2isoft.in2igui.jsf;

import java.io.IOException;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.ScriptWriter;
import dk.in2isoft.commons.jsf.TagWriter;

@FacesComponent(value=FoundationComponent.TYPE)
@Dependencies(
	js = { "/hui/js/hui_animation.js", "/hui/js/Foundation.js" },
	css = { "/hui/css/foundation.css" },
	requires = { HUIComponent.class },
	uses = { SymbolComponent.class }
)
public class FoundationComponent extends AbstractComponent {

	public static final String TYPE = "hui.foundation";

	private String name;

	public FoundationComponent() {
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
		out.startDiv("hui_foundation").withId(id);

		out.startDiv("hui_foundation_overlay_toggle").endDiv();
		out.startDiv("hui_foundation_overlay");
		out.startDiv("hui_foundation_overlay_title");
		out.startSpan("hui_foundation_back").text("Back").endSpan();
		out.startSpan("hui_foundation_overlay_close").endSpan();
		out.endDiv();
		{
			out.startDiv("hui_foundation_navigation hui_context_sidebar");
			out.startDiv("hui_foundation_resize hui_foundation_resize_navigation").endDiv();
			UIComponent facet = getFacet("navigation");
			if (facet!=null) { facet.encodeAll(context); }
			out.endDiv();
		}
		{
			out.startDiv("hui_foundation_results");
			UIComponent facet = getFacet("results");
			if (facet!=null) { facet.encodeAll(context); }
			out.endDiv();
		}
		out.startDiv("hui_foundation_resize hui_foundation_resize_overlay").endDiv();
		out.endDiv();
		out.startDiv("hui_foundation_main");
		writeFacet(context, out, "actions");
		writeFacet(context, out, "content");
		out.endDiv();
		{
			out.startDiv("hui_foundation_details");
			out.startDiv("hui_foundation_details_toggle").endDiv();
			UIComponent facet = getFacet("details");
			if (facet!=null) { facet.encodeAll(context); }
			out.endDiv();
		}

		out.endDiv();

		ScriptWriter js = out.getScriptWriter().startScript();
		js.startNewObject("hui.ui.Foundation").property("element", id);
		String name = getName(context);
		if (name!=null) {
			js.comma().property("name", name);
		}
		js.endNewObject().endScript();
	}

	private void writeFacet(FacesContext context, TagWriter writer, String name) throws IOException {
		writer.startDiv("hui_foundation_"+name);
		UIComponent facet = getFacet(name);
		if (facet!=null) { facet.encodeAll(context); }
		writer.endDiv();
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
