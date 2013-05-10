package org.nism.tellthetime;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private AnalogClockFace acf;
	private Scoreboard sb;
	private TextView tt, pt;
	private Game game;
	private Announcer sp;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button enterb = (Button)findViewById(R.id.enter_button);
        enterb.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
        acf = (AnalogClockFace) findViewById(org.nism.tellthetime.R.id.clock_face);    	    	
    	sb = (Scoreboard)findViewById(org.nism.tellthetime.R.id.scoreboard);
    	tt = (TextView)findViewById(org.nism.tellthetime.R.id.time_text);
    	pt = (TextView)findViewById(org.nism.tellthetime.R.id.prompt_text);
    	sp = new Announcer(this);
    	
        XmlResourceParser xml = this.getResources().getXml(org.nism.tellthetime.R.xml.gamespec);

        game = new Game(xml, sb, acf, tt, pt, sp);
        sb.invalidate();
    }

    /**
     * Catch events created by the "check" button
     */
	@Override
	public void onClick(View v) {
		game.submit();
		sb.invalidate();
	}
	
	/**
	 * Save and restore game state when paused
	 */
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences.Editor pe = getPreferences(MODE_PRIVATE).edit();
		pe.putInt("GameLevel", game.getLevel());
		pe.putInt("GameQuestion", game.getQuestion());
		pe.putInt("GameTime", game.getTime());
		pe.putInt("GameAttempts", game.getAttempts());
		pe.putFloat("GameAvScore", game.getAverageScore());
		pe.putFloat("GameCurrentScore", game.getCurrentScore());
		
		pe.apply();
	}
	
	@Override
	public void onResume() {
		super.onResume();
 		SharedPreferences p = getPreferences(MODE_PRIVATE);
		game.restoreState(p.getInt("GameLevel", 0),
						  p.getInt("GameQuestion", 0),
						  p.getInt("GameTime", -1),
						  p.getInt("GameAttempts", 0),
						  p.getFloat("GameAvScore", game.getInitAverageScore()),
						  p.getFloat("GameCurrentScore", game.getInitScore()));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Just call the speech engine's closedown method
		sp.onDestroy();
	}
}
