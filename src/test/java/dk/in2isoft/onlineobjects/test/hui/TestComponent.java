package dk.in2isoft.onlineobjects.test.hui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Maps;
import com.sun.faces.config.WebConfiguration.DisableUnicodeEscaping;
import com.sun.faces.renderkit.html_basic.HtmlResponseWriter;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.xml.DOM;
import dk.in2isoft.in2igui.jsf.ButtonComponent;
import dk.in2isoft.in2igui.jsf.LocationInputComponent;
import dk.in2isoft.in2igui.jsf.TextInputComponent;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.ui.HUIService;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations="classpath:applicationContext.test.xml")
@PrepareForTest({ FacesContext.class })
@Ignore
public class TestComponent extends AbstractSpringTestCase {

	private static final Logger log = LogManager.getLogger(TestComponent.class);
	
	@Autowired
	private HUIService HUIService;

	public String render(UIComponent component) throws IOException {
	    // mock all static methods of FacesContext
	    PowerMock.mockStatic(FacesContext.class);

	    FacesContext context = EasyMock.createMock(FacesContext.class);
	    ExternalContext ext = EasyMock.createMock(ExternalContext.class);
	    HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
	    PartialViewContext partialViewContext = EasyMock.createMock(PartialViewContext.class);

	    EasyMock.expect(FacesContext.getCurrentInstance()).andReturn(context).anyTimes();

	    EasyMock.expect(context.getExternalContext()).andReturn(ext).anyTimes();
	    Map<String, Object> appMap = Maps.newHashMap();
	    EasyMock.expect(ext.getApplicationMap()).andReturn(appMap).anyTimes();
		EasyMock.expect(ext.getRequest()).andReturn(request).anyTimes();
		EasyMock.expect(ext.getRequestMap()).andReturn(appMap).anyTimes();
		EasyMock.expect(context.getPartialViewContext()).andReturn(partialViewContext).anyTimes();
		EasyMock.expect(request.getHeader(EasyMock.anyString())).andReturn("").anyTimes();
		EasyMock.expect(partialViewContext.isAjaxRequest()).andReturn(false).anyTimes();

		
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    PrintWriter printWriter = new PrintWriter(stream);
		ResponseWriter responseWriter = new HtmlResponseWriter(printWriter, "text/html", Strings.UTF8, false, true, DisableUnicodeEscaping.Auto, false);
		EasyMock.expect(context.getResponseWriter()).andReturn(responseWriter).anyTimes();

		PowerMock.replay(FacesContext.class);
	    EasyMock.replay(context, ext, request, partialViewContext);

	    component.encodeAll(context);

		responseWriter.flush();
		printWriter.flush();
		//PowerMock.verify(FacesContext.class);
		return new String(stream.toByteArray(), Strings.UTF8);

	}
	
	@Test
	public void testButton() throws IOException {
	    ButtonComponent button = new ButtonComponent();
		button.setId("myId");
		button.setText("Button text");
		
		String rendering = render(button);
		Assert.assertEquals("<a href=\"javascript://\" class=\"hui_button\" id=\"myId\">Button text</a>", rendering);

		String huiRendering = HUIService.renderFragment("<button text=\"Button text\"/>");

		Assert.assertEquals(clean(huiRendering),clean(rendering));
	}

	@Test
	public void testTextInput() throws IOException {
		TextInputComponent component = new TextInputComponent();
		component.setId("myId");
		
		String jsfRendering = render(component);
		Assert.assertEquals("<input class=\"hui_textinput\" id=\"myId\" />", jsfRendering);
				
		String huiRendering = HUIService.renderFragment("<text-input/>");
		
		String huiCleaned = clean(huiRendering);
		String jsfCleaned = clean(jsfRendering);

		Assert.assertEquals(huiCleaned, jsfCleaned);
		
	}

	@Test
	public void testLocationInput() throws IOException {
		LocationInputComponent component = new LocationInputComponent();
		component.setId("myId");
		
		String jsfRendering = render(component);
		Assert.assertEquals("<span class=\"hui_locationinput\" id=\"myId\"><span class=\"hui_locationinput_latitude\"><span><input /></span></span><span class=\"hui_locationinput_longitude\"><span><input /></span></span><a href=\"javascript://\" class=\"hui_locationinput_picker\"></a></span>", jsfRendering);
				
		String huiRendering = HUIService.renderFragment("<location-input/>");
		
		String huiCleaned = clean(huiRendering);
		String jsfCleaned = clean(jsfRendering);

		Assert.assertEquals(huiCleaned, jsfCleaned);
		
	}

	private String clean(String markup) {
		markup = markup.replaceAll("<![^>]+>", "");
		Document huiXOM = DOM.parseXOM("<div>" + markup + "</div>");
		strip(huiXOM.getRootElement());
		StringBuilder sb = new StringBuilder();
		Elements elements = huiXOM.getRootElement().getChildElements();
		for (int i = 0; i < elements.size(); i++) {
			sb.append(elements.get(i).toXML());
			
		}
		return sb.toString();
	}
	
	public void strip(Element element) {
		if (element.getLocalName().equals("script")) {
			element.detach();
		} else {
			int declarationCount = element.getNamespaceDeclarationCount();
			for (int i = declarationCount-1; i > -1; i--) {
				String namespacePrefix = element.getNamespacePrefix(i);
				element.removeNamespaceDeclaration(namespacePrefix);
			}
			element.setNamespaceURI(null);
			for (int i = element.getAttributeCount() - 1; i >= 0; i--) {
               Attribute attribute = element.getAttribute(i);
               log.debug(attribute.getLocalName());
               if (!"".equals(attribute.getNamespacePrefix())) {
            	   attribute.detach();
               }
               else if ("id".equals(attribute.getLocalName())) {
            	   attribute.detach();
               }
            }
			Elements elements = element.getChildElements();      
            for (int i = 0; i < elements.size(); i++) {
                strip(elements.get(i));
            }
		}
	}

	public void setHUIService(HUIService hUIService) {
		HUIService = hUIService;
	}
}
