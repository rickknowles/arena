package arena.translation;

import java.util.Locale;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;


/**
 * Request scoped translation set that knows how to get the user's language from the session.
 * NOTE: use an AOP-scoped proxy when injecting as a dependency
 */
public class RequestLanguageTranslationSet implements TranslationSet {

    private TranslationSetSelectorBean translationSetSelectorBean;
    private LocaleResolver localeResolver;
    private String translationSetParamName = "RequestLanguageTranslationSet.SELECTED_TRANSLATION";
    
    public String translate(String template, Object... params) {
        RequestAttributes request = RequestContextHolder.getRequestAttributes();
        TranslationSet ts = (TranslationSet) request.getAttribute(this.translationSetParamName, RequestAttributes.SCOPE_REQUEST);
        if (ts == null) {
            ServletRequestAttributes sra = (ServletRequestAttributes) request;
            Locale locale = this.localeResolver.resolveLocale(sra.getRequest());
            ts = translationSetSelectorBean.getTranslationSet(locale);
            request.setAttribute(this.translationSetParamName, ts, RequestAttributes.SCOPE_REQUEST);
        }
        return ts.translate(template, params);
    }

    public void setTranslationSetSelectorBean(TranslationSetSelectorBean translationSetSelectorBean) {
        this.translationSetSelectorBean = translationSetSelectorBean;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public void setTranslationSetParamName(String translationSetParamName) {
        this.translationSetParamName = translationSetParamName;
    }
}
