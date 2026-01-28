package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.in2igui.jsf.HUIComponent;

@FacesComponent(value=OnlineObjectsComponent.FAMILY)
@Dependencies(
		js = { "/core/js/onlineobjects.js" },
		css = { "/core/css/common.css", "/core/css/oo_object.css" },
		requires = { HUIComponent.class })
public class OnlineObjectsComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.oo";

	public OnlineObjectsComponent() {
		super(FAMILY);
	}

	@Override
	public void restoreState(Object[] state) {
	}

	@Override
	public Object[] saveState() {
		return new Object[] { };
	}

	@Override
	protected void encodeBegin(FacesContext context, TagWriter writer) throws IOException {
	}

}
