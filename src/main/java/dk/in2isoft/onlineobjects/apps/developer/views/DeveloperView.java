package dk.in2isoft.onlineobjects.apps.developer.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.PairSearchResult;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UsersPersonQuery;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;

public class DeveloperView extends AbstractView {
	
	private ModelService modelService;
	private SecurityService securityService;
	private ConfigurationService configurationService;

	private List<Item> users;
	private List<String> icons;

	@Override
	protected void before(Request request) throws Exception {
		// TODO Auto-generated method stub
		loadIcons();
	}
	
	public List<String> getIcons() {
		loadIcons();
		return icons;
	}

	private void loadIcons() {
		if (icons != null) return;
		File file = configurationService.getFile("WEB-INF","core","web","fonts","onlineobjects","selection.json");
		String json = Files.readString(file);
		Gson g = new Gson();
		icons = new ArrayList<>();
		Map<String, ?> parsed = g.fromJson(json, java.util.Map.class);
		List<Map<String,?>> object = (List<Map<String, ?>>) parsed.get("icons");
		for (Map<String, ?> icon : object) {
			Map<String,Object> props = (Map<String, Object>) icon.get("properties");
			icons.add(props.get("name").toString());
		}
	}

	public List<String> getModelTypeNames() {
		return modelService.getEntityClasses().stream().map(Class::getSimpleName).collect(Collectors.toList());
	}
	
	public List<Item> getUsers() {
		if (users == null) {
			Request request = getRequest();
			PairSearchResult<User,Person> result = modelService.searchPairs(new UsersPersonQuery(), request);
			users = Lists.newArrayList();
			for (Pair<User, Person> pair : result.getList()) {
				Item item = new Item();
				User user = pair.getKey();
				item.user = user;
				item.person = pair.getValue();
				item.userCanModifyPerson = securityService.canModify(item.person, request);
				item.setPublicCanViewPerson(securityService.canView(user, request.as(securityService.getPublicUser())));
				users.add(item);
			}
		}
		return users;
	}
	
	public Date getNow() {
		return new Date();
	}
	
	public class Item {
		private User user;
		private Person person;
		private boolean userCanModifyPerson;
		private boolean publicCanViewPerson;
		
		public User getUser() {
			return user;
		}
		
		public Person getPerson() {
			return person;
		}
		
		public boolean isUserCanModifyPerson() {
			return userCanModifyPerson;
		}
		
		public void setUserCanModifyPerson(boolean userCanModifyPerson) {
			this.userCanModifyPerson = userCanModifyPerson;
		}

		public void setPublicCanViewPerson(boolean publicCanViewPerson) {
			this.publicCanViewPerson = publicCanViewPerson;
		}

		public boolean isPublicCanViewPerson() {
			return publicCanViewPerson;
		}
	}
	
	// Wiring...
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
