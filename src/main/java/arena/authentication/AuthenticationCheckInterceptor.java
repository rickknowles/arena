package arena.authentication;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import arena.action.RequestState;
import arena.action.ServletRequestState;
import arena.utils.ServletUtils;


public class AuthenticationCheckInterceptor<T> extends HandlerInterceptorAdapter implements ServletContextAware {
    private final Log log = LogFactory.getLog(AuthenticationCheckInterceptor.class);

    private Authentication<T> authentication;
    private ServletContext servletContext;

    private String loggedInUserParam = "loggedInUser";
    private String viewName;
    private ViewResolver viewResolver;
    private LocaleResolver localeResolver;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        RequestState requestState = new ServletRequestState(request, response, this.servletContext);
        if (this.authentication.isLoggedIn(requestState)) {
            return true;
        } else {
            String uri = getFullRequestURI(request);
            requestState.setArg("redirectURI", uri);
            log.info("Non-authenticated access to " + uri);
            
            if (this.viewName != null && this.viewResolver != null) {
                View v = this.viewResolver.resolveViewName(this.viewName, this.localeResolver.resolveLocale(request));
                if (v == null) {
                    throw new RuntimeException("Login form view not found: " + this.viewName);
                }
                v.render(requestState.getArgsAsMap(), request, response);
            }
            return false;
        }
    }
    
    protected String getFullRequestURI(HttpServletRequest request) {
        String qs = request.getQueryString();
        return ServletUtils.getURIFromRequest(request) + (qs != null && !qs.equals("") ? "?" + qs : "");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        RequestState requestState = new ServletRequestState(request, response, this.servletContext);
        if (modelAndView != null && loggedInUserParam != null) {
            T lu = this.authentication.getLoggedInUser(requestState);
            if (lu != null) {
                modelAndView.addObject(this.loggedInUserParam, lu);
            }
        }
        super.postHandle(request, response, handler, modelAndView);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public Authentication<T> getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication<T> authentication) {
        this.authentication = authentication;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public ViewResolver getViewResolver() {
        return viewResolver;
    }

    public void setViewResolver(ViewResolver viewResolver) {
        this.viewResolver = viewResolver;
    }

    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }
}
