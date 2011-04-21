package arena.validation;

import junit.framework.TestCase;

public class DecimalValidationTest extends TestCase {

    public void testSimple() throws Exception {
        assertNotNull(new DecimalOnlyValidation().validate("aaa"));
        assertNotNull(new DecimalOnlyValidation().validate("a1111"));
        assertNotNull(new DecimalOnlyValidation().validate("1.11111111e-07"));
        assertNull(new DecimalOnlyValidation().validate("1.111111111111"));
        assertNull(new DecimalOnlyValidation().validate("191919191919191"));
        assertNotNull(new DecimalOnlyValidation().validate("1,111111111111"));
        assertNotNull(new DecimalOnlyValidation().validate("a91782644aa"));
        assertNotNull(new DecimalOnlyValidation().validate("a.121.1111"));
        assertNotNull(new DecimalOnlyValidation().validate("1.121.1111"));
    }
}
