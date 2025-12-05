package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.in2igui.jsf.PreloaderComponent;

@FacesComponent(value=PhotoViewerComponent.FAMILY)
@Dependencies(
	js = {"/core/js/oo_photoviewer.js"},
	css = {"/core/css/oo_photoviewer.css"},
	requires = {OnlineObjectsComponent.class},
	uses = {PreloaderComponent.class}
)
public class PhotoViewerComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.photoviewer";

	public PhotoViewerComponent() {
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
