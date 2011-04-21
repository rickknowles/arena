package arena.authentication;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.action.RequestState;
import arena.action.ServerSideUserState;
import arena.dao.DAO;
import arena.utils.ReflectionUtils;


public class SessionAuthentication<T> implements Authentication<T> {
    private final Log log = LogFactory.getLog(SessionAuthentication.class);
    
    private AutoLoginCookieSetter autoLoginCookieSetter;

    private String sessionUserParameterName;
    private String sessionUserIdParameterName = "SessionAuthentication.loggedInUserId";
    
    private boolean deleteAutoLoginCookiesOnLogout = true;
    
    private String idField = "id";
    
    private DAO<T> dao;
    
    @SuppressWarnings("unchecked")
    public T getLoggedInUser(RequestState requestState) {
        ServerSideUserState session = requestState.getServerSideUserState();
        if (this.sessionUserParameterName != null) {
            T user = (T) session.getAttribute(this.sessionUserIdParameterName);
            if (user != null) {
                return user;
            }
        }
        if (this.sessionUserIdParameterName != null) {
            Object userId = session.getAttribute(this.sessionUserIdParameterName);
            if (userId != null) {
                T user = getUserById(userId);
                if (user != null) {
                    return user;
                }
            }
        }
        return null;
    }
    
    protected T getUserById(Object userId) {
        return this.dao.select().where(this.idField, userId).unique();
    }

    public boolean isLoggedIn(RequestState requestState) {
        return (getLoggedInUser(requestState) != null);
    }

    public void logout(RequestState requestState) {
        log.info("User being logged out");        
//        requestState.getRequest().setAttribute(SessionAuthentication.class.getName() + ".loggedOut", Boolean.TRUE);
        ServerSideUserState session = requestState.getServerSideUserState();
        if (this.sessionUserParameterName != null) {
            session.removeAttribute(this.sessionUserParameterName);
        }
        if (this.sessionUserIdParameterName != null) {
            session.removeAttribute(this.sessionUserIdParameterName);
        }
        if (this.deleteAutoLoginCookiesOnLogout && this.autoLoginCookieSetter != null) {
            this.autoLoginCookieSetter.removeAutoLoginCookies(requestState);
        }
    }

    public void setLoggedInUser(RequestState requestState, T loggedInUser) {
        ServerSideUserState session = requestState.getServerSideUserState();
        if (this.sessionUserParameterName != null) {
            session.setAttribute(this.sessionUserParameterName, loggedInUser);
        }
        if (this.sessionUserIdParameterName != null) {
            session.setAttribute(this.sessionUserIdParameterName, 
                    ReflectionUtils.getAttributeUsingGetter(this.idField, loggedInUser));
        }
//        HttpServletRequest request = requestState.getRequest();
//        this.authenticationService.onLogin(loggedInUser, new Date(), 
//                request.getRemoteHost(), request.getHeader("User-agent"));
    }

    public AutoLoginCookieSetter getAutoLoginCookieSetter() {
        return autoLoginCookieSetter;
    }

    public void setAutoLoginCookieSetter(
            AutoLoginCookieSetter autoLoginCookieSetter) {
        this.autoLoginCookieSetter = autoLoginCookieSetter;
    }

    public String getSessionUserParameterName() {
        return sessionUserParameterName;
    }

    public void setSessionUserParameterName(String sessionUserParameterName) {
        this.sessionUserParameterName = sessionUserParameterName;
    }

    public String getSessionUserIdParameterName() {
        return sessionUserIdParameterName;
    }

    public void setSessionUserIdParameterName(String sessionUserIdParameterName) {
        this.sessionUserIdParameterName = sessionUserIdParameterName;
    }

    public boolean isDeleteAutoLoginCookiesOnLogout() {
        return deleteAutoLoginCookiesOnLogout;
    }

    public void setDeleteAutoLoginCookiesOnLogout(
            boolean deleteAutoLoginCookiesOnLogout) {
        this.deleteAutoLoginCookiesOnLogout = deleteAutoLoginCookiesOnLogout;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public DAO<T> getDao() {
        return dao;
    }

    public void setDao(DAO<T> dao) {
        this.dao = dao;
    }
}
