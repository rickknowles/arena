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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Special tag to translate the contents using the translations cache for the selected language 
 */
public class TranslateTag extends BodyTagSupport {

    private String translationSet = "translationSet";
    private Object[] parameters = new Object[5];
    
	public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
    
    public <T> void setParameterList(Collection<T> parameters) {
        setParameters(new ArrayList<T>(parameters).toArray());
    }
    
    public void setParam1(Object p) {
        this.parameters[0] = p;
    }
    
    public void setParam2(Object p) {
        this.parameters[1] = p;
    }
    
    public void setParam3(Object p) {
        this.parameters[2] = p;
    }
    
    public void setParam4(Object p) {
        this.parameters[3] = p;
    }
    
    public void setParam5(Object p) {
        this.parameters[4] = p;
    }

    public void setTranslationSet(String translationSet) {
        this.translationSet = translationSet;
    }

    @Override
    public int doAfterBody() throws JspTagException {
        try {
            BodyContent bodyContent = super.getBodyContent();
            String bodyString = bodyContent.getString();
            JspWriter out = bodyContent.getEnclosingWriter();
            TranslationSet translation = (TranslationSet) pageContext.getRequest().getAttribute(this.translationSet);
            out.print(translation == null ? bodyString : translation.translate(bodyString, this.parameters));
            bodyContent.clear(); // empty buffer
            return SKIP_BODY;
        } catch (IOException e) {
            throw new RuntimeException("Error during translate tag", e);
        }
    }
}
