package arena.form;

import java.util.List;

import arena.validation.ValidationFailure;



public interface FormValidator<U,F> {

    /**
     * Execution of the validations
     * 
     * @param loggedInUser the currently logged in user
     * @param form form object to validate
     * @param errors errors found during validation. Add to this list
     * @return true if no errors were found in this set of validations
     * 
     * @throws Exception
     */
    public boolean validate(U loggedInUser, F form, List<ValidationFailure> errors) throws Exception;
}
