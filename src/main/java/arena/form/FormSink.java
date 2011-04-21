package arena.form;

import arena.action.RequestState;

public interface FormSink<F> {
    public void sink(F form, RequestState state) throws Exception;
}
