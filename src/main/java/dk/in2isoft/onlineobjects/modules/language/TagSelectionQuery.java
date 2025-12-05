package dk.in2isoft.onlineobjects.modules.language;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;

import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.core.CustomQuery;
import dk.in2isoft.onlineobjects.core.Privileged;

public class TagSelectionQuery implements CustomQuery<ItemData> {

	private long privileged;

	public TagSelectionQuery(Privileged privileged) {
		super();
		this.privileged = privileged.getIdentity();
	}

	public String getSQL() {
		String SQL = "SELECT\n"
				+ "	tag_entity.id,\n"
				+ "	tag_entity.name,\n"
				+ "	count(other_entity.id) AS count\n"
				+ "FROM\n"
				+ "	tag\n"
				+ "	\n"
				+ "join entity AS tag_entity on tag.id = tag_entity.id\n"
				+ "left join relation on tag.id = relation.super_entity_id\n"
				+ "left join entity as other_entity on relation.sub_entity_id = other_entity.id and other_entity.id in (select object from privilege where subject = :privileged and \"alter\" = TRUE) \n"
				+ "\n"
				+ "where tag.id in (select object from privilege where subject = :privileged and \"alter\" = TRUE)\n"
				+ "\n"
				+ "GROUP BY\n"
				+ "	tag_entity.id,\n"
				+ "	tag_entity.name\n"
				+ "ORDER BY\n"
				+ "	lower(tag_entity.name)";


		return SQL;
	}

	public String getCountSQL() {
		return null;
	}

	public ItemData convert(Object[] row) {
		ItemData item = new ItemData();
		item.setId(((Number) row[0]).longValue());
		item.setValue(((Number) row[0]));

		item.setText((String) (row[1]==null ? "none" : row[1]));
		item.setBadge(((Number) row[2]).toString());
		return item;
	}

	public void setParameters(NativeQuery<?> sql) {
		sql.setParameter("privileged", privileged, StandardBasicTypes.LONG);
	}
}
