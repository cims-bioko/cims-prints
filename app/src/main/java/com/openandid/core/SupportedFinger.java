package com.openandid.core;

import bmtafis.simple.Finger;

public enum SupportedFinger {

    left_thumb,
    right_thumb,
    left_index,
    right_index;

    public Finger getAFISFinger() {
        return Finger.valueOf(name().toUpperCase());
    }
}
