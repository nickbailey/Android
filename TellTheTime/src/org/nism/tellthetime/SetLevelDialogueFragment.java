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

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialogue_set_level, null))
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
        
        return builder.create();

     }
    
}
