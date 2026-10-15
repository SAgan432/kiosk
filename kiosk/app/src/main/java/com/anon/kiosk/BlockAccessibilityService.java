package com.anon.kiosk;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class BlockAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {}

    @Override
    public void onInterrupt() {}

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        int c = e.getKeyCode();
        if (c == KeyEvent.KEYCODE_BACK
            || c == KeyEvent.KEYCODE_HOME
            || c == KeyEvent.KEYCODE_APP_SWITCH
            || c == KeyEvent.KEYCODE_MENU
            || c == KeyEvent.KEYCODE_VOLUME_UP
            || c == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true; // глотаем
        }
        return super.onKeyEvent(e);
    }
}