package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.File;
import java.io.IOException;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.context.FacesContext;

import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.DependencyGraph;
import dk.in2isoft.commons.jsf.ScriptWriter;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.DependencyService;

@FacesComponent(BodyComponent.TYPE)
@Dependencies(css={"/core/css/oo_body.css"},requires={OnlineObjectsComponent.class})
public class BodyComponent extends HtmlBody {

	public static final String TYPE = "onlineobjects.body";

	private boolean plain;
	private boolean light;

	public java.lang.String getStyleClass() {
		if (plain) {
			return null;
		}
		String styleClass = super.getStyleClass();
		String cls = "oo_body";
		if (light) {
			cls+=" oo_body-light";
		}
		if (cls!=null && Strings.isBlank(styleClass)) {
			return cls;
		}
		if (cls!=null && Strings.isNotBlank(styleClass)) {
			return cls+" "+styleClass;
		}
		return styleClass;

    }

	@Override
	public void encodeEnd(FacesContext context) throws IOException {

		ConfigurationService configurationService = Components.getBean(ConfigurationService.class);
		DependencyGraph graph = Components.getDependencyGraph(context);

		TagWriter out = new TagWriter(this, context);
		ScriptWriter writer = Components.getScriptWriter(context);
		String js = writer.toString();

		if (configurationService.isOptimizeAssets()) {

			DependencyService dependencyService = Components.getBean(DependencyService.class);

			if (Strings.isNotBlank(js)) {
				out.startScopedScript().write("hui.on(function() {").write(js).write("});").endScopedScript();
			}
			String scriptUrl = dependencyService.handleScripts(graph);
		 	out.startScript().withAttribute("async", "async").withAttribute("defer", "defer").src(scriptUrl).endScript();
		} else {

			for (String url : graph.getScripts()) {
			 	out.startScript().src(DependencyService.pathToUrl(url)).endScript();
			}
			File tail = configurationService.getFile(DependencyService.TAIL_PATH);
			if (tail.exists()) {
				String tailContents = Files.readString(tail);
			 	out.startScript().write(tailContents).endScript();
			}
			if (Strings.isNotBlank(js)) {
				out.startScopedScript().write(js).endScopedScript();
			}
		}
		out.flush();

		super.encodeEnd(context);
	}

	public boolean isPlain() {
		return plain;
	}

	public void setPlain(boolean plain) {
		this.plain = plain;
	}

	public boolean isLight() {
		return light;
	}

	public void setLight(boolean light) {
		this.light = light;
	}
}
