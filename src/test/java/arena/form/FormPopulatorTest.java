package arena.form;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import arena.action.ServletRequestState;

public class FormPopulatorTest extends TestCase {

    public void testSimple() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/abc/testURI.do");
        request.addParameter("locationId", "35");
        request.addParameter("locationId", "36");
        request.addParameter("verticalId", "1000");
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockServletContext context = new MockServletContext();
        
        ServletRequestState state  = new ServletRequestState(request, response, context);
        
        RequestFormPopulator<UserAccountEditForm> populator = new RequestFormPopulator<UserAccountEditForm>();
        populator.setFormClass(UserAccountEditForm.class);
        
        UserAccountEditForm form = populator.createPopulatedForm(state);
        assertNotNull(form.getLocationId());
        assertEquals(2, form.getLocationId().length);
        assertEquals("35", form.getLocationId()[0]);
        assertEquals("36", form.getLocationId()[1]);
        
        assertNotNull(form.getVerticalId());
        assertEquals(1, form.getVerticalId().length);
        assertEquals("1000", form.getVerticalId()[0]);
    }

    
    public static class UserAccountEditForm {
        private String[] locationId;
        private String[] verticalId;
        public String[] getLocationId() {
            return locationId;
        }
        public void setLocationId(String[] locationId) {
            this.locationId = locationId;
        }
        public String[] getVerticalId() {
            return verticalId;
        }
        public void setVerticalId(String[] verticalId) {
            this.verticalId = verticalId;
        }
        
    }
}
