package org.nism.tellthetime;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	private AnalogClockFace acf;
	private Game game;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    	acf.mQuantum = 10;
    	
        XmlResourceParser xml = this.getResources().getXml(org.nism.tellthetime.R.xml.gamespec);
        Game g = new Game(xml);
    }
}
