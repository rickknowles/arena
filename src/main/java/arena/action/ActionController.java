package arena.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UrlPathHelper;

public class ActionController implements Controller, BeanFactoryAware, ServletContextAware {
    private final Log log = LogFactory.getLog(ActionController.class);

    private ServletContext servletContext;
    private BeanFactory beanFactory;
    private boolean sessionSemaphore = true;
    private List<ActionControllerUrlMapping> urlMappings = new ArrayList<ActionControllerUrlMapping>();

    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // Get the full canonicalized uri without the query string
        String uri = new UrlPathHelper().getPathWithinApplication(request);

        // Map to the bean and method pair
        ActionControllerUrlMapping mappingParent = null;
        Object mapping = null;
        for (Iterator<ActionControllerUrlMapping> i = this.urlMappings.iterator(); mapping == null && i.hasNext(); ) {
            ActionControllerUrlMapping check = i.next();
            if (check != null) {
                mapping = check.getUrlMappings().get(uri);
                if (mapping != null) {
                    mappingParent = check;
                }
            }
        }
        if (mapping == null) {
            log.error("No mapping found for URI: " + uri);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        String mappingStr = mapping.toString();
        int dotPos = mappingStr.lastIndexOf('.');
        String beanName = mappingStr.substring(0, dotPos);
        String methodName = mappingStr.substring(dotPos + 1);
        log.info("Using mapped bean: " + beanName + ", method=" + methodName + " for uri=" + uri);

        // Look up the bean and run the method
        Object action = this.beanFactory.getBean(beanName);
        if (action == null) {
            throw new RuntimeException("Bean not found: name = " + beanName + ", uri = " + uri);
        }
        List<HandlerInterceptor> interceptors = mappingParent.getInterceptors();

        // run interceptor preHandles
        if (interceptors != null) {
            for (HandlerInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(request, response, null)) {
                    return null;
                }
            }
        }
        Object returnCode = null;
        ModelAndView mav = null;
        Exception err = null;
        try {
            Method method = action.getClass().getMethod(methodName, new Class<?>[] {RequestState.class});
            if (method != null) {
                ServletRequestState state = new ServletRequestState(request, response, this.servletContext);
                if (sessionSemaphore) {
                    synchronized (state.getServerSideUserState().getSemaphore("actionControllerLock")) {
                        returnCode = method.invoke(action, state);
                    }
                } else {
                    returnCode = method.invoke(action, state);
                }
                log.info(uri + ": " + beanName + "." + methodName + " = " + returnCode);

                // Build a model and view to send back
                if (returnCode != null) {
                    mav = new ModelAndView(beanName + "." + methodName + "." + returnCode);
                    mav.addAllObjects(state.getArgsAsMap());
                }
            }
            return mav;
        } catch (NoSuchMethodException errFound) {
            // attempt raw servlet style
            try {
                Method method = action.getClass().getMethod(methodName, new Class<?>[] {HttpServletRequest.class, HttpServletResponse.class});
                if (method != null) {
                    if (sessionSemaphore) {
                        ServerSideUserState session = new HttpSessionWrapper(request);
                        synchronized (session.getSemaphore("actionControllerLock")) {
                            returnCode = method.invoke(action, request, response);
                        }
                        session.close();
                    } else {
                        returnCode = method.invoke(action, request, response);
                    }
                    log.info(uri + ": " + beanName + "." + methodName + " = " + returnCode);

                    // Build a model and view to send back
                    if (returnCode != null) {
                        mav = new ModelAndView(beanName + "." + methodName + "." + returnCode);
                        for (Enumeration<?> e = request.getAttributeNames(); e.hasMoreElements(); ) {
                            String attributeName = (String) e.nextElement();
                            mav.addObject(attributeName, request.getAttribute(attributeName));
                        }
                    }
                }
                return mav;
            } catch (NoSuchMethodException errFound2) {
                err = new RuntimeException("No method found: " + methodName + " on bean: " + beanName, errFound);
                throw err;
            }
        } finally {
            if (interceptors != null) {
	            for (HandlerInterceptor interceptor : interceptors) {
	                interceptor.postHandle(request, response, null, mav);
	            }
	            for (HandlerInterceptor interceptor : interceptors) {
	                interceptor.afterCompletion(request, response, null, err);
	            }
            }
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setUrlMappings(List<ActionControllerUrlMapping> urlMappings) {
        this.urlMappings = urlMappings;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void addUrlMappings(ActionControllerUrlMapping mapping) {
        if (this.urlMappings == null) {
            throw new RuntimeException("urlMappings is null");
        }
        this.urlMappings.add(mapping);
    }

    public void setSessionSemaphore(boolean sessionSemaphore) {
        this.sessionSemaphore = sessionSemaphore;
    }

}
