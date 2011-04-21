package arena.dao;


public interface AliasDotColumnResolver {
    
    public String getDefaultAlias();

    /**
     * Returns null if there is no match for the alias and field
     * @param conditionFieldName
     * @param conditionAlias
     * @return
     */
    public ResolvedColumnMetadata resolve(String conditionFieldName, String conditionAlias); 
}
