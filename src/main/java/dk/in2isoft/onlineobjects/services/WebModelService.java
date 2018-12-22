package dk.in2isoft.onlineobjects.services;

import java.util.List;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.WebNode;
import dk.in2isoft.onlineobjects.model.WebPage;
import dk.in2isoft.onlineobjects.model.WebSite;

public class WebModelService {
	
	private ModelService modelService;
	private PageRenderingService pageRenderingService;
	
	public WebPage getWebSiteFrontPage(WebSite site, Privileged privileged) throws ModelException {
		WebPage page = null;
		WebNode node = modelService.getChild(site, WebNode.class, privileged);
		if (node!=null) {
			page = modelService.getChild(node, WebPage.class, privileged);
		}
		return page;
	}

	public WebSite getUsersWebSite(User user, Privileged privileged) throws ModelException {
		WebSite site = modelService.getChild(user, WebSite.class, privileged);
		return site;
	}
	
	public WebSite getWebSiteOfPage(WebPage page, Privileged privileged) throws ModelException {
		WebNode node = modelService.getParent(page, WebNode.class, privileged);
		if (node==null) {
			return null;
		}
		WebSite site = modelService.getParent(node, WebSite.class, privileged);
		return site;
	}

	public WebPage getPageForWebNode(long id, Privileged privileged) throws ModelException {
		WebPage page = null;
		WebNode node = modelService.get(WebNode.class, id, privileged);
		if (node!=null) {
			page = modelService.getChild(node, WebPage.class, privileged);
		}
		return page;
	}
	
	public long createWebPageOnSite(long webSiteId, Class<? extends Entity> clazz, Privileged priviledged) throws EndUserException {
		WebSite site = modelService.get(WebSite.class, webSiteId, priviledged);
		
		// Create a web page
		WebPage page = new WebPage();
		page.setName("Min side");
		page.setTitle("Min side");
		modelService.create(page,priviledged);
		
		// Create a web node
		WebNode node = new WebNode();
		node.setName("Min side");
		modelService.create(node,priviledged);
		
		// Update positions of nodes
		List<Relation> relations = modelService.getRelationsFrom(site,WebNode.class, priviledged);
		int position = 1;
		for (Relation relation : relations) {
			relation.setPosition(position);
			modelService.update(relation, priviledged);
			position++;
		}
		
		// Create a relation between node and page
		Relation nodePageRelation = new Relation(node,page);
		modelService.create(nodePageRelation,priviledged);
		
		// Create a relation between site and node
		Relation siteNodeRelation = new Relation(site,node);
		siteNodeRelation.setPosition(position);
		modelService.create(siteNodeRelation,priviledged);
		
		Entity document = pageRenderingService.getBuilder(clazz).create(priviledged); 
		
		// Set gallery as content of page
		Relation pageDocumentRelation = new Relation(page,document);
		pageDocumentRelation.setKind(Relation.KIND_WEB_CONTENT);
		modelService.create(pageDocumentRelation,priviledged);
		
		return node.getId();
		
	}

	public void moveNodeUp(WebNode node, Privileged privileged) throws ModelException, SecurityException {

		WebSite site = modelService.getParent(node, WebSite.class, privileged);
		List<Relation> relations = modelService.getRelationsFrom(site,WebNode.class, privileged);
		int index = getIndexOfNode(relations,node);
		if (index>0) {
			Relation relation = relations.remove(index);
			relations.add(index-1, relation);
			updatePositions(relations, privileged);
		}
	}

	public void moveNodeDown(WebNode node, Privileged privileged) throws ModelException, SecurityException {

		WebSite site = modelService.getParent(node, WebSite.class, privileged);
		List<Relation> relations = modelService.getRelationsFrom(site, WebNode.class, privileged);
		int index = getIndexOfNode(relations,node);
		if (index<relations.size()-1) {
			Relation relation = relations.remove(index);
			relations.add(index+1, relation);
			updatePositions(relations, privileged);
		}
	}
	
	private void updatePositions(List<Relation> relations, Privileged priviledged) throws SecurityException, ModelException {
		int position = 1;
		for (Relation relation : relations) {
			relation.setPosition(position);
			modelService.update(relation, priviledged);
			position++;
		}
	}
	
	private int getIndexOfNode(List<Relation> relations, WebNode node) {
		for (int i	= 0; i < relations.size(); i++) {
			if (relations.get(i).getTo().getId()==node.getId()) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean isLastPageOnSite(long pageId,Privileged privileged) throws EndUserException {
		WebPage page = modelService.get(WebPage.class, pageId, privileged);
		if (page == null) {
			throw new EndUserException("The page does not exist");
		}
		
		WebNode node = modelService.getParent(page, WebNode.class, privileged);
		if (node==null) {
			throw new EndUserException("The page has no menu item");
		}
		WebSite site = modelService.getParent(node, WebSite.class, privileged);
		List<WebNode> nodes = modelService.getChildren(site, WebNode.class, privileged);
		return nodes.size()==1;
	}
	
	public void deleteWebPage(long pageId,Privileged privileged) throws EndUserException {
		WebPage page = modelService.get(WebPage.class, pageId, privileged);
		if (page == null) {
			throw new EndUserException("The page does not exist");
		}

		// Delete Nodes
		List<WebNode> nodes = modelService.getParents(page, WebNode.class, privileged);
		for (WebNode node : nodes) {
			modelService.delete(node,privileged);
		}

		// Delete page content
		List<Entity> contents = modelService.getChildren(page, Relation.KIND_WEB_CONTENT, privileged);
		for (Entity content : contents) {
			modelService.delete(content, privileged);
		}
		
		modelService.delete(page,privileged);
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}
	
	public void setPageRenderingService(PageRenderingService pageRenderingService) {
		this.pageRenderingService = pageRenderingService;
	}
}