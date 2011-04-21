package arena.dao.consumer;

import java.util.HashMap;
import java.util.Map;

import arena.dao.SelectSQLConsumerWithOutput;
import arena.utils.ReflectionUtils;

public class MapByFieldConsumer<X,T> implements SelectSQLConsumerWithOutput<T, Map<X,T>> {
    
    private Map<X,T> map = new HashMap<X,T>();
    private String field = "id";

    public MapByFieldConsumer() {
        super();
    }
    
    public MapByFieldConsumer(String field) {
        super();
        this.field = field;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean consume(T input, int rowCount) throws Exception {
        X id = (X) ReflectionUtils.getAttributeUsingGetter(this.field, input);
        map.put(id, input);
        return true;
    }

    @Override
    public Map<X,T> getOutput() {
        return map;
    }

}
