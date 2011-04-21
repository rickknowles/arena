package arena.translation;

import java.util.Map;

import arena.utils.StringUtils;



public class FixedTranslationSet implements TranslationSet {
//    private final Log log = LogFactory.getLog(TranslationSet.class);
    
    private Map<String,String> translations;
    private String language;
    
    public FixedTranslationSet(Map<String,String> translations) {
        this.translations = translations;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String translate(String template, Object... params) {
//        log.info("Translating: " + template);
        String translated = null;
        if (translations != null) {
            // no sync required, read only
            translated = (String) translations.get(template);
        }
        if (translated == null) {
            translated = template;
        }
        if (params != null) {
            String tokens[][] = new String[params.length][2];
            for (int n = 0; n < params.length; n++) {
                tokens[n][0] = "[#" + (n + 1) + "]";
                tokens[n][1] = params[n] == null ? "" : params[n].toString();
            }
            translated = StringUtils.stringReplace(translated, tokens);
        }
        return translated;
    }

}
