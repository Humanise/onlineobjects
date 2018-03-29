package dk.in2isoft.onlineobjects.apps.people.views;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.PairSearchResult;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.UsersPersonQuery;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.jsf.ListModel;
import dk.in2isoft.onlineobjects.ui.jsf.ListModelResult;

public class PeopleListView extends AbstractManagedBean implements InitializingBean {

	private ModelService modelService;

	private ListModel<UserInfo> model;

	public void afterPropertiesSet() throws Exception {
		Privileged privileged = getRequest().getSession();
		model = new ListModel<UserInfo>() {
			@Override
			public ListModelResult<UserInfo> getResult() {
				UsersPersonQuery query = new UsersPersonQuery().withPaging(getPage(), getPageSize()).withPublicView();
				PairSearchResult<User, Person> search = modelService.searchPairs(query);
				return new ListModelResult<UserInfo>(convert(search.getList(), privileged),search.getTotalCount());
			}
			
		};
		model.setPageSize(10);
	}
	
	private List<UserInfo> convert(List<Pair<User,Person>> list, Privileged privileged) {
		List<UserInfo> result = new ArrayList<UserInfo>();
		for (Pair<User, Person> pair : list) {
			UserInfo info = new UserInfo();
			info.setPerson(pair.getValue());
			info.setUser(pair.getKey());
			try {
				info.setImage(modelService.getChild(pair.getKey(), Image.class, privileged));
			} catch (ModelException ignore) {}
			result.add(info);
		}
		return result;
	}
	
	public ListModel<UserInfo> getUserList() {
		return model;
	}

	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

}
