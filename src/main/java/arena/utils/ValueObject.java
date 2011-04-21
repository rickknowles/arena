package arena.utils;

import java.util.Date;

public class ValueObject extends SimpleValueObject {
	private Date insertTimestamp;

	public Date getInsertTimestamp() {
		return insertTimestamp;
	}

	public void setInsertTimestamp(Date insertTimestamp) {
		this.insertTimestamp = insertTimestamp;
	}
}
