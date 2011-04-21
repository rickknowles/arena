package arena.action;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.HandlerInterceptor;

public class ActionControllerUrlMapping implements BeanFactoryAware, InitializingBean {

    private List<HandlerInterceptor> interceptors; 
    private Properties urlMappings;
    private ActionController actionController;
    private String actionControllerName = "actionController";
 
    private BeanFactory beanFactory;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.urlMappings != null) {
            if (this.actionController == null && this.beanFactory != null && this.actionControllerName != null) {
                this.actionController = (ActionController) this.beanFactory.getBean(this.actionControllerName);
            }
            this.actionController.addUrlMappings(this);
        }
    }
    public void setUrlMappings(Properties urlMappings) {
        this.urlMappings = urlMappings;
    }
    public void setActionController(ActionController actionController) {
        this.actionController = actionController;
    }
    public void setActionControllerName(String actionControllerName) {
        this.actionControllerName = actionControllerName;
    }
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    public List<HandlerInterceptor> getInterceptors() {
        return interceptors;
    }
    public void setInterceptors(List<HandlerInterceptor> interceptors) {
        this.interceptors = interceptors;
    }
    public Properties getUrlMappings() {
        return urlMappings;
    }
    public ActionController getActionController() {
        return actionController;
    }
    public String getActionControllerName() {
        return actionControllerName;
    }
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
