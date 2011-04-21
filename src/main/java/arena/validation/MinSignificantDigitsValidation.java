package arena.validation;


public class MinSignificantDigitsValidation extends ValidationCheck {

    private int digits;
    
    public MinSignificantDigitsValidation(int digits) {
        super();
        this.digits = digits;
    }
    
    @Override
    public ValidationFailure validate(Object value) {
        if (value == null || value.equals("")) {
            return null;
        }

        int dotIndex = value.toString().lastIndexOf('.');
        if (dotIndex == -1 && digits == 0) {
            return null;
        } else if (value.toString().length() - dotIndex >= this.digits) {
            return null;
        }
        return new ValidationFailure(getFieldName(), "minSignificantDigits", value, this.digits);
    }

}
