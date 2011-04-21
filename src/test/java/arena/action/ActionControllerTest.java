package arena.action;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;

public class ActionControllerTest extends TestCase {

    public void testSimple() throws Exception {
        BeanFactory bf = getBeanFactory("src/test/conf/ActionControllerTest.xml", null);
        ActionController controller = (ActionController) bf.getBean("actionController");
        ActionControllerUrlMapping mapping = (ActionControllerUrlMapping) bf.getBean("mapping");
        controller.setServletContext(new MockServletContext());
        
        mapping.afterPropertiesSet();
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/abc/testURI.do");
        request.setParameter("testArg", "35");
        request.setMethod("GET");
        
        ModelAndView mav = controller.handleRequest(request, new MockHttpServletResponse());
        assertEquals("testAction.testMethod.OK", mav.getViewName());
        assertEquals("35", mav.getModel().get("testArg"));
    }

    private BeanFactory getBeanFactory(String configXml, String configProps) {
        // Initialize spring context and replace properties with the propsFile
        ConfigurableListableBeanFactory context = new XmlBeanFactory(new FileSystemResource(new File(configXml)));
        if (configProps != null) {
            PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
            cfg.setLocation(new FileSystemResource(configProps));
            // now actually do the replacement
            cfg.postProcessBeanFactory(context);
        }
        return context;
    }
}
