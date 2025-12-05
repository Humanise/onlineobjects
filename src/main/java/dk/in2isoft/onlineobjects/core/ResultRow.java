package dk.in2isoft.onlineobjects.core;

public class ResultRow {

	private Object[] row;

	public ResultRow(Object[] row) {
		change(row);
	}

	public void change(Object[] row) {
		this.row = row == null ? new Object[] {} : row;
	}

	public String getString(int index) {
		return row[index] != null ? row[index].toString() : null;
	}

	public boolean getBoolean(int i) {
		Object value = get(i);
		if (value != null && value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		return false;
	}

	private Object get(int i) {
		return row[i];
	}

	public long getLong(int i) {
		Object value = get(i);
		if (value != null && value instanceof Number) {
			return ((Number) value).longValue();
		}
		return 0;
	}
}
