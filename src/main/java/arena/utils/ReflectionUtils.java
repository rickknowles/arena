/*
 * Generator Runtime Servlet Framework
 * Copyright (C) 2004 Rick Knowles
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
package arena.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

/**
 * Utility methods for dealing with class manipulation within the G-R framework. 
 * NOTE: this has now been ported to use commons-beanutils internally instead of bcel
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class ReflectionUtils {
    
    /**
     * Removes the package section from a fully qualified class name.
     */
    public static String extractClassNameWithoutPackage(String className) {
        int dotPos = className.lastIndexOf(".");
        if (dotPos != -1) {
            className = className.substring(dotPos + 1);
        }
        int dollarPos = className.lastIndexOf("$");
        if (dollarPos != -1) {
            className = className.substring(dollarPos + 1);
        }
        return className;
    }
    
    public static Object getAttributeUsingGetter(String attributeName, Object entity) {
        try {
            return PropertyUtils.getSimpleProperty(entity, attributeName);
        } catch (NoSuchMethodException err) {
            LogFactory.getLog(ReflectionUtils.class).error(err);
            throw new RuntimeException("Error in getter", err);   
        } catch (IllegalAccessException err) {
            LogFactory.getLog(ReflectionUtils.class).error(err);
            throw new RuntimeException("Error in getter", err);   
        } catch (InvocationTargetException err) {
            LogFactory.getLog(ReflectionUtils.class).error(err);
            throw new RuntimeException("Error in getter", err);
        }
    }
    public static Object getAttributeUsingGetter(String attributeName, Object entity, Class<?> entityClass) {
        return getAttributeUsingGetter(attributeName, entity);
    }

    public static String[] getAttributeNamesUsingGetter(Class<?> entityClass) {
//        return BeanUtils.getPropertyDescriptors(entityClass);
        Class<?> clsMe = entityClass;
        List<String> voFieldNames = new ArrayList<String>();

        while (clsMe != null) {
            Field[] fields = clsMe.getDeclaredFields();

            for (int n = 0; n < fields.length; n++) {
                int modifiers = fields[n].getModifiers();
                if (!Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                    String fieldName = fields[n].getName();
                    try {
                        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(entityClass, fieldName);
                        if (pd.getReadMethod() != null) {
                            voFieldNames.add(fieldName);
                        }
                    } catch (Throwable err) {
                        // skip
                    }
                }
            }

            // Loop back to parent class
            clsMe = clsMe.getSuperclass();
        }
        return voFieldNames.toArray(new String[voFieldNames.size()]);
    }    
    
    public static Class<?> getAttributeTypeUsingGetter(String attributeName, Class<?> entityClass) {
        return BeanUtils.getPropertyDescriptor(entityClass, attributeName).getPropertyType();
    }
    
    public static void setAttributeUsingSetter(String attributeName, Object entity, Object attribute)  {
        
        try {
            PropertyUtils.setSimpleProperty(entity, attributeName, attribute);
        } catch (NoSuchMethodException err) {
            LogFactory.getLog(ReflectionUtils.class).error(err);
            throw new RuntimeException("Error in setter", err);   
        } catch (IllegalAccessException err) {
            LogFactory.getLog(ReflectionUtils.class).error(err);
            throw new RuntimeException("Error in setter", err);   
        } catch (InvocationTargetException err) {
            LogFactory.getLog(ReflectionUtils.class).error(err);
            throw new RuntimeException("Error in setter", err);
        }
    }
    
    public static void setAttributeUsingSetter(String attributeName, Object entity, Object attribute, 
            Class<?> entityClass) throws NoSuchMethodException {
        setAttributeUsingSetter(attributeName, entity, attribute);
    }
//    
//    public static Object getPublicStaticFieldName(String fieldName, Class<?> entityClass) 
//            throws NoSuchFieldException {
//        PropertyUtils.get
//        return getAccessor(entityClass).getPublicField(fieldName);
//    }
    
    public static String toString(Object input) {
        if (input == null) {
            return null;
        }
        StringBuffer out = new StringBuffer();
        out.append("[");
        out.append(extractClassNameWithoutPackage(input.getClass().getName()));
        out.append(": ");
        String attNames[] = getAttributeNamesUsingGetter(input.getClass());
        for (int n = 0; n < attNames.length; n++) {
            out.append(n == 0 ? "" : ", ").append(attNames[n]).append("=");
            try {
                Object value = getAttributeUsingGetter(attNames[n], input);
                if ((value != null) && value.getClass().isArray()) {
                    value = Arrays.asList((Object []) value);
                }
                out.append(value);
            } catch (Throwable err) {
                throw new RuntimeException("Error in toString()", err);
            }
        }
        out.append("]");
        return out.toString();
    }
}
