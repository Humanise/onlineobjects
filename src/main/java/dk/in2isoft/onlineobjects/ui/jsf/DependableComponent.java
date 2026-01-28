package dk.in2isoft.onlineobjects.ui.jsf;

import jakarta.faces.context.FacesContext;

public interface DependableComponent {

	String[] getScripts(FacesContext context);

	String[] getStyles(FacesContext context);

	Class<?>[] getComponents(FacesContext context);
}
