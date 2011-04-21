package arena.osc;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

public class OscServerBean implements InitializingBean, BeanFactoryAware {
    private final Log log = LogFactory.getLog(OscServerBean.class);

    private BeanFactory beanFactory;
    
    private OSCPortIn oscReceiver;
    private int listenerPort = 8000;
    private Map<String,String> mappings;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.oscReceiver = new OSCPortIn(this.listenerPort);
        for (Entry<String,String> entry : this.mappings.entrySet()) {
            final String path = entry.getKey();
            final String beanName = entry.getValue();
            this.oscReceiver.addListener(path, new OSCListener() {
                @Override
                public void acceptMessage(Date date, OSCMessage message) {
                    log.info("Mapped request for path: " + path + " to bean " + beanName);
                    Object bean = beanFactory.getBean(beanName);
                    if (bean == null) {
                        throw new IllegalArgumentException("No bean found for " + beanName);
                    } else if (bean instanceof OSCListener) {
                        ((OSCListener) bean).acceptMessage(date, message);
                    } else {
                        throw new IllegalArgumentException(beanName + " is not an OSCListener instance");
                    }
                }
            });
        }
        this.oscReceiver.startListening();
    }
    
    public void destroy() {
        if (this.oscReceiver != null) {
            this.oscReceiver.stopListening();
            this.oscReceiver.close();
            this.oscReceiver = null;
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }
    
}
