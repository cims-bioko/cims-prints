package logic;

import com.openandid.core.Controller;
import com.openandid.core.R;

/**
 * An enumeration for each finger.
 *
 * There are currently far too many enums/constants for this and they should be unified so that the purpose is not so
 * muddled. There are at most 10 values. This is insanity.
 *
 * @see bmtafis.simple.Finger
 * @see sourceafis.simple.Finger
 * @see com.openandid.core.SupportedFinger
 */
public enum FingerType {

    left_thumb(R.string.left_thumb, R.drawable.l_thumb),
    right_thumb(R.string.right_thumb, R.drawable.l_thumb),
    left_index(R.string.left_index, R.drawable.l_index),
    right_index(R.string.right_index, R.drawable.r_index),
    left_middle(R.string.left_middle, R.drawable.l_middle),
    right_middle(R.string.right_middle, R.drawable.r_middle),
    left_ring(R.string.left_ring, R.drawable.l_middle),
    right_ring(R.string.right_ring, R.drawable.r_middle),
    left_pinky(R.string.left_pinky, R.drawable.l_pinky),
    right_pinky(R.string.right_pinky, R.drawable.r_pinky);

    private int label;
    private int drawable;

    public String getLabel() {
        return Controller.getAppContext().getResources().getString(label);
    }

    public String getKey() {
        return name();
    }

    public int getDrawable() {
        return drawable;
    }

    FingerType(int label, int drawable) {
        this.label = label;
        this.drawable = drawable;
    }
}
