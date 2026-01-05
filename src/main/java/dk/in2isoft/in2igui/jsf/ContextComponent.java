package dk.in2isoft.in2igui.jsf;

import javax.faces.component.FacesComponent;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Dependencies;

@FacesComponent(value = ContextComponent.TYPE)
@Dependencies(
	js = { "/hui/js/Context.js", "/hui/js/hui_query.js"}, css = {"/hui/css/context.css"},
	uses = {PanelComponent.class}, requires = { HUIComponent.class }
)
public class ContextComponent extends AbstractComponent {

	public static final String TYPE = "hui.context";

	public ContextComponent() {
		super(TYPE);
	}

	@Override
	public void restoreState(Object[] state) {
	}

	@Override
	public Object[] saveState() {
		return new Object[] {};
	}

}
