package arena.asterisk;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.fastagi.AbstractMappingStrategy;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.AsteriskServerListener;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerEventListener;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

public class AgiServerBean implements InitializingBean, BeanFactoryAware, Runnable {
    private final Log log = LogFactory.getLog(AgiServerBean.class);

    private BeanFactory beanFactory;
    
    private DefaultAgiServer agiServer = new DefaultAgiServer(new BeanFactoryMappingStrategy());
   
    private AsteriskServer asteriskServer;
    private AsteriskServerListener[] liveEventListeners;
    
    private ManagerConnection managerConnection;
    private ManagerConnectionAware[] managerConnectionAware;
    private ManagerEventListener[] managerEventListeners;
    
    private Map<String,String> mappings;
    
    private String managerServerAddress = "localhost";
    private String managerUser = "manager";
    private String managerPassword = "password";
    private int managerPort = 5038;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(this, "Asterisk listener thread").start();
    }
    
    public void destroy() {
        destroyInternal();
        this.agiServer.shutdown();
    }
    
    protected void destroyInternal() {
        if (this.managerConnection != null) {
            this.managerConnection.logoff();
            for (ManagerEventListener listener : this.managerEventListeners) {
                this.managerConnection.removeEventListener(listener);
            }
            if (this.managerConnectionAware != null) {
                for (ManagerConnectionAware aware : this.managerConnectionAware) {
                    aware.setManagerConnection(null);
                }
            }
            this.managerConnection = null;
        }
        if (this.asteriskServer != null) {
            for (AsteriskServerListener listener : this.liveEventListeners) {
                this.asteriskServer.removeAsteriskServerListener(listener);
            }
            this.asteriskServer.shutdown();
            this.asteriskServer = null;
        }
    }
    
    public void run() {        
        destroyInternal();
        
        log.info("Starting agi server");
        new Thread(new Runnable() {
                public void run() {agiServer.run();}
            }).start();
        
        if (this.liveEventListeners != null && this.liveEventListeners.length > 0) {            
            this.asteriskServer = new DefaultAsteriskServer(this.managerServerAddress, this.managerUser, this.managerPassword);
            log.info("Adding live event listeners");
            for (AsteriskServerListener listener : this.liveEventListeners) {
                this.asteriskServer.addAsteriskServerListener(listener);
            }
        }
        if (this.managerEventListeners != null && this.managerEventListeners.length > 0) { 
            ManagerConnectionFactory factory = new ManagerConnectionFactory(this.managerServerAddress, this.managerPort, 
                    this.managerUser, this.managerPassword);
            this.managerConnection = factory.createManagerConnection();
            log.info("Adding manager event listeners");
            for (ManagerEventListener listener : this.managerEventListeners) {
                this.managerConnection.addEventListener(listener);
                if (listener instanceof ManagerConnectionAware) {
                    ((ManagerConnectionAware) listener).setManagerConnection(managerConnection);
                }
            }
            try {
                this.managerConnection.login();
            } catch (Throwable err) {
                throw new RuntimeException("Login failure", err);
            }
            
//            this.managerEventWatchThread = new Thread(new PollForStatusRunnable(), "poll for asterisk status");
//            this.managerEventWatchThread.start();
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
    
    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public void setAgiServer(DefaultAgiServer agiServer) {
        this.agiServer = agiServer;
    }

    public void setManagerServerAddress(String managerServerAddress) {
        this.managerServerAddress = managerServerAddress;
    }

    public void setManagerUser(String managerUser) {
        this.managerUser = managerUser;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public void setLiveEventListeners(AsteriskServerListener[] liveEventListeners) {
        this.liveEventListeners = liveEventListeners;
    }

    public void setManagerEventListeners(ManagerEventListener[] managerEventListeners) {
        this.managerEventListeners = managerEventListeners;
    }

    public void setManagerPort(int managerPort) {
        this.managerPort = managerPort;
    }

    public void setManagerConnectionAware(ManagerConnectionAware[] managerConnectionAware) {
        this.managerConnectionAware = managerConnectionAware;
    }

    class BeanFactoryMappingStrategy extends AbstractMappingStrategy {
        @Override
        public AgiScript determineScript(AgiRequest request) {
            String beanName;
            if (mappings != null) {
                beanName = mappings.get(request.getScript());
            } else {
                beanName = request.getScript();
            }
            Object bean = beanFactory.getBean(beanName);
            if (bean == null) {
                throw new IllegalArgumentException("No script bean found for " + beanName);
            } else if (bean instanceof AgiScript) {
                return (AgiScript) bean;
            } else {
                throw new IllegalArgumentException(beanName + " is not an AgiScript bean");
            }
        }
    }
}
