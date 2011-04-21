package arena.lucene;

public interface FormToQueryConverter<F,UA> {
    public LuceneQueryModel buildQueryModel(F form, UA userAccount) throws Exception;
}
