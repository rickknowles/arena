package arena.authentication;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import arena.action.RequestState;
import arena.action.ServletRequestState;
import arena.dao.ReadOnlyDAO;


public class CookieLoginInterceptor<T> extends HandlerInterceptorAdapter implements ServletContextAware {

    private Authentication<T> authentication;
    private AutoLoginCookieSetter autoLoginCookieSetter;
    private ServletContext servletContext;
    
    private ReadOnlyDAO<T> dao;
    private String idField = "id";
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        RequestState requestState = new ServletRequestState(request, response, this.servletContext);
        String id = this.autoLoginCookieSetter.getIdFromAutoLoginCookie(requestState);
        if (id != null && !id.equals("")) {
            Object parsedId = parseId(id);
            T loggedInUser = this.dao.select().where(this.idField, parsedId).unique();
            this.authentication.setLoggedInUser(requestState, loggedInUser);
        }
        return true;
    }
    
    protected Object parseId(String idStr) {
        return Long.valueOf(idStr);
    }

    public AutoLoginCookieSetter getAutoLoginCookieSetter() {
        return autoLoginCookieSetter;
    }

    public void setAutoLoginCookieSetter(AutoLoginCookieSetter autoLoginCookieSetter) {
        this.autoLoginCookieSetter = autoLoginCookieSetter;
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

    public ReadOnlyDAO<T> getDao() {
        return dao;
    }

    public void setDao(ReadOnlyDAO<T> dao) {
        this.dao = dao;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

}
