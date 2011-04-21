package arena.action;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import arena.form.RequestFormPopulator;

public class FormPopulatorTest extends TestCase {

    public void testRequestFormPopulator() throws Exception {
        MockHttpServletRequest mreq = new MockHttpServletRequest();
        MockHttpServletResponse mresp = new MockHttpServletResponse();
        MockServletContext msc = new MockServletContext();
        
        mreq.setParameter("abc", new String [] {"111", "222", "333"});
        mreq.setParameter("def", "123");
        mreq.setParameter("variableLength1", "2");
        mreq.setParameter("variableLength2", "2");
        mreq.setParameter("variableLengthCount", "2");
        
        RequestState state = new ServletRequestState(mreq, mresp, msc);
        
        RequestFormPopulator<TestForm> fp = new RequestFormPopulator<TestForm>();
        fp.setVariableLengthCollectionFields(new String[] {"variableLength"});
        fp.setFormClass(TestForm.class);
        TestForm form = fp.createPopulatedForm(state);
        assertEquals("123", form.getDef());
        assertEquals(3, form.getAbc().length);
        assertEquals(2, form.getVariableLength().length);
        assertEquals("123", form.getDef());
    }
    
    
}
