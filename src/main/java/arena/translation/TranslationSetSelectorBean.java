/*
 * Generator Runtime Servlet Framework
 * Copyright (C) 2004 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.translation;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import arena.utils.StringUtils;
import arena.utils.XMLUtils;

public class TranslationSetSelectorBean implements ServletContextAware {
    private final Log log = LogFactory.getLog(TranslationSetSelectorBean.class);
    
    private ServletContext servletContext;
    private String defaultBasePath = "/WEB-INF/translations/";
    private Map<?,?> translationSheets = StringUtils.makeLookupTable(new String[][] {
            {"en", "/WEB-INF/translations/english.xml"},
    });
    
    private Map<String,TranslationSet> translationSets = new HashMap<String,TranslationSet>();
    
    public TranslationSet getTranslationSet(Locale locale) {
        String language = locale.getLanguage();
        synchronized (this.translationSets) {
            TranslationSet translationSet = (TranslationSet) this.translationSets.get(language.toLowerCase());
            if (translationSet == null) {
                Object translationSheet = null;
                if (this.translationSheets != null) {
                    translationSheet = this.translationSheets.get(language); 
                }
                if (translationSheet == null) {
                    translationSheet = this.defaultBasePath + language + ".xml";
                }
                if (this.servletContext != null) {
                    translationSheet = servletContext.getRealPath(translationSheet.toString());
                }
                File file = new File(translationSheet.toString());
                if (!file.isFile()) {
                    log.warn("Translation file not found: " + translationSheet);
                }
                Document doc = XMLUtils.parseFileToXML(file);
                NodeList items = doc.getDocumentElement().getChildNodes();
                Map<String,String> translations = new Hashtable<String,String>();
                for (int n = 0; n < items.getLength(); n++) {
                    Node node = items.item(n);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        String key = XMLUtils.getAttributeByName(node, "key");
                        String text = XMLUtils.extractStringFromElement(node);
                        if ((key != null) && (text != null)) {
                            translations.put(key, text);
                        }
                    }
                }
                log.info("Loaded " + translations.size() + " translations for " + language + " from " + translationSheet);
                translationSet = new FixedTranslationSet(translations);
                this.translationSets.put(language.toLowerCase(), translationSet);
            }
            return translationSet;
        }
    }
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setDefaultBasePath(String defaultBasePath) {
        this.defaultBasePath = defaultBasePath;
    }

    public void setTranslationSheets(Map<?, ?> translationSheets) {
        this.translationSheets = translationSheets;
    }
}
