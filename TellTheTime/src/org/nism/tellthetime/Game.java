/**
 * 
 */
package org.nism.tellthetime;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Random;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;
import android.widget.TextView;

/**
 * @author nick
 *
 */
public final class Game {
	private ArrayList<LevelSpec> levelSpecs;	// Properties of each game level
	private RoundScores roundScores;			// Scores for the last rounds
	private AnalogClockFace theClockFace;		// Clock Face for player input
	private Scoreboard theScoreboard;			// Display of current & running av. score
	private TextView theTimeText;				// Text displaying the question
	private TextView thePromptText;				// Text to encourage the player
	private int theTime;						// Time in Minutes to display
	private Announcer theAnnouncer;				// Speech engine
	
	private int level;							// Current level
	private int question;						// Question number in current level
	private int attempts;						// Attempts at current question
	
	private Random rng = new Random();
	
	private class RoundScores extends ArrayList<Float> {

		private static final long serialVersionUID = 2851820252911102669L;

		void setAll(int newsize, float val) {
			ensureCapacity(newsize);
			// Capacity and size aren't the same!!
			while (size() < newsize) add(null);
			while (--newsize >= 0) set(newsize, val);
		}
		
		/**
		 * Average the first el elements
		 * @param el The number of elements to average (starts at 0)
		 * @return Average value
		 */
		float getAverage(int el) {
			float sum = 0.0f;
			for (int i=0; i<el; i++) sum += get(i);
			return sum/size();
		}
	}
	
	/**
	 * A class describing the parameters of one level in the game
	 * Note that none of the fields in this class can be primitive
	 * because their valueOf(String) method is called during initialisation
	 */
	private class LevelSpec {
        public Integer initialScore        = -1;
        public Integer timeQuantum         = -1;
        public Integer minHandStep         = -1;
        public Integer attemptsPerQuestion = -1;
        public Integer gamesPerRound       = -1;
        public Integer gamesAveraged       = -1;
        public Float   promotionScore      = -1.0f;
        public Float   demotionScore       = -1.0f;
        
        /**
         * Check that the current level parameters are valid
         * @return true if a level can be played
         */
        public boolean isValid() {
        	return initialScore        >= 0   &&
        		   timeQuantum         >= 1   &&
        		   timeQuantum         <= 30  &&
        		   minHandStep         >= 1   &&
        		   attemptsPerQuestion >= 1   &&
        		   gamesPerRound       >= 1   &&
        		   gamesAveraged       >= 1   &&
        		   promotionScore      >  0   &&
        		   demotionScore       >= 0   &&
        		   promotionScore > demotionScore;
        }
	}
	
