package com.polyap.po_equalizer;

public class Settings {
    public static boolean        isEqualizerEnabled  = true;
    public static boolean        isEqualizerReloaded = true;
    public static int[]          seekbarpos          = new int[5];
    public static int            presetPos;
    public static short          reverbPreset        = 0;
    public static short          bassStrength        = 0;
    public static EqualizerModel equalizerModel;
    public static double         ratio               = 1.0;
    public static boolean        isEditing           = false;
}
