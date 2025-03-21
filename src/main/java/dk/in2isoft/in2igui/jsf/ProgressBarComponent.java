package dk.in2isoft.in2igui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.TagWriter;

@FacesComponent(value=ProgressBarComponent.TYPE)
@Dependencies(
	js = {"/hui/js/ProgressBar.js"},
	css = {"/hui/css/progressbar.css"},
	requires = {HUIComponent.class}
)
public class ProgressBarComponent extends AbstractComponent {

	public static final String TYPE = "hui.progressbar";

	private String name;

	public ProgressBarComponent() {
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
		// TODO Not implemented yet
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getName(FacesContext context) {
		return Components.getExpressionValue(this, "name", name, context);
	}
	
}
