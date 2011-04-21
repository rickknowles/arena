package arena.form;

import arena.action.RequestState;

public interface FormPopulator<F> {

    public F createPopulatedForm(RequestState state) throws Exception;
    
    public void releaseForm(RequestState state, F form) throws Exception;

}