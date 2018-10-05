package dk.in2isoft.onlineobjects.ui.jsf;

import java.io.File;
import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import com.sun.faces.component.visit.FullVisitContext;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.DependencyGraph;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.DependencyService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.ScriptCompressor;

@FacesComponent(value = HeadComponent.FAMILY)
public class HeadComponent extends AbstractComponent {

	public static final String FAMILY = "onlineobjects.head";
	
	private String inlineJs;

	//private static final Logger log = LogManager.getLogger(HeadComponent.class);

	public HeadComponent() {
		super(FAMILY);
	}

	@Override
	public void restoreState(Object[] state) {
	}

	@Override
	public Object[] saveState() {
		return new Object[] {};
	}

	@Override
	protected void encodeBegin(FacesContext context, TagWriter out) throws IOException {
		out.startElement("head");
		out.startElement("meta").withAttribute("http-equiv", "Content-Type").withAttribute("content", "text/html; charset=utf-8").endElement("meta");
		out.startElement("meta").withAttribute("name", "viewport").withAttribute("content", "user-scalable=yes, width=device-width, initial-scale = 1, maximum-scale = 10, minimum-scale = 0.2").endElement("meta");
		String application = Components.getRequest().getApplication();
		String icon = "words".equals(application) ? "Words" : "OnlineObjects";
		int[] sizes = {29,40,58,60,76,80,87,120,152,180,512,1024};
		for (int size : sizes) {
			out.startElement("link").withAttribute("rel", "apple-touch-icon");
			out.withAttribute("sizes", size+"x"+size);
			out.withHref("/core/gfx/icons/iOS/"+icon+"-" + size + ".png");
			out.endElement("link");
		}
	}

	@Override
	protected void encodeEnd(FacesContext context, TagWriter out) throws IOException {
		DependencyGraph graph = Components.getDependencyGraph(context);

		context.getViewRoot().visitTree(new FullVisitContext(context), (visitContext, component) -> {
			visit(component.getClass(), component, graph, context);
			return VisitResult.ACCEPT;
		});
		ConfigurationService configurationService = getBean(ConfigurationService.class);
		
		
		Request request = Components.getRequest();
		if (!configurationService.isOptimizeResources()) {
			for (String url : graph.getStyles()) {
			 	out.startElement("link").rel("stylesheet").type("text/css").href(url).endElement("link");
			}
		 	writeInlineJs(configurationService, out);
		} else {
			DependencyService dependencyService = getBean(DependencyService.class);

			String styleUrl = dependencyService.handleStyles(graph);
		 	out.startElement("link").rel("stylesheet").type("text/css").href(styleUrl).endElement("link");
			
		 	writeInlineJs(configurationService, out);
		}
		out.write("<!--[if IE 8]><link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getBaseContext() + "/hui/css/msie8.css\"></link><![endif]-->");
		out.write("<!--[if IE 7]><link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getBaseContext() + "/hui/css/msie7.css\"></link><![endif]-->");
		out.write("<!--[if lt IE 7]><link rel=\"stylesheet\" type=\"text/css\" href=\"" + request.getBaseContext() + "/hui/css/msie6.css\"></link><![endif]-->");
		out.write("<!--[if lt IE 9]><script type=\"text/javascript\" src=\"" + request.getBaseContext() + "/hui/bin/compatibility.min.js\"></script><![endif]-->");
		out.newLine().startElement("script").withAttribute("data-hui-context", request.getBaseContext()).withAttribute("data-hui-lang", request.getLanguage()).endElement("script");

		out.startScript().newLine();
		
		out.write("window.oo = window.oo || {};").newLine();
		if (StringUtils.isNotBlank(request.getLanguage())) {
			out.write("oo.language = '").write(request.getLanguage()).write("';").newLine();
		}
		out.endElement("script").newLine();
		out.endElement("head");
	}
	
	private void writeInlineJs(ConfigurationService configurationService, TagWriter out) throws IOException {
		String content = getInline(configurationService);
		if (content!=null) {
			out.startScript().write(content).endScript();
		}
	}

	private String getInline(ConfigurationService configurationService) {
		if (inlineJs!=null && !configurationService.isDevelopmentMode()) {
			return inlineJs;
		}
		File file = configurationService.getFile("WEB-INF","core","web","js","inline.js");
		String content = null;
		if (file.exists()) {
			content = Files.readString(file);
			if (configurationService.isOptimizeResources()) {
				content = new ScriptCompressor().compress(content);
			}
		}
		inlineJs = content;
		return content;
	}

	private void visit(Class<?> componentClass, UIComponent componentInstance, DependencyGraph graph, FacesContext context) {
		if (componentInstance!=null && !componentInstance.isRendered()) {
			return;
		}
		if (!graph.isVisited(componentClass)) {
			graph.markVisited(componentClass);
			Dependencies annotation = componentClass.getAnnotation(Dependencies.class);
			if (annotation != null) {
				for (Class<? extends AbstractComponent> depComponentClass : annotation.requires()) {
					visit(depComponentClass, null, graph, context);
				}

				graph.addScripts(annotation.js());
				graph.addStyles(annotation.css());

				for (Class<? extends AbstractComponent> depComponentClass : annotation.uses()) {
					visit(depComponentClass, null, graph, context);
				}
			}
		}
		if (componentInstance instanceof DependableComponent) {
			DependableComponent dep = (DependableComponent) componentInstance;
			graph.addScripts(dep.getScripts(context));
			graph.addStyles(dep.getStyles(context));
			Class<?>[] components = dep.getComponents(context);
			if (components!=null) {
				for (Class<?> cls : components) {
					visit(cls, null, graph, context);
				}
			}
		}
	}
}
