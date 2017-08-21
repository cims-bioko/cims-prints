package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

public class IntentFilterTest extends Activity {

    private final String TAG = "IntentFilterTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent i = new Intent("com.openandid.core.PIPE");
        i.putExtra("action_0", "com.openandid.core.SCAN");
        i.putExtra("action_1", "com.openandid.core.IDENTIFY");

		
		/*
		i.putExtra("left_finger_assignment", "left_index");
		i.putExtra("right_finger_assignment", "right_index");
		i.putExtra("easy_skip", "true");
		*/
        //

        i.putExtra("prompt_0", "poop");
        i.putExtra("left_finger_assignment_0", "left_index");
        i.putExtra("right_finger_assignment_0", "right_index");
        i.putExtra("easy_skip_0", "true");

        i.putExtra("left_finger_assignment_1", "left_thumb");
        i.putExtra("right_finger_assignment_1", "right_thumb");
        i.putExtra("easy_skip_1", "true");
		
		/*
		String left_thumb = "464D520020323000000000BA00000120016800C500C501000000001A408E00D6670040AE00DC6000409300676000403C008E220040B700695700408F0052B100402400DC12004098003B600040E10113550040B300C84400405600D61700403B00AB1B00409001090600403B0086810040AD00564F00403400607700806A012D060080F001044B00807400DF0D00807C0068060080A50069AC0080D300DE490080CA00F55600803700F412008052011E0F0080E200589B000000";
		String right_thumb = "464D520020323000000000C600000120016800C500C501000000001C408500BB0200407400DE0F00404A00D3190040A5006EAE00408701140800403800F3120040AF005756004052011E0F0040E101145400404501400400408D00D06900405600D61700407000696F0040940067600040C900F55600403F006F1700402100E51400806C0127060080F001044B00809F009D5F0080AE00DC6000807C0067040080D200DE4800804C00686E00808E0052B0008039006275008099003B600080E300589A000000";
		i.putExtra("left_thumb", left_thumb);
		i.putExtra("right_thumb", right_thumb);
		*/
		
		/*
		Intent i = new Intent("com.openandid.core.IDENTIFY");
		
		String left_thumb = "464D520020323000000000BA00000120016800C500C501000000001A408E00D6670040AE00DC6000409300676000403C008E220040B700695700408F0052B100402400DC12004098003B600040E10113550040B300C84400405600D61700403B00AB1B00409001090600403B0086810040AD00564F00403400607700806A012D060080F001044B00807400DF0D00807C0068060080A50069AC0080D300DE490080CA00F55600803700F412008052011E0F0080E200589B000000";
		String right_thumb = "464D520020323000000000C600000120016800C500C501000000001C408500BB0200407400DE0F00404A00D3190040A5006EAE00408701140800403800F3120040AF005756004052011E0F0040E101145400404501400400408D00D06900405600D61700407000696F0040940067600040C900F55600403F006F1700402100E51400806C0127060080F001044B00809F009D5F0080AE00DC6000807C0067040080D200DE4800804C00686E00808E0052B0008039006275008099003B600080E300589A000000";
		i.putExtra("left_thumb", left_thumb);
		i.putExtra("right_thumb", right_thumb);
		*/
		/*
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.openandidlogo);
		i.putExtra("image", bmp);
		i.putExtra("prompt", "This is a\nTest Prompt!");
		i.putExtra("easy_skip", "true");
		i.putExtra("left_finger_assignment", "left_index");
		i.putExtra("right_finger_assignment", "right_middle");
		*/
        startActivityForResult(i, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String txt = "";
        Bundle b = data.getExtras();
        if (resultCode != RESULT_CANCELED) {
            try {
                Set<String> keys = b.keySet();
                for (String k : keys) {
                    txt += k + " : " + b.get(k) + "\n";
                }
                Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
                // TODO Auto-generated method stub
            } catch (Exception e) {
                Log.i(TAG, "No result returned");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
