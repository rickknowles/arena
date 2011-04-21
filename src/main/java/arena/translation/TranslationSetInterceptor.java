package arena.translation;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;


public class TranslationSetInterceptor implements HandlerInterceptor {

    private String translationSetSelectorBeanParam = "translationSet";
    private TranslationSetSelectorBean translationSetSelectorBean;
    private LocaleResolver localeResolver;

    public boolean preHandle(HttpServletRequest req, HttpServletResponse rsp,
            Object handler) throws Exception {
//        String languageParam = req.getParameter(RouterConstants.PREFERRED_LANGUAGE_PARAM);
//        if (languageParam != null) {
//            HttpSession session = req.getSession(true);
//            session.setAttribute(RouterConstants.PREFERRED_LANGUAGE_PARAM, languageParam);
//        }
//        String language = RequestLanguageTranslationSet.getLanguage(req);
        Locale locale = this.localeResolver.resolveLocale(req);
        req.setAttribute(this.translationSetSelectorBeanParam, this.translationSetSelectorBean.getTranslationSet(locale));
        return true;
    }

    public void postHandle(HttpServletRequest req, HttpServletResponse rsp,
            Object handler, ModelAndView mav) throws Exception {}
    
    public void afterCompletion(HttpServletRequest req,
            HttpServletResponse rsp, Object handler, Exception err)
            throws Exception {}
    
    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public void setTranslationSetSelectorBeanParam(String translationSetSelectorBeanParam) {
        this.translationSetSelectorBeanParam = translationSetSelectorBeanParam;
    }

    public void setTranslationSetSelectorBean(TranslationSetSelectorBean translationSetSelectorBean) {
        this.translationSetSelectorBean = translationSetSelectorBean;
    }

}
