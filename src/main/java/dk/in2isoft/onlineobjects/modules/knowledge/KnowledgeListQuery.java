package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.Collection;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.onlineobjects.apps.api.KnowledgeListRow;
import dk.in2isoft.onlineobjects.core.CustomQuery;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.ResultRow;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;

public class KnowledgeListQuery implements CustomQuery<KnowledgeListRow> {

	private long privileged;
	private Collection<Long> ids;

	public KnowledgeListQuery(Privileged privileged) {
		super();
		this.privileged = privileged.getIdentity();
	}

	public KnowledgeListQuery withIds(Collection<Long> ids) {
		this.ids = ids;
		return this;
	}

	public String getSQL() {
		return "select entity.id,"

				+ "case"
				+ "	when internetaddress.id is not null then '" + InternetAddress.class.getSimpleName() + "'" 
				+ "	when question.id is not null then '" + Question.class.getSimpleName() + "'"
				+ "	when hypothesis.id is not null then '" + Hypothesis.class.getSimpleName() + "'" 
				+ "	when \"statement\".id is not null then '" + Statement.class.getSimpleName() + "'" 
				+ " end as \"type\"," 

				+ " favorite_relation.sub_entity_id NOTNULL as favorite,"
				+ " inbox_relation.sub_entity_id NOTNULL as inbox,"

				+ " COALESCE(question.\"text\", \"statement\".\"text\", hypothesis.\"text\", entity.\"name\") as text," 

				+ " internetaddress.address as url" 

				+ " from entity"

				+ " left join question on entity.id = question.id"

				+ " left join internetaddress on entity.id = internetaddress.id" 

				+ " left join \"statement\" on entity.id = statement.id" 
				+ " left join hypothesis on entity.id = hypothesis.id" 
				
				+ " inner join privilege on privilege.\"object\" = entity.id and privilege.subject = :privileged"
				+ " left join relation as favorite_relation on entity.id = favorite_relation.sub_entity_id"
				+ " and favorite_relation.super_entity_id = (select relation.sub_entity_id from relation where kind = '" + Relation.KIND_SYSTEM_USER_FAVORITES + "' and relation.super_entity_id = :privileged limit 1)"

				+ " left join relation as inbox_relation on entity.id = inbox_relation.sub_entity_id"
				+ " and inbox_relation.super_entity_id = (select relation.sub_entity_id from relation where kind = '" + Relation.KIND_SYSTEM_USER_INBOX + "' and relation.super_entity_id = :privileged limit 1)" +

				" where entity.id in (:ids)";
	}

	public String getCountSQL() {
		return null;
	}

	public KnowledgeListRow convert(Object[] raw) {
		ResultRow row = new ResultRow(raw);
		KnowledgeListRow item = new KnowledgeListRow();
		item.setId(row.getLong(0));
		item.setType(row.getString(1));
		item.setFavorite(row.getBoolean(2));
		item.setInbox(row.getBoolean(3));
		item.setText(row.getString(4));
		item.setUrl(row.getString(5));
		return item;
	}

	public void setParameters(NativeQuery<?> sql) {
		sql.setParameter("privileged", privileged, LongType.INSTANCE);
		Collection<Long> ids = Code.isEmpty(this.ids) ? Lists.newArrayList(-1l) : this.ids;
		sql.setParameterList("ids", ids, LongType.INSTANCE);
	}
}
