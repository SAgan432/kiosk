package com.anon.kiosk;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class BlockAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e == null) return;
        int type = e.getEventType();
        CharSequence pkg = e.getPackageName();

        // если пришло что-то не наше — вернуть оверлей
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            || type == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            if (pkg != null && !"com.anon.kiosk".contentEquals(pkg)) {
                Intent i = new Intent(this, BlockerService.class);
                i.setAction("REASSERT");
                try { startService(i); } catch (Exception ignored) {}
            }
        }

        // если открылась панель уведомлений — жмём back, чтобы её закрыть
        if ("com.android.systemui".contentEquals(pkg)) {
            performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }

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
            return true; // swallow
        }
        return super.onKeyEvent(e);
    }
}
