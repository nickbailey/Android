package org.nism.tellthetime;

import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.content.Context;

public class Announcer implements TextToSpeech.OnInitListener {
	private TextToSpeech tts;
	
	Announcer(Context context) {
		tts = new TextToSpeech(context, this);
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.UK); 
            if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                System.out.println("This Language is not supported");
            } else {
                say("");
            }
		}
	}
		
	public void say(String text) {
		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	}
	
	public void flush() {
		tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
	}
	
	public void onDestroy() {
		tts.shutdown();
	}
}
