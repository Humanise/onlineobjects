package dk.in2isoft.onlineobjects.modules.user;

import java.util.List;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.PairSearchResult;
import dk.in2isoft.onlineobjects.core.UsersPersonQuery;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;

public class UserService {

	private ModelService modelService;

	public List<UserInfo> list(UsersPersonQuery query, Operator operator) throws ModelException {
		PairSearchResult<User,Person> result = modelService.searchPairs(query, operator);
		List<UserInfo> list = Lists.newArrayList();
		for (Pair<User, Person> pair : result.getList()) {
			UserInfo info = new UserInfo();
			info.setPerson(pair.getValue());
			info.setUser(pair.getKey());
			Image image = modelService.getChild(pair.getKey(), Image.class, operator);
			info.setImage(image);
			list.add(info);
		}
		return list;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
