package dk.in2isoft.in2igui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.TagWriter;

@FacesComponent(value=PreloaderComponent.FAMILY)
@Dependencies(js = { "/hui/js/hui_preloader.js" }, requires = { HUIComponent.class })
public class PreloaderComponent extends AbstractComponent {

	public static final String FAMILY = "hui.preloader";
		
	public PreloaderComponent() {
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
