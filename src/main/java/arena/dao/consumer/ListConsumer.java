package arena.dao.consumer;

public class ListConsumer<I> extends TransformListConsumer<I,I> {
    @Override
    protected I transformRow(I input) {
        return input;
    }
}
