/**
 * 
 */
package org.nism.tellthetime;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;

/**
 * @author nick
 *
 */
public class Game {
	protected ArrayList<LevelSpec> levelSpecs;
	
	/**
	 * A class describing the parameters of one level in the game
	 * Note that none of the fields in this class can be primitive
	 * because their valueOf(String) method is called during initialisation
	 */
	private class LevelSpec {
        public Integer initialScore  = -1;
        public Integer minHandStep   = -1;
        public Integer gamesPerRound = -1;
        public Integer gamesAveraged = -1;
        
        /**
         * Check that the current level parameters are valid
         * @return true if a level can be played
         */
        public boolean isValid() {
        	return initialScore  >= 0   &&
        		   minHandStep   >= 1   &&
        		   gamesPerRound >= 1   &&
        		   gamesAveraged >= 1;
        }
	}
	
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
	
	/**
	 * Read the game levels.
	 * 
	 * @param xpp XmlResourceParser from which levels are read
	 * @return Vector describing different levels of the game
	 */
	private final ArrayList<LevelSpec> readLevels(XmlResourceParser xpp) {
		ArrayList<LevelSpec> result = new ArrayList<LevelSpec>();
		final Class<LevelSpec> lsc = LevelSpec.class;
		try {
			System.out.println("Ready to read gamespec file");

			boolean readingGame = false;        // Only set to true after a <game> tag
			int eventType = xpp.getEventType();			
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            if (eventType == XmlPullParser.START_DOCUMENT) {
	                System.out.println("Start document");
	            } else if (eventType == XmlPullParser.START_TAG) {
	            	final String tagName = xpp.getName();
            		System.out.println("Start tag "+tagName);
	            	if (tagName.equals("game")) {
	            		System.out.println("Game specification begins");
	            		readingGame = true;
	            	} else if (xpp.getName().equals("level") && readingGame) {
	                	final Map<String,String> attributes = getAttributes(xpp);
	                	LevelSpec ls = new LevelSpec();
	                	for (Map.Entry<String, String> entry : attributes.entrySet()) {
	                		final String key = entry.getKey();
	                		final String val = entry.getValue();
	                	    System.out.println(key + " := " + val);
	                	    final Field f = lsc.getField(key);
	                	    final Class<?> ft = f.getType();
	                	    System.out.println("field type is "+ft);
	                	    if (f.get(ls) instanceof String) // If the target's a string...
	                	    	f.set(ls, val);              // ...simply assign it.
	                	    else {                           // Find the appropriate valueof(String) method
	                	    	Method strParser = ft.getMethod("valueOf", new Class[]{String.class});
	                	    	f.set(ls, strParser.invoke(f, val));
	                	    }
	                	}
	                	if (!ls.isValid())
	                		throw new Exception("Invialid level spec at tag " + (result.size()+1));
	                	result.add(ls);
	                }
	            } else if (eventType == XmlPullParser.END_TAG) {
	            	final String tagName = xpp.getName();
	                System.out.println("End tag "+tagName);
	                if (tagName.equals("game")) {
	                	System.out.println("Game specifcation ends");
	                	readingGame = false;
	                }
	            } else if (eventType == XmlPullParser.TEXT) {
	                System.out.println("Text "+xpp.getText());
	            }
	            eventType = xpp.next();
	        }
	
	        System.out.println("Document ends");
	
		} catch (java.lang.Exception e) {
			System.out.println("Failure reading gamespec.xml");
			e.printStackTrace();
		}
		return result;
	}
	
	public Game(XmlResourceParser xpp) {
		levelSpecs = readLevels(xpp);
	}
}
