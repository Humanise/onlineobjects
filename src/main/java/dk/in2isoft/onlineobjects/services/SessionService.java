package dk.in2isoft.onlineobjects.services;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.core.SubSession;

public class SessionService {

	private static final Logger log = LogManager.getLogger(SessionService.class);


	private List<SubSession> subSessions = Lists.newArrayList();

	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		//session.setMaxInactiveInterval(10);
		log.debug("Session created: id="+session.getId()+",interval="+session.getMaxInactiveInterval());
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		log.debug("Session destroyed: "+event.getSession().getId());
	}

	public void registerSubSession(SubSession subSession) {
		this.subSessions.add(subSession);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public <T extends SubSession> @Nullable T getSubSession(String id, Class<T> type) {
		for (SubSession subSession : subSessions) {
			if (subSession.getId().equals(id) && type.isAssignableFrom(subSession.getClass())) {
				return (T) subSession;
			}
		}
		return null;
	}

}
