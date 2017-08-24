package logic;

import android.content.res.Resources;
import android.util.Log;

import com.openandid.core.Controller;
import com.openandid.core.R;

public class FingerType {

    public final String TAG = "FingerType";

    private String label;
    private String key;
    private int drawableId;

    public String getLabel() {
        return label;
    }

    public String getKey() {
        return key;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public FingerType(String type) throws IllegalArgumentException {
        Resources res = Controller.getAppContext().getResources();
        ReadableFinger f;
        try {
            f = ReadableFinger.valueOf(type);
        } catch (Exception e) {
            Log.i(TAG, "Couldn't parse string for finger: " + type);
            throw new IllegalArgumentException();
        }
        key = f.name();
        switch (f) {
            case left_thumb:
                label = String.format("%s", res.getString((R.string.left_thumb)));
                drawableId = R.drawable.l_thumb;
                break;
            case right_thumb:
                label = String.format("%s", res.getString(R.string.right_thumb));
                drawableId = R.drawable.r_thumb;
                break;
            case left_index:
                label = String.format("%s", res.getString(R.string.left_index));
                drawableId = R.drawable.l_index;
                break;
            case right_index:
                label = String.format("%s", res.getString(R.string.right_index));
                drawableId = R.drawable.r_index;
                break;
            case left_middle:
                label = String.format("%s %s %s", res.getString(R.string.left), res.getString(R.string.middle), res.getString(R.string.finger));
                drawableId = R.drawable.l_middle;
                break;
            case right_middle:
                label = String.format("%s %s %s", res.getString(R.string.right), res.getString(R.string.middle), res.getString(R.string.finger));
                drawableId = R.drawable.r_middle;
                break;
            case left_ring:
                label = String.format("%s %s %s", res.getString(R.string.left), res.getString(R.string.ring), res.getString(R.string.finger));
                drawableId = R.drawable.l_ring;
                break;
            case right_ring:
                label = String.format("%s %s %s", res.getString(R.string.right), res.getString(R.string.ring), res.getString(R.string.finger));
                drawableId = R.drawable.r_ring;
                break;
            case left_pinky:
                label = String.format("%s %s %s", res.getString(R.string.left), res.getString(R.string.pinky), res.getString(R.string.finger));
                drawableId = R.drawable.l_pinky;
                break;
            case right_pinky:
                label = String.format("%s %s %s", res.getString(R.string.right), res.getString(R.string.pinky), res.getString(R.string.finger));
                drawableId = R.drawable.r_pinky;
                break;
        }
    }

    /**
     * Too many enums/constants for the same things. They all should be unified so that the purpose is not so muddled.
     *
     * @see bmtafis.simple.Finger
     * @see sourceafis.simple.Finger
     * @see com.openandid.core.SupportedFinger
     */
    private enum ReadableFinger {
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
