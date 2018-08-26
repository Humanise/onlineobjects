package dk.in2isoft.onlineobjects.model;

import java.util.Date;

public class LogEntry {

	private long id;
	private Date time;
	private LogLevel level;
	private LogType type;
	private Long subject;
	private Long object;
	private String data;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}

	public LogType getType() {
		return type;
	}

	public void setType(LogType type) {
		this.type = type;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Long getSubject() {
		return subject;
	}

	public void setSubject(Long subject) {
		this.subject = subject;
	}

	public Long getObject() {
		return object;
	}

	public void setObject(Long object) {
		this.object = object;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	
}
