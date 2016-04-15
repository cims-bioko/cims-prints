package logic;


import com.openandid.core.Controller;
import com.openandid.core.R;

import android.content.res.Resources;
import android.util.Log;

public class FingerType {

	public final String TAG = "FingerType";
	
	public String finger_name;
	public String finger_key;
	public int finger_code;
	public int finger_image_location;
    Resources res;

	public FingerType(String type) throws IllegalArgumentException{
		res = Controller.getAppContext().getResources();
        readable_finger f;
		try{
			f = readable_finger.valueOf(type);	
		}
		catch (Exception e) {
			Log.i(TAG,"Couldn't parse string for finger: " + type);
			throw new IllegalArgumentException();
		}
		finger_key = f.toString();
		switch(f){
		case left_thumb:
			finger_name = String.format("%s",res.getString((R.string.left_thumb)));
			finger_code = 4;
			finger_image_location = R.drawable.l_thumb;
			break;
		case right_thumb:
            finger_name = String.format("%s",res.getString(R.string.right_thumb));
			finger_code = 5;
			finger_image_location = R.drawable.r_thumb; 
			break;
		case left_index:
            finger_name = String.format("%s",res.getString(R.string.left_index));
			finger_code = 3;
			finger_image_location = R.drawable.l_index;
			break;
		case right_index:
            finger_name = String.format("%s",res.getString(R.string.right_index));
			finger_code = 6;
			finger_image_location = R.drawable.r_index;
			break;
		case left_middle:
            finger_name = String.format("%s %s %s",res.getString(R.string.left), res.getString(R.string.middle), res.getString(R.string.finger));
			finger_code = 2;
			finger_image_location = R.drawable.l_middle;
			break;
		case right_middle:
            finger_name = String.format("%s %s %s",res.getString(R.string.right), res.getString(R.string.middle), res.getString(R.string.finger));
			finger_code = 7;
			finger_image_location = R.drawable.r_middle;
			break;
		case left_ring:
            finger_name = String.format("%s %s %s",res.getString(R.string.left), res.getString(R.string.ring), res.getString(R.string.finger));
			finger_code = 1;
			finger_image_location = R.drawable.l_ring;
			break;
		case right_ring:
            finger_name = String.format("%s %s %s",res.getString(R.string.right), res.getString(R.string.ring), res.getString(R.string.finger));
			finger_code = 8;
			finger_image_location = R.drawable.r_ring;
			break;
		case left_pinky:
            finger_name = String.format("%s %s %s",res.getString(R.string.left), res.getString(R.string.pinky), res.getString(R.string.finger));
			finger_code = 0;
            finger_image_location = R.drawable.l_pinky;
			break;
		case right_pinky:
            finger_name = String.format("%s %s %s",res.getString(R.string.right), res.getString(R.string.pinky), res.getString(R.string.finger));
			finger_code = 9;
			finger_image_location = R.drawable.r_pinky;
			break;
		}
	}
	
	private enum readable_finger{
		left_thumb,
		right_thumb,
		left_index,
		right_index,
		left_middle,
		right_middle,
		left_ring,
		right_ring,
		left_pinky,
		right_pinky
	}
}
