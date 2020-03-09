package dk.in2isoft.onlineobjects.core;

import java.util.Date;

import org.hibernate.query.NativeQuery;

public class UserStatisticsQuery implements CustomQuery<UserStatisticsQuery.UserStatistic> {

	private SecurityService securityService;
	
	public UserStatisticsQuery(SecurityService securityService) {
		super();
		this.securityService = securityService;
	}

	public class UserStatistic {
		public long userId;
		public long entityCount;
		public Date latestModification;
	}

	@Override
	public String getSQL() {
		return "select count(item.id), max(item.updated), privilege.subject from item inner join privilege on item.id = privilege.\"object\" and privilege.\"alter\" = true where privilege.subject != :public_user_id group by privilege.subject;";
	}

	@Override
	public String getCountSQL() {
		return null;
	}

	@Override
	public UserStatisticsQuery.UserStatistic convert(Object[] row) {
		UserStatisticsQuery.UserStatistic result = new UserStatisticsQuery.UserStatistic();
		result.entityCount = ((Number) row[0]).longValue(); 
		if (row[1] != null) {
			result.latestModification = (Date) row[1];
		}
		result.userId = ((Number) row[2]).longValue(); 
		return result;
	}

	@Override
	public void setParameters(NativeQuery<?> sql) {
		sql.setParameter("public_user_id", securityService.getPublicUser().getId());
	}
}
