/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.translation;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class LocaleInModelInterceptor extends HandlerInterceptorAdapter {

    private LocaleResolver localeResolver;
    private String countryCodeParamName = "locale.country";
    private String languageCodeParamName = "locale.language";
    private String localeParamName;
    
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
        if (modelAndView != null) {
            Locale locale = this.localeResolver.resolveLocale(request);
            if (this.countryCodeParamName != null) {
                modelAndView.addObject(this.countryCodeParamName, locale.getCountry());
            }
            if (this.languageCodeParamName != null) {
                modelAndView.addObject(this.languageCodeParamName, locale.getLanguage());
            }
            if (this.localeParamName != null) {
                modelAndView.addObject(this.localeParamName, locale);
            }
        }
    }

    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public String getCountryCodeParamName() {
        return countryCodeParamName;
    }

    public void setCountryCodeParamName(String countryCodeParamName) {
        this.countryCodeParamName = countryCodeParamName;
    }

    public String getLanguageCodeParamName() {
        return languageCodeParamName;
    }

    public void setLanguageCodeParamName(String languageCodeParamName) {
        this.languageCodeParamName = languageCodeParamName;
    }

    public String getLocaleParamName() {
        return localeParamName;
    }

    public void setLocaleParamName(String localeParamName) {
        this.localeParamName = localeParamName;
    }
}
