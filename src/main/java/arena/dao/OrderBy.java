package arena.dao;

import java.lang.reflect.Array;
import java.util.Arrays;

public class OrderBy {
    private String alias;
    private String field;
    private boolean asc;
    
    public OrderBy(String field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }
    public OrderBy(String alias, String field, boolean asc) {
        this.alias = alias;
        this.field = field;
        this.asc = asc;
    }
    public String getAlias() {
        return alias;
    }
    public String getField() {
        return field;
    }
    public boolean isAsc() {
        return asc;
    }

    public static OrderBy[] appendToArray(OrderBy[] arr, OrderBy extra) {
        OrderBy[] out = null;
        if (arr == null) {
            out = (OrderBy[]) Array.newInstance(OrderBy.class, 1);
        } else {
            out = Arrays.copyOf(arr, arr.length + 1);
        }
        out[out.length - 1] = extra;
        return out;
    }
}
