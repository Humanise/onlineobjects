package dk.in2isoft.onlineobjects.model.validation;

import java.util.List;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.util.ValidationUtil;

public class UserValidator implements EntityValidator {

	private ModelService modelService;
	
	@Override
	public void validate(Entity entity) throws ModelException {
		if (entity instanceof User) {
			User user = (User) entity;
			if (!ValidationUtil.isValidUsername(user.getUsername())) {
				throw new ModelException("Invalid username");
			}
			Query<User> query = Query.after(User.class).withField(User.FIELD_USERNAME, user.getUsername());
			List<User> users = modelService.list(query);
			for (User other : users) {
				if (other.getId() != user.getId()) {
					throw new ModelException("The username already exists");
				}
			}
		}
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
