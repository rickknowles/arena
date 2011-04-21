package arena.utils;


public class SimpleValueObject {
    public String toString() {
        return ReflectionUtils.toString(this);
    }
}
