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
package arena.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Utility class for generating and parsing XML strings
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class XMLUtils {
    public final static String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public final static String FORMAT_TAG = "dateFormat";
    private final static DateFormat sdfDefaultXML = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    /**
     * Searches the node for content of type Long. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static Integer extractIntegerFromElement(Node element) {
        // Get the content and try to parse for a long
        String nodeContent = extractStringFromElement(element);

        if (nodeContent == null) {
            return null;
        } else {
            try {
                return new Integer(nodeContent);
            } catch (NumberFormatException err) {
                LogFactory.getLog(XMLUtils.class).warn("Invalid data in integer field: " + nodeContent);

                return null;
            }
        }
    }

    /**
     * Searches the node for content of type Long. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static byte[] extractByteArrayFromElement(Node element) {
        // Get the content and try to parse for a base 64 array
        String nodeContent = extractStringFromElement(element);

        if (nodeContent == null) {
            return null;
        } else {
            return Base64.decodeBase64(nodeContent);
        }
    }

    /**
     * Searches the node for content of type Long. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static Long extractLongFromElement(Node element) {
        // Get the content and try to parse for a long
        String nodeContent = extractStringFromElement(element);

        if (nodeContent == null) {
            return null;
        } else {
            try {
                return Long.valueOf(nodeContent);
            } catch (NumberFormatException err) {
                LogFactory.getLog(XMLUtils.class).warn("Invalid data in long field: " + nodeContent);

                return null;
            }
        }
    }

    /**
     * Searches the node for content of type Float. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static Float extractFloatFromElement(Node element) {
        // Get the content and try to parse for a long
        String nodeContent = extractStringFromElement(element);

        if (nodeContent == null) {
            return null;
        } else {
            try {
                return new Float(nodeContent);
            } catch (NumberFormatException err) {
                LogFactory.getLog(XMLUtils.class).warn("Invalid data in float field: " + nodeContent);
                return null;
            }
        }
    }

    /**
     * Searches the node for content of type Long. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static Date extractDateFromElement(Node element) {
        // Get the content and try to parse for a long
        String format = searchForAttribute(element, FORMAT_TAG);
        String nodeContent = extractStringFromElement(element);

        if (nodeContent == null) {
            return null;
        } else {
            try {
                if (format == null) {
                    synchronized (sdfDefaultXML) {
                        return sdfDefaultXML.parse(nodeContent);
                    }
                } else {
                    DateFormat sdfXML = new SimpleDateFormat(format);
                    synchronized (sdfDefaultXML) {
                        return sdfXML.parse(nodeContent);
                    }
                }
            } catch (ParseException err) {
                LogFactory.getLog(XMLUtils.class).warn( 
                        "Invalid data in date field: " + nodeContent + " (format=" + format + ")", err);
                return null;
            }
        }
    }

    /**
     * Searches the node for content of type Long. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static String extractStringFromElement(Node element) {
        if (element == null) {
            return null;
        } else if (element.getFirstChild() == null) {
            return null;
        } else if (element.getFirstChild().getNodeValue() == null) {
            return null;
        } else {
            // Get all the children
            NodeList children = element.getChildNodes();
            StringBuffer output = new StringBuffer();
            for (int n = 0; n < children.getLength(); n++) {
                Node child = children.item(n);
                if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                    output.append(child.getFirstChild().getNodeValue());
                } else if (child.getNodeType() == Node.CDATA_SECTION_NODE){
                    output.append(child.getFirstChild().getNodeValue());
                } else if (child.getNodeType() == Node.ENTITY_NODE){
                    output.append(child.getFirstChild().getNodeValue());
                } else if (child.getNodeType() == Node.TEXT_NODE){
                    output.append(child.getNodeValue());
                }
            }
            return output.toString().trim();
        }
    }

    /**
     * Searches the node for content of type Long. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static String[] extractStringArrayFromElement(Node element) {
        // Get all the children
        NodeList children = element.getChildNodes();
        List<String> output = new ArrayList<String>();

        for (int n = 0; n < children.getLength(); n++) {
            Node child = children.item(n);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                output.add(extractStringFromElement(child));
            }
        }

        return output.toArray(new String[output.size()]);
    }

    /**
     * Searches the node for content of type Long. If non-long content is found,
     * it logs a warning and returns null.
     */
    public static Boolean extractBooleanFromElement(Node element) {
        String value = extractStringFromElement(element);

        return (value == null) ? null : new Boolean(value);
    }

    /**
     * Searches for a particular attribute attached to a node. Returns null
     * if not found
     */
    public static String searchForAttribute(Node content, String attributeName) {
        if (content == null) {
            return null;
        } else if (attributeName == null) {
            return null;
        }

        Node node = content.getAttributes()
                           .getNamedItem(attributeName);

        if (node == null) {
            return null;
        } else {
            return node.getNodeValue();
        }
    }

    /**
     * Gets an attribute value by name, returning null if not found
     */
    public static String getAttributeByName(Node content, String attributeName) {
        if (content != null) {
            NamedNodeMap atts = content.getAttributes();

            if (atts == null) {
                return null;
            }

            Node att = atts.getNamedItem(attributeName);

            if (att != null) {
                return att.getNodeValue();
            }
        }

        return null;
    }

    public static boolean getBooleanAttributeByName(Node content, String attributeName, boolean defaultTrue) {
        String value = getAttributeByName(content, attributeName);

        if (defaultTrue) {
            return (value == null) || (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"));
        } else {
            return (value != null) && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"));
        }
    }

//    /**
//     * Serializes an entire collection to an XML tree. Important - for subclasses
//     * of ValueObject, the toXML method is called on each element, otherwise
//     * toString is called.
//     *
//     * @param coll The collection to be serialized.
//     * @param childName The xml tag name for each child element.
//     */
//    public static String collectionToXML(Collection<?> coll, String childName) {
//        if (coll == null) {
//            return "";
//        }
//
//        StringBuffer out = new StringBuffer();
//        int counter = 0;
//        Map<String,Object> atts = new HashMap<String,Object>();
//        for (Iterator<?> i = coll.iterator(); i.hasNext(); counter++) {
//            Object item = i.next();
//
//            atts.clear();
//            atts.put("listIndex", counter + "");
//
//            // Serialize this child object
//            String key = childName;
//            Object outItem = item;
//            if (item == null) {
//                LogFactory.getLog(XMLUtils.class).warning("WARNING: Found null value in collection - counter=" + 
//                        counter + " childName=" + childName);
//                continue;
//            } else if (item instanceof Collection<?>) {
//                key = ParamUtils.nvl(childName, "collection");
//                outItem = new XMLContentString(collectionToXML((Collection<?>) item, null));
//            } else if (item instanceof Map<?,?>) {
//                key = ParamUtils.nvl(childName, "map");
//                outItem = new XMLContentString(mapToXML((Map<?,?>) item, null));
////            } else if (item instanceof OrderedMap<?,?>) {
////                key = ParamUtils.nvl(childName, "map");
////                outItem = new XMLContentString(orderedMapToXML((OrderedMap<?,?>) item, null));
//            }
//            out.append(element(key, outItem, atts));
//        }
//
//        return out.toString();
//    }
//
//    /**
//     * @deprecated Use XMLContentStrings instead
//     */
//    public static String mapToXML(Map<?,?> map, String childName, 
//            boolean isXHTML, String keyName) {
//        Map<Object,Object> rebuild = new HashMap<Object,Object>();
//        for (Iterator<?> i = map.keySet().iterator(); i.hasNext(); ) {
//            Object key = i.next();
//            Object value = map.get(key);
//            if (isXHTML && (value instanceof String)) {
//                rebuild.put(key, new XMLContentString((String) value));
//            } else {
//                rebuild.put(key, value);
//            }
//        }
//        return mapToXML(rebuild, childName);
//    }
//
//    /**
//     * Serializes an entire map to an XML tree. Important - for subclasses
//     * of ValueObject, the toXML method is called on each element, otherwise
//     * toString is called. The key is added added as an attribute to each child
//     * element.
//     *
//     * @param map The map to be serialized.
//     * @param childName The xml tag name for each child element.
//     */
//    public static String mapToXML(Map<?,?> map, String childName) {
//        if (map == null) {
//            return "";
//        }
//
//        // Alphabeticise the keyset
//        Object keyset[] = map.keySet().toArray();
//        Arrays.sort(keyset);
//
//        StringBuffer out = new StringBuffer();
//        Map<String,Object> atts = new HashMap<String,Object>();
//        for (int n = 0; n < keyset.length; n++) {
//            String key = (String) keyset[n];
//            Object value = map.get(key);
//
//            atts.clear();
//            atts.put("key", key);
//            Object outValue = value;
//
//            // Serialize this child object
//            if (value == null) {
//                LogFactory.getLog(XMLUtils.class).warning("WARNING: Found null value in map - key=" + 
//                        key + " childName=" + childName);
//            } else if (value instanceof Collection<?>) {
//                childName = ParamUtils.nvl(childName, "collection");
//                outValue = new XMLContentString(collectionToXML((Collection<?>) value, null));
//            } else if (outValue instanceof Map<?,?>) {
//                childName = ParamUtils.nvl(childName, "map");
//                outValue = new XMLContentString(mapToXML((Map<?,?>) value, null));
//            } else if (outValue instanceof OrderedMap<?,?>) {
//                childName = ParamUtils.nvl(childName, "map");
//                outValue = new XMLContentString(orderedMapToXML((OrderedMap<?,?>) value, null));
//            }
//            out.append(element(childName, outValue, atts));
//        }
//
//        return out.toString();
//    }

//    /**
//     * @deprecated Use XMLContentStrings instead
//     */
//    public static String orderedMapToXML(OrderedMap<?,?> map, String childName, 
//            boolean isXHTML, String keyName) {
//        OrderedMap<Object,Object> rebuild = new OrderedMap<Object,Object>();
//        for (Iterator<?> i = map.keyList().iterator(); i.hasNext(); ) {
//            Object key = i.next();
//            Object value = map.get(key);
//            if (isXHTML && (value instanceof String)) {
//                rebuild.put(key, new XMLContentString((String) value));
//            } else {
//                rebuild.put(key, value);
//            }
//        }
//        return orderedMapToXML(rebuild, childName);
//    }
//    
//    /**
//     * Serializes an entire map to an XML tree. Important - for subclasses
//     * of ValueObject, the toXML method is called on each element, otherwise
//     * toString is called. The key is added added as an attribute to each child
//     * element.
//     *
//     * @param map The map to be serialized.
//     * @param childName The xml tag name for each child element.
//     */
//    public static String orderedMapToXML(OrderedMap<?,?> map, String childName) {
//        if (map == null) {
//            return "";
//        }
//
//        StringBuffer out = new StringBuffer();
//        Map<String,Object> atts = new HashMap<String,Object>();
//        int n = 0;
//        for (Iterator<?> i = map.keyList().iterator(); i.hasNext(); n++) {
//            Object key = i.next();
//            Object value = map.get(key);
//
//            atts.clear();
//            atts.put("key", "" + key);
//            atts.put("listIndex", "" + n);
//            Object outValue = value;
//
//            // Serialize this child object
//            if (value == null) {
//                LogFactory.getLog(XMLUtils.class).warning("WARNING: Found null value in map - key=" + 
//                        key + " childName=" + childName);
//            } else if (value instanceof Collection<?>) {
//                childName = ParamUtils.nvl(childName, "collection");
//                outValue = new XMLContentString(collectionToXML((Collection<?>) value, null));
//            } else if (outValue instanceof Map<?,?>) {
//                childName = ParamUtils.nvl(childName, "map");
//                outValue = new XMLContentString(mapToXML((Map<?,?>) value, null));
//            } else if (outValue instanceof OrderedMap<?,?>) {
//                childName = ParamUtils.nvl(childName, "map");
//                outValue = new XMLContentString(orderedMapToXML((OrderedMap<?,?>) value, null));
//            }
//            out.append(element(childName, outValue, atts));
//        }
//
//        return out.toString();
//    }
//    

    public static Document parseStreamToXML(InputStream in) {
        try {
            // Use JAXP to create a document builder
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

            builderFactory.setExpandEntityReferences(false);
            builderFactory.setValidating(false);
            builderFactory.setNamespaceAware(true);
            builderFactory.setIgnoringComments(true);
            builderFactory.setCoalescing(true);
            builderFactory.setIgnoringElementContentWhitespace(true);

            return builderFactory.newDocumentBuilder()
                                 .parse(in);
        } catch (ParserConfigurationException errParser) {
            throw new RuntimeException("Error getting XML parser", errParser);
        } catch (SAXException errSax) {
            throw new RuntimeException("Error parsing XML files", errSax);
        } catch (IOException errIO) {
            throw new RuntimeException("Error parsing XML files", errIO);
        }
    }

    public static Document parseStreamToXML(Reader in) {
        try {
            // Use JAXP to create a document builder
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

            builderFactory.setExpandEntityReferences(false);
            builderFactory.setValidating(false);
            builderFactory.setNamespaceAware(true);
            builderFactory.setIgnoringComments(true);
            builderFactory.setCoalescing(true);
            builderFactory.setIgnoringElementContentWhitespace(true);

            return builderFactory.newDocumentBuilder()
                                 .parse(new InputSource(in));
        } catch (ParserConfigurationException errParser) {
            throw new RuntimeException("Error getting XML parser", errParser);
        } catch (SAXException errSax) {
            throw new RuntimeException("Error parsing XML files", errSax);
        } catch (IOException errIO) {
            throw new RuntimeException("Error parsing XML files", errIO);
        }
    }

    public static Document parseFileToXML(File in) {
        try {
            InputStream defXML = new FileInputStream(in);
            Document defDoc = parseStreamToXML(defXML);
            defXML.close();
            return defDoc;
        } catch (IOException errIO) {
            throw new RuntimeException("Error parsing XML files", errIO);
        }
    }
    
    public static Document createDocument() {
        try {
            // Use JAXP to create a document builder
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

            builderFactory.setExpandEntityReferences(false);
            builderFactory.setValidating(false);
            builderFactory.setNamespaceAware(true);
            builderFactory.setIgnoringComments(true);
            builderFactory.setCoalescing(true);
            builderFactory.setIgnoringElementContentWhitespace(true);

            return builderFactory.newDocumentBuilder()
                                 .newDocument();
        } catch (ParserConfigurationException errParser) {
            throw new RuntimeException("Error getting XML parser", errParser);
        }
    }
    
    public static Node createTextElement(Document doc, String name, String content) {
        Node elementNode = doc.createElement(name);
        Node textNode = doc.createTextNode(content);
        elementNode.appendChild(textNode);
        return elementNode;
    }
    
    public static void addAttribute(Document doc, Node parentNode, String name, String content) {
        Attr attNode = doc.createAttribute(name);
        attNode.setNodeValue(content);
        parentNode.getAttributes().setNamedItem(attNode);
    }
    
    public static void writeOutAttributesForNode(Map<?,?> attributes, Node node) {
        if (attributes != null) {
            // Add attributes
            for (Iterator<?> i = attributes.keySet().iterator(); i.hasNext(); ) {
                Object key = i.next();
                Object value = attributes.get(key);
                if ((key != null) && (value != null)) {
                    Attr attNode = node.getOwnerDocument().createAttribute(key.toString());
                    attNode.setNodeValue(value.toString());
                    node.getAttributes().setNamedItem(attNode);
                }
            }
        }
    }
    
    public static void writeOutAttributesForNode(String[][] attributes, Node node) {
        if (attributes != null) {
            // Add attributes
            for (int n = 0; n < attributes.length; n++) {
                String key = attributes[n][0];
                String value = attributes[n][1];
                if ((key != null) && (value != null)) {
                    Attr attNode = node.getOwnerDocument().createAttribute(key.toString());
                    attNode.setNodeValue(value.toString());
                    node.getAttributes().setNamedItem(attNode);
                }
            }
        }
    }
    

    public static String getStringFromDocument(Document doc) throws Exception {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }
}
