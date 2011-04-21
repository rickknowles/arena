package arena.form;

import arena.action.RequestState;

public interface FormInitializer<U, F> {

    /**
     * Called before a form is displayed to the user to allow lookup lists to be displayed
     *
     * @param loggedInUser the currently logged in user
     * @param state the request object to add the lookups to
     *
     * @throws Exception
     */
    public void setInitialValues(U loggedInUser, F form, RequestState state) throws Exception;
}
