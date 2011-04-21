package arena.dao.consumer;

import java.util.ArrayList;
import java.util.List;

import arena.dao.SelectSQLConsumerWithOutput;

public abstract class TransformListConsumer<I,O> implements SelectSQLConsumerWithOutput<I, List<O>> {
    
    private List<O> list = new ArrayList<O>();

    @Override
    public boolean consume(I input, int rowCount) {
        list.add(transformRow(input));
        return true;
    }
    
    protected abstract O transformRow(I input);

    @Override
    public List<O> getOutput() {
        return list;
    }

}
