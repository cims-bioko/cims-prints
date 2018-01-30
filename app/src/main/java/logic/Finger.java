package logic;

import com.github.cimsbioko.cimsprints.R;
import com.openandid.core.Controller;


/**
 * An enumeration for each finger. Not all fingers are supported currently for enrollment/identification.
 *
 * @see bmtafis.simple.Finger
 */
public enum Finger {

    left_thumb(R.string.left_thumb, R.drawable.l_thumb, bmtafis.simple.Finger.LEFT_THUMB),
    right_thumb(R.string.right_thumb, R.drawable.r_thumb, bmtafis.simple.Finger.RIGHT_THUMB),
    left_index(R.string.left_index, R.drawable.l_index, bmtafis.simple.Finger.LEFT_INDEX),
    right_index(R.string.right_index, R.drawable.r_index, bmtafis.simple.Finger.RIGHT_INDEX),
    left_middle(R.string.left_middle, R.drawable.l_middle, bmtafis.simple.Finger.LEFT_MIDDLE),
    right_middle(R.string.right_middle, R.drawable.r_middle, bmtafis.simple.Finger.RIGHT_MIDDLE),
    left_ring(R.string.left_ring, R.drawable.l_middle, bmtafis.simple.Finger.LEFT_RING),
    right_ring(R.string.right_ring, R.drawable.r_middle, bmtafis.simple.Finger.LEFT_RING),
    left_pinky(R.string.left_pinky, R.drawable.l_pinky, bmtafis.simple.Finger.LEFT_LITTLE),
    right_pinky(R.string.right_pinky, R.drawable.r_pinky, bmtafis.simple.Finger.RIGHT_LITTLE);

    private static final Finger[] ENROLLED = {left_thumb, right_thumb, left_index, right_index};

    private int label;
    private int drawable;
    private bmtafis.simple.Finger afisValue;

    public String getLabel() {
        return Controller.getAppContext().getResources().getString(label);
    }

    public String getKey() {
        return name();
    }

    public int getDrawable() {
        return drawable;
    }

    public bmtafis.simple.Finger afisValue() {
        return afisValue;
    }

    Finger(int label, int drawable, bmtafis.simple.Finger afisValue) {
        this.label = label;
        this.drawable = drawable;
        this.afisValue = afisValue;
    }

    public static Finger[] enrolledValues() {
        return ENROLLED;
    }
}
