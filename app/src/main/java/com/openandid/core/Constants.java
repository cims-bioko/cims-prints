package com.openandid.core;

import java.util.Arrays;
import java.util.List;

public final class Constants {

    public static final String KILL_ACTION = "com.openandid.core.KILL";
    public static final String SCAN_ACTION = "com.openandid.core.SCAN";

    public static final String ENROLL_ACTION = "com.openandid.core.ENROLL";
    public static final String PIPE_ACTION = "com.openandid.core.PIPE";
    public static final String IDENTIFY_ACTION = "com.openandid.core.IDENTIFY";

    public static final String INTERNAL_SCAN_ACTION = "com.openandid.internal.SCAN";
    public static final String INTERNAL_IDENTIFY_ACTION = "com.openandid.internal.IDENTIFY";
    public static final String INTERNAL_ENROLL_ACTION = "com.openandid.internal.ENROLL";

    public static final String ENROLL = "ENROLL";
    public static final String IDENTIFY = "IDENTIFY";

    public static final String ODK_INTENT_BUNDLE_KEY = "odk_intent_bundle";

    public static final String SESSION_ID_KEY = "sessionID";
    public static final String PROMPT_KEY = "prompt";
    public static final String LEFT_FINGER_ASSIGNMENT_KEY = "left_finger_assignment";
    public static final String RIGHT_FINGER_ASSIGNMENT_KEY = "right_finger_assignment";
    public static final String EASY_SKIP_KEY = "easy_skip";

    public static final List<String> SCAN_FIELDS = Arrays.asList(
            PROMPT_KEY,
            LEFT_FINGER_ASSIGNMENT_KEY,
            RIGHT_FINGER_ASSIGNMENT_KEY,
            EASY_SKIP_KEY
    );

    public static final String DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String SCANNER_ATTACHED = "com.openandid.core.SCANNER_ATTACHED";

}
