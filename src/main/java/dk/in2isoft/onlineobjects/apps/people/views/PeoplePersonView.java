package dk.in2isoft.onlineobjects.apps.people.views;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.PairSearchResult;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UsersPersonQuery;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.UserProfileInfo;
import dk.in2isoft.onlineobjects.services.PersonService;
import dk.in2isoft.onlineobjects.ui.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.ListModel;
import dk.in2isoft.onlineobjects.ui.jsf.ListModelResult;

public class PeoplePersonView extends AbstractManagedBean {
	
	private PersonService personService;
	private ModelService modelService;
	private SecurityService securityService;
	
	private User user;
	private Person person;
	private Image image;
	private ListModel<Image> listModel;
	private UserProfileInfo profileInfo;
	private ListModel<Image> latestImages;
	private boolean canModify;
	private String usersName;
	
	public void before(Request request) throws Exception {
		String[] path = request.getLocalPath();
		usersName = path.length==2 ? path[1] : path[0];
		UsersPersonQuery query = new UsersPersonQuery().withUsername(getUsersName());
		PairSearchResult<User,Person> result = modelService.searchPairs(query);
		if (result.getTotalCount()==0) {
			throw new ContentNotFoundException();
		}
		Pair<User, Person> next = result.iterator().next();
		user = next.getKey();
		person = next.getValue();
		Privileged privileged = request.getSession();
		if (!securityService.canView(user, privileged)) {
			throw new ContentNotFoundException();
		}
		canModify = securityService.canModify(person, privileged);
		try {
			image = modelService.getChild(user, Relation.KIND_SYSTEM_USER_IMAGE, Image.class,  privileged);
		} catch (ModelException e) {
			// TODO: Do something usefull
		}
		this.profileInfo = personService.getProfileInfo(getPerson(),request.getSession());
	}
	
	public ListModel<Image> getLatestImages() {
		if (latestImages==null) {
			latestImages = new ListModel<Image>() {
				private ListModelResult<Image> result;
				
				@Override
				public ListModelResult<Image> getResult() {
					if (result!=null) return result;
					User user = modelService.getUser(getUsersName());
					Query<Image> query = Query.of(Image.class).as(user).orderByCreated().withPaging(0, getPageSize()).descending();
					if (!canModify) {
						query.withPublicView();
					}
					SearchResult<Image> search = modelService.search(query);
					result = new ListModelResult<Image>(search.getList(),search.getList().size());
					return result;
				}
			};
			latestImages.setPageSize(16);
		}
		return latestImages;
	}
	
	public ListModel<Image> getImageList() {
		if (listModel!=null) return listModel;
		ListModel<Image> model = new ListModel<Image>() {

			@Override
			public ListModelResult<Image> getResult() {
				User user = modelService.getUser(getUsersName());
				Query<Image> query = Query.of(Image.class).as(user).orderByCreated().withPaging(getPage(), getPageSize()).descending();
				if (!canModify) {
					query.withPublicView();
				}
				SearchResult<Image> search = modelService.search(query);
				return new ListModelResult<Image>(search.getList(),search.getTotalCount());
			}
			
		};
		model.setPageSize(24);
		listModel = model;
		return model;
	}
	
	private String getUsersName() {
		return this.usersName;
	}
	
	public User getUser() {
		return user;
	}
	
	public UserProfileInfo getInfo() throws ModelException {
		return this.profileInfo;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public Image getImage() {
		return image;
	}
	
	public boolean isCanModify() {
		return canModify;
	}
	
	public boolean isFound() {
		return user!=null;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}
}
