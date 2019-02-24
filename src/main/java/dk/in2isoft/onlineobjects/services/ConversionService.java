package dk.in2isoft.onlineobjects.services;

import java.util.List;

import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.conversion.EntityConverter;
import nu.xom.Node;


public class ConversionService {
	
	private List<EntityConverter> converters;

	//private static Logger log = LogManager.getLogger(ConversionFacade.class);
		
	public EntityConverter getConverter(Class<? extends Entity> classObj) {
		for (EntityConverter converter : converters) {
			if (converter.getType().isAssignableFrom(classObj)) {
				return converter;
			}
		}
		return new EntityConverter();
	}
	
	public EntityConverter getConverter(Entity entity) {
		return getConverter(entity.getClass());
	}
		
	public final Node generateXML(Entity entity, Operator privileged) throws ModelException {
		EntityConverter converter = getConverter(entity);
		return converter.generateXML(entity, privileged);
	}
	
	public void setConverters(List<EntityConverter> converters) {
		this.converters = converters;
	}
}
