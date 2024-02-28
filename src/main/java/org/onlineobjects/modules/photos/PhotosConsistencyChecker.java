package org.onlineobjects.modules.photos;

import dk.in2isoft.onlineobjects.core.ConsistencyChecker;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.services.PileService;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class PhotosConsistencyChecker implements ConsistencyChecker {

	private PileService piles;
	private ModelService model;
	private SecurityService security;
	
	@Override
	public void check() throws ModelException, SecurityException, ExplodingClusterFuckException {
		model.asAdmin((Operator operator) -> {
			Pile pile = piles.getOrCreateGlobalPile(ImageService.FEATURED_PILE, operator);
			security.grantPublicView(pile, true, operator);
		});
	}

	public void setPiles(PileService piles) {
		this.piles = piles;
	}
	
	public void setModel(ModelService model) {
		this.model = model;
	}
	
	public void setSecurity(SecurityService security) {
		this.security = security;
	}
}
