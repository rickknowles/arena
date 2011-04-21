package arena.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValidation extends ValidationCheck {

	private String format;

	public DateValidation(String format) {
		super();
		this.format = format;
	}

    public ValidationFailure validate(Object value) {
    	if (value == null || value.equals("")) {
    		return null;
    	}

    	SimpleDateFormat sdf = new SimpleDateFormat(this.format);
    	try {
    		Date date = sdf.parse(value.toString());
    		if (sdf.format(date).equals(value.toString())) {
        		return null;
    		}
    	} catch (ParseException err) {
    	}
        return new ValidationFailure(getFieldName(), "dateFormat", value, this.format);
    }

}