	/**
	 * Utility method to read all supplied attributes into a Map
	 * 
	 * @param parser the parser to query
	 * @return Map of name, value pairs for each attribute
	 * @throws Exception
	 */
	private Map<String,String> getAttributes(XmlPullParser parser) throws Exception {
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
	                	    else {                           // Find the appropriate valueOf(String) method
	                	    	Method strParser = ft.getMethod("valueOf", new Class[]{String.class});
	                	    	f.set(ls, strParser.invoke(ft, val));
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
	
	private static String intToWords(int n) {
		final String[] units_en = {"", "one", "two", "three", "four", "five",
				                   "six", "seven", "eight", "nine", "ten",
				                   "eleven", "twelve", "thirteen", "fourteen",
				                   "fifteen", "sixteen", "seventeen", "eighteen",
				                   "nineteen"};
		final String[] tens_en  = {"", "", "twenty", "thirty", "forty",
			                       "fifty", "sixty", "seventy", "eighty",
			                       "ninety"};
		
		if (n < 20) return units_en[n];
		String t = tens_en[n/10];
		String u = units_en[n%10];
		if (u != "") t = t + "-"; // Numbers less than 20 handled above
		return t + u;	
	}
	
	private static String capitaliseWord(String w) {
		return Character.toUpperCase(w.charAt(0)) + w.substring(1);
	}
	
	/**
	 * Dreadful hack to convert time to English text
	 * @param t time in minutes
	 * @return time as text
	 */
	private static String timeToWords(int t) {
		int m = t%60;
		int h = t/60;
		String prefix, hour;
		switch (m) {
		case 0:  prefix = ""; break;
		case 15: prefix = "A quarter past"; break;
		case 30: prefix = "Half past"; break;
		case 45: prefix = "A quarter to"; break;
		default:
			if (m < 30)
				prefix = capitaliseWord(intToWords(m)) + " minutes past";
			else
				prefix = capitaliseWord(intToWords(60-m)) + " minutes to";
		}
		if (m > 30) h++;
		if (h == 0) h=12;
		hour = intToWords(h);
		if (m == 0) {
			return capitaliseWord(hour) + " o'clock";
		} else { 
			return prefix + "\n" + hour;
		}
	}
	
	/**
	 * Make a new random time for the next question
	 * 
	 * @param tq Time Quantum. Time will be a multiple of tq mins after the hour
	 * @return Random time in minutes after 00h00
	 */
	private int newRandomTime(int tq) {
		final int minsteps = 60/tq;
		int newTime;
		do {
			newTime = 60*rng.nextInt(12) + tq*rng.nextInt(minsteps);
		} while (newTime == theTime);
		return newTime;
	}
	
	private void initLevel() {
		//System.out.println("INITLEVEL");
		
		// Set a new time (in minutes) for the first game
		final LevelSpec ls = levelSpecs.get(level);
		theTime = newRandomTime(ls.timeQuantum);
		question = 0;
		setUIComponents(0f, ls.initialScore, level);
	}
	
	/**
	 * Make all UI components have state consistent with the game.
	 * This is called to restore state as well from initLevel()
	 *
	 * @param currentScore The desired game score to display
	 * @param avScore The average score this round (services running average calcs)
	 * @param level The current game level
	 */
	private void setUIComponents(float currentScore, float avScore, int level) {
		final LevelSpec ls = levelSpecs.get(level);

		// Set up score board data (our owning activity will cause a redraw)
		roundScores.setAll(ls.gamesAveraged, avScore);
		theScoreboard.mCurrentScore = currentScore;
		theScoreboard.mAverageScore = avScore;
		theScoreboard.mMaxScore     = ls.gamesPerRound;
		theScoreboard.mStars        = level+1;

		// Set up skill-appropriate clock face behaviour
		theTimeText.setText(timeToWords(theTime));
		theClockFace.mQuantum = ls.minHandStep;
	}
	
	public Game(XmlResourceParser xpp, Scoreboard sb, AnalogClockFace acf,
			    TextView tt, TextView pt, Announcer sp) {
		roundScores = new RoundScores();
		
		levelSpecs = readLevels(xpp);
		theScoreboard = sb;
		theClockFace = acf;
		theTimeText = tt;
		thePromptText = pt;
		theAnnouncer = sp;

		level = 0;
		sp.say("Let's tell the time");
		thePromptText.setText("Move the hands\nthen press the button");
	}
	
	/**
	 * Jump immediately to a new level. Levels start at 1, and out-of-range
	 * arguments are silently ignored.
	 * 
	 * @param l The new level.
	 */
	public void setLevel(int l) {
		if (l > 0 && l <= levelSpecs.size()) {
			level = l-1;
			initLevel();
		}
	}
	
	/**
	 * Change the state of the game according to the time entered and the level
	 * Displays the result on the scoreboard.
	 */
	public void submit() {
		final LevelSpec ls = levelSpecs.get(level);
		final int timeSet = theClockFace.getTime();

		String prompt = new String();
		
		if (timeSet == theTime) { // Player entered the correct time

			theScoreboard.mCurrentScore += 1.0f;
			prompt = "That's right!";
			question++;
			theTime = newRandomTime(ls.timeQuantum);
				
		} else { // The wrong time was entered
			prompt = "Oh no! I wanted " + timeToWords(theTime).toLowerCase() + 
					 ", not "+ timeToWords(timeSet).toLowerCase() + ".";
			// Handle multiple attempts if this level permits it.
			attempts++;
			if (attempts < ls.attemptsPerQuestion) {
				// Player may re-attempt the same question
				if (attempts < ls.attemptsPerQuestion - 1)
					prompt += "\nTry again.";
				else
					prompt += "\nOne last try.";
			} else {
				// No more attempts permitted.
				prompt += "\nLet's move on.";
				attempts = 0;
				theTime = newRandomTime(ls.timeQuantum);
				question++;
			}
		}
				
		if (question >= ls.gamesPerRound) { // End of this game: save result
			roundScores.remove(0);
			roundScores.add(theScoreboard.mCurrentScore);
			if (!prompt.equals("")) prompt += " ";
			prompt += "New game!";
			question = 0;
			theScoreboard.mAverageScore = 
					roundScores.getAverage(ls.gamesAveraged);
			theScoreboard.mCurrentScore = 0.0f;

			// Check for a level change
			float rs = roundScores.getAverage(ls.gamesAveraged);			
			if (rs >= ls.promotionScore) {
				// Qualifies for next level; assume this is allowed
				level++;
				if (level < levelSpecs.size())
					prompt = "WELL DONE!\nOn to level " + (level+1);
				else { // Already at the highest level
					level--;
					prompt = "BRILLIANT!\nThere are no more levels,\n" +
							 "but play on if you wish.";
				}
				initLevel();
			} else if (rs <= ls.demotionScore && level > 0) {
				prompt = "This is too hard. Let's drop to level " + level;
				level--;
				initLevel();
			}
		}

		theAnnouncer.say(prompt);
		thePromptText.setText(prompt);
		String tt = timeToWords(theTime);
		theAnnouncer.say("show me "+tt);
		theTimeText.setText(tt);
		
		//System.out.println("Question "+question+" Level "+level+" has "+ls.gamesPerRound);
	}
	
	//
	// Utility methods to intialise, save and restore game state
	//
	public int getInitAverageScore() { return levelSpecs.get(0).initialScore; }
	public int getInitScore() { return levelSpecs.get(0).initialScore; }
	public int getLevel() { return level; }
	public int getMaxlevel() { return levelSpecs.size(); }
	public int getQuestion() { return question; }
	public int getAttempts() { return attempts; }
	public int getTime() { return theTime; }
	public float getAverageScore() { return theScoreboard.mAverageScore; }
	public float getCurrentScore() { return theScoreboard.mCurrentScore; }
	
	public void restoreState(int level, int question,
			                 int time, int attempts,
			                 float averageScore, float currentScore) {
		this.level = level;
		this.question = question;
		this.attempts = attempts;
		this.theTime = time>=0 ? time : newRandomTime(levelSpecs.get(level).timeQuantum);

		setUIComponents(currentScore, averageScore, level);
		theAnnouncer.say("show me "+timeToWords(theTime));
	}
}
