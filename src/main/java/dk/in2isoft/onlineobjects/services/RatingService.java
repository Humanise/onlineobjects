package dk.in2isoft.onlineobjects.services;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Rating;
import dk.in2isoft.onlineobjects.model.User;

public class RatingService {
	
	private ModelService modelService;
	
	public void rate(Entity entity, double rate, User user, Operator operator) throws SecurityException, ModelException {
		rate = makeValidRate(rate);
		Query<Rating> query = Query.of(Rating.class).from(entity).as(user); 
		Rating rating = modelService.search(query, operator).getFirst();
		if (rating!=null) {
			rating.setRating(rate);
			modelService.update(rating, operator);
		} else {
			rating = new Rating();
			rating.setRating(rate);
			modelService.create(rating, operator);
			modelService.createRelation(entity, rating, operator);
			modelService.createRelation(user, rating, operator);
		}
	}
	
	public double makeValidRate(double rate) {
		rate = Math.min(1, rate);
		rate = Math.max(-1, rate);
		return rate;
	}
	
	public void rateAdditive(Entity entity, double rate, User user, Operator operator) throws SecurityException, ModelException {
		Query<Rating> query = Query.of(Rating.class).from(entity).as(user); 
		Rating rating = modelService.search(query, operator).getFirst();
		if (rating!=null) {
			double newRate = makeValidRate(rating.getRating()+rate);
			rating.setRating(newRate);
			modelService.update(rating, operator);
		} else {
			rating = new Rating();
			rating.setRating(makeValidRate(rate));
			modelService.create(rating, operator);
			modelService.createRelation(entity, rating, operator);
			modelService.createRelation(user, rating, operator);
		}
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}

}
