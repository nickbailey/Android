/**
 * 
 */
package org.nism.tellthetime;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;

/**
 * @author nick
 *
 */
public class Game {
	/**
	 * Utility method to read all supplied attributes into a Map
	 * 
	 * @param parser the parser to query
	 * @return Map of name, value pairs for each attribute
	 * @throws Exception
	 */
	private Map<String,String>  getAttributes(XmlPullParser parser) throws Exception {
	    Map<String,String> attrs=null;
	    int acount=parser.getAttributeCount();
	    if(acount != -1) {
	        attrs = new HashMap<String,String>(acount);
	        for(int x=0; x<acount; x++) {
	            attrs.put(parser.getAttributeName(x), parser.getAttributeValue(x));
	        }
	    }
	    else {
	        throw new Exception("Required entity attributes missing");
	    }
	    return attrs;
	}
	
	public Game(XmlResourceParser xpp) {
		try {
			System.out.println("Ready to read gamespec file");
			
			int eventType = xpp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            if(eventType == XmlPullParser.START_DOCUMENT) {
	                System.out.println("Start document");
	            } else if(eventType == XmlPullParser.START_TAG) {
	                System.out.println("Start tag "+xpp.getName());
	                if (xpp.getName().equals("level")) {
	                	Map<String,String> attributes = getAttributes(xpp);
	                	for (Map.Entry<String, String> entry : attributes.entrySet()) {
	                	    System.out.println(entry.getKey() + " => " + entry.getValue());
	                	}
	                }
	            } else if(eventType == XmlPullParser.END_TAG) {
	                System.out.println("End tag "+xpp.getName());
	            } else if(eventType == XmlPullParser.TEXT) {
	                System.out.println("Text "+xpp.getText());
	            }
	            eventType = xpp.next();
	        }
	
	        System.out.println("gamespec ends");

		} catch (java.lang.Exception e) {
			System.out.println("Failure reading gamespec.xml");
			e.printStackTrace();
		}
	}
}
