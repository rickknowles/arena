package arena.authentication;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.action.RequestState;
import arena.dao.DAO;
import arena.utils.ReflectionUtils;

public class AuthenticationAction<T> {
    private final Log log = LogFactory.getLog(AuthenticationAction.class);

    private Authentication<T> authentication;
    private AutoLoginCookieSetter autoLoginCookieSetter;
    private DAO<T> dao;
    private PasswordEncrypter passwordEncrypter;
    
    private String loginIdParam = "loginId";
    private String passwordParam = "password";

    private String idField = "id";
    private String loginIdField = "loginId";
    private String encryptedPasswordField = "encryptedPassword";

    public String loginForm(RequestState state) throws Exception {
        return "OK";
    }
    
    public String login(RequestState state) throws Exception {
        String loginId = state.getArg(this.loginIdParam, "");
        String rawPassword = state.getArg(this.passwordParam, "");
        boolean autoLoginCookie = state.getArg("autoLoginCookie", Boolean.FALSE);
        
        if (!loginId.equals("")) {
            String encrypted = this.passwordEncrypter.encrypt(rawPassword);
            log.info("Using loginId: " + loginId + ", encryptedPassword: " + encrypted);
            
            T user = this.dao.select()
                                  .where(this.loginIdField, loginId)
                                  .where(this.encryptedPasswordField, encrypted)
                                  .ascOrderBy("id").first();
            if (user != null) {
                if (this.authentication != null) {
                    this.authentication.setLoggedInUser(state, user);
                }
                if (autoLoginCookie && this.autoLoginCookieSetter != null) {
                    Object id = ReflectionUtils.getAttributeUsingGetter(this.idField, user);
                    this.autoLoginCookieSetter.addAutoLoginCookies(state, id == null ? null : id.toString());
                }
                return "OK";
            }
        }
        return "INVALID";
    }
    
    public String logout(RequestState requestState) throws Exception {
        if (this.authentication != null) {
            this.authentication.logout(requestState);
        }
        return "OK";
    }

    public PasswordEncrypter getPasswordEncrypter() {
        return passwordEncrypter;
    }

    public void setPasswordEncrypter(PasswordEncrypter passwordEncrypter) {
        this.passwordEncrypter = passwordEncrypter;
    }

    public String getLoginIdParam() {
        return loginIdParam;
    }

    public void setLoginIdParam(String loginIdParam) {
        this.loginIdParam = loginIdParam;
    }

    public String getPasswordParam() {
        return passwordParam;
    }

    public void setPasswordParam(String passwordParam) {
        this.passwordParam = passwordParam;
    }

    public String getLoginIdField() {
        return loginIdField;
    }

    public void setLoginIdField(String loginIdField) {
        this.loginIdField = loginIdField;
    }

    public Authentication<T> getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication<T> authentication) {
        this.authentication = authentication;
    }

    public AutoLoginCookieSetter getAutoLoginCookieSetter() {
        return autoLoginCookieSetter;
    }

    public void setAutoLoginCookieSetter(AutoLoginCookieSetter autoLoginCookieSetter) {
        this.autoLoginCookieSetter = autoLoginCookieSetter;
    }

    public DAO<T> getDao() {
        return dao;
    }

    public void setDao(DAO<T> dao) {
        this.dao = dao;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getEncryptedPasswordField() {
        return encryptedPasswordField;
    }

    public void setEncryptedPasswordField(String encryptedPasswordField) {
        this.encryptedPasswordField = encryptedPasswordField;
    }
    
    
}
