package arena.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.action.ClientSideUserState;
import arena.action.RequestState;

public class AutoLoginCookieSetter {
    private final Log log = LogFactory.getLog(AutoLoginCookieSetter.class);

    private PasswordEncrypter encrypter;
    private String autoLoginCookieName = "alc";
    
    private int autoLoginCookieAge = 7776000; // 90 days in seconds
    private String cookieDomain;
    private String cookiePath;

    private String salt = "AutoLoginCookieSetter";
    
    public void addAutoLoginCookies(RequestState requestState, String id) {
        if (this.autoLoginCookieName != null) {
            // Look up a cookie login value, and set it on the user
            String autoLoginCookieValue = saltAndEncrypt(id);
            
            ClientSideUserState cookies = requestState.getClientSideUserState();
            cookies.setAttribute(this.autoLoginCookieName, autoLoginCookieValue, 
                    this.autoLoginCookieAge, this.cookieDomain, this.cookiePath);
        }
    }
    
    public String getIdFromAutoLoginCookie(RequestState requestState) {
        try {
            ClientSideUserState cookies = requestState.getClientSideUserState();
            String encrypted = cookies.getAttribute(this.autoLoginCookieName);
            return encrypted == null ? null : desaltAndDecrypt(encrypted);
        } catch (Throwable err) {
            log.error("Error during auto-login cookie attempt, ignoring cookie", err);
            return null;
        }
    }
    
    protected String saltAndEncrypt(String id) {
        return encrypter.encrypt(salt + id + new StringBuffer(salt).reverse().toString());
    }
    
    protected String desaltAndDecrypt(String encrypted) {
        String decrypted = encrypter.decrypt(encrypted);
        return decrypted.substring(salt.length(), decrypted.length() - salt.length());
    }

    public void removeAutoLoginCookies(RequestState requestState) {
        ClientSideUserState cookies = requestState.getClientSideUserState();
//        if (this.usernameCookieName != null) {
//            cookies.removeAttribute(this.usernameCookieName, this.cookiePath);
//        }
        if (this.autoLoginCookieName != null) {
            cookies.removeAttribute(this.autoLoginCookieName, this.cookieDomain, this.cookiePath);
        }
    }

    public String getAutoLoginCookieName() {
        return autoLoginCookieName;
    }

    public void setAutoLoginCookieName(String autoLoginCookieName) {
        this.autoLoginCookieName = autoLoginCookieName;
    }

    public int getAutoLoginCookieAge() {
        return autoLoginCookieAge;
    }

    public void setAutoLoginCookieAge(int autoLoginCookieAge) {
        this.autoLoginCookieAge = autoLoginCookieAge;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public String getCookiePath() {
        return cookiePath;
    }

    public void setCookiePath(String cookiePath) {
        this.cookiePath = cookiePath;
    }

    public PasswordEncrypter getEncrypter() {
        return encrypter;
    }

    public void setEncrypter(PasswordEncrypter encrypter) {
        this.encrypter = encrypter;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
