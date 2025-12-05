package dk.in2isoft.onlineobjects.modules.language;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;

import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.core.CustomQuery;
import dk.in2isoft.onlineobjects.core.Privileged;

public class WordByMultipleTypesQuery implements CustomQuery<ItemData> {

	private long privileged;

	public WordByMultipleTypesQuery(Privileged privileged) {
		super();
		this.privileged = privileged.getIdentity();
	}

	public String getSQL() {
		String SQL = "select word.id as word_id,word.text,count(entity.id) as count from word,relation,entity,privilege "+
				" where privilege.subject=:privileged and privilege.alter=true and entity.id = relation.super_entity_id and relation.sub_entity_id=word.id and privilege.object = entity.id"+
				" group by word.id,word.text order by lower(word.text)";
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
