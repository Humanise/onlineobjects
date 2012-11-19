package dk.in2isoft.onlineobjects.publishing.remoting;

import java.util.Collection;

import dk.in2isoft.onlineobjects.core.EndUserException;
import dk.in2isoft.onlineobjects.core.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.WebNode;
import dk.in2isoft.onlineobjects.model.WebPage;
import dk.in2isoft.onlineobjects.model.WebSite;
import dk.in2isoft.onlineobjects.model.util.ModelClassInfo;
import dk.in2isoft.onlineobjects.publishing.Document;
import dk.in2isoft.onlineobjects.services.WebModelService;
import dk.in2isoft.onlineobjects.ui.AbstractRemotingFacade;

public class PublishingRemotingFacade extends AbstractRemotingFacade {
	
	private WebModelService webModelService;

	public WebPageInfo getPageInfo(long pageId) throws EndUserException {
		WebPage page = modelService.get(WebPage.class, pageId);
		if (page==null) {
			throw new IllegalRequestException("Page not found");
		}
		WebNode node = modelService.getParent(page, WebNode.class);
		WebSite site = null;
		if (node!=null) {
			site = modelService.getParent(node, WebSite.class);
		}
		WebPageInfo info = new WebPageInfo();
		info.setPageTitle(page.getName());
		if (node!=null) {
			info.setNodeTitle(node.getName());
		}
		if (site!=null) {
			info.setSiteTitle(site.getName());
		}
		info.setTags(page.getPropertyValues(Property.KEY_COMMON_TAG));
		return info;
	}

	public void updatePageInfo(WebPageInfo info) throws EndUserException {
		WebPage page = modelService.get(WebPage.class, info.getId());
		page.setName(info.getPageTitle());
		page.overrideProperties(Property.KEY_COMMON_TAG, info.getTags());

		WebNode node = modelService.getParent(page, WebNode.class);
		node.setName(info.getNodeTitle());
		
		WebSite site = modelService.getParent(node, WebSite.class);
		site.setName(info.getSiteTitle());
		
		modelService.updateItem(page, getUserSession());
	}

	public void changePageTemplate(long pageId, String template) throws EndUserException {
		WebPage page = modelService.get(WebPage.class, pageId);
		page.overrideFirstProperty(WebPage.PROPERTY_TEMPLATE, template);
		modelService.updateItem(page, getUserSession());
	}
	
	public Collection<ModelClassInfo> getDocumentClasses() {
		return modelService.getClassInfo(Document.class);
	}
	
	public long createWebPage(long webSiteId,String template) throws EndUserException {
		Class<?> docClass = modelService.getModelClass(template);
		return webModelService.createWebPageOnSite(webSiteId, docClass, getUserSession());
	}

	public boolean deleteWebPage(long id) throws EndUserException {
		if (webModelService.isLastPageOnSite(id, getUserSession())) {
			throw new EndUserException("The last page of a site cannot be deleted", "lastPage");
		}
		webModelService.deleteWebPage(id,getUserSession());
		return true;
	}

	public boolean updateWebNode(long id, String name) throws EndUserException {
		WebNode node = modelService.get(WebNode.class, id);
		node.setName(name);
		modelService.updateItem(node, getUserSession());
		return true;
	}
	
	public void moveNodeUp(long id) throws EndUserException {
		WebNode node = modelService.get(WebNode.class, id);
		webModelService.moveNodeUp(node,getUserSession());
	}
	
	public void moveNodeDown(long id) throws EndUserException {
		WebNode node = modelService.get(WebNode.class, id);
		webModelService.moveNodeDown(node,getUserSession());
	}

	public void setWebModelService(WebModelService webModelService) {
		this.webModelService = webModelService;
	}

	public WebModelService getWebModelService() {
		return webModelService;
	}
}