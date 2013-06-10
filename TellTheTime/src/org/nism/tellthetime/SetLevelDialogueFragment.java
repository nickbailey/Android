/**
 * A dialogue fragment which allows the player to set the level
 * via the MainActivity menu
 */
package org.nism.tellthetime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author nick
 *
 */
public class SetLevelDialogueFragment extends DialogFragment {
	
	public interface SetLevelListener {
		public void setLevel(int newLevel);
	}

	// Use this instance of the interface to deliver action events
    SetLevelListener theListener;
    
    private int maxLevel;
    private int minLevel;
    
    public SetLevelDialogueFragment() {
    	super();
    }
    
    public void setMaxLevel(int min, int max) {
    	minLevel = min;
    	maxLevel = max;
    }
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            theListener = (SetLevelListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SetLevelListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	final Activity act = getActivity();
    	
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View theView = inflater.inflate(R.layout.dialogue_set_level, null);
        
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(theView)
               .setPositiveButton(R.string.dialogue_level_OK, new DialogInterface.OnClickListener() {
            	    @Override
            	    public void onClick(DialogInterface d, int id) {
            	            // Send the positive button event back to the host activity
            	     	   EditText et = (EditText)((AlertDialog)d).findViewById(R.id.dialogue_set_level);
            	     	   if (et==null) System.out.println("et really is null");
            	           theListener.setLevel(Integer.parseInt(et.getText().toString()));
            	    }
			})
               .setNegativeButton(R.string.dialogue_level_cancel, null);        
        
        AlertDialog ad = builder.create();
        
        Button upB = (Button)theView.findViewById(R.id.dialogue_level_up);
        upB.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
				AlertDialog d = (AlertDialog)getDialog();
				EditText et = (EditText)d.findViewById(R.id.dialogue_set_level);
				Integer etVal = Integer.valueOf(et.getText().toString());
				if (etVal < maxLevel) etVal++;
				et.setText(etVal.toString());
        	}
		});
        
        Button downB = (Button)theView.findViewById(R.id.dialogue_level_down);
        downB.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
				AlertDialog d = (AlertDialog)getDialog();
				EditText et = (EditText)d.findViewById(R.id.dialogue_set_level);
				Integer etVal = Integer.valueOf(et.getText().toString());
				if (etVal > minLevel) etVal--;
				et.setText(etVal.toString());
        	}
		});
        
        return ad;
    }
    
}
