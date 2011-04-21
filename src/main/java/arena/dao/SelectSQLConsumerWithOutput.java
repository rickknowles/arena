package arena.dao;

public interface SelectSQLConsumerWithOutput<I,O> extends SelectSQLConsumer<I>{
    /**
     * Returns the output of the consumption process. Called by the consume() method on completion
     * @return
     */
    public O getOutput();
}
