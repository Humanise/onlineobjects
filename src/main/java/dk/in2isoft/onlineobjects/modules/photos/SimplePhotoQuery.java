package dk.in2isoft.onlineobjects.modules.photos;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;

import dk.in2isoft.onlineobjects.core.CustomQuery;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class SimplePhotoQuery implements CustomQuery<SimplePhotoPerspective> {

	private long viewer;
	private boolean featured;

	public SimplePhotoQuery(Privileged viewer) {
		super();
		this.viewer = viewer.getIdentity();
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}

	public String getSQL() {
		StringBuilder sql = new StringBuilder();
		sql.append("select image.id, image.width, image.height, entity.name, own.subject as owner, rotation_property.doublevalue, colors_property.value from image ");
		sql.append("left join privilege as own ON own.object = image.id and own.subject!=:viewer and own.alter = true, privilege as view_privilege, entity ");
		sql.append("left join property as rotation_property on entity.id=rotation_property.entity_id and rotation_property.key='"+Property.KEY_PHOTO_ROTATION+"' ");
		sql.append("left join property as colors_property on entity.id=colors_property.entity_id and colors_property.key='"+Property.KEY_PHOTO_COLORS+"' ");

		sql.append("where view_privilege.object = image.id and view_privilege.subject=:viewer and view_privilege.view = TRUE and entity.id = image.id");
		if (featured) {
			sql.append(" and image.id in (select sub_entity_id from relation where relation.super_entity_id in (select pile.id from pile join property on property.entity_id = pile.id and property.\"value\" = '" + ImageService.FEATURED_PILE + "'))");
		}
		sql.append(" order by entity.id desc");

		return sql.toString();
	}

	public String getCountSQL() {
		return null;
	}

	public SimplePhotoPerspective convert(Object[] row) {
		SimplePhotoPerspective item = new SimplePhotoPerspective();
		if (row[0]!=null) {
			item.setId(((Number) row[0]).longValue());
		}
		if (row[1]!=null) {
			item.setWidth(((Number) row[1]).intValue());
		}
		if (row[2]!=null) {
			item.setHeight(((Number) row[2]).intValue());
		}
		if (row[3]!=null) {
			item.setTitle(row[3].toString());
		}
		if (row[4]!=null) {
			item.setOwnerId(((Number) row[4]).longValue());
		}
		if (row[5]!=null) {
			item.setRotation(((Number) row[5]).floatValue());
		}
		if (row[6]!=null) {
			item.setColors(row[6].toString());
		}
		return item;
	}

	public void setParameters(NativeQuery<?> sql) {
		sql.setParameter("viewer", viewer, StandardBasicTypes.LONG);
	}
}
