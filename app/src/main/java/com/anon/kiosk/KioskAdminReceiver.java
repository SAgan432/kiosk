package com.anon.kiosk;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class KioskAdminReceiver extends DeviceAdminReceiver {
    @Override public void onEnabled(Context c, Intent i) {}
    @Override public void onDisabled(Context c, Intent i) {
        // user somehow got to settings and deactivated admin — kill ourselves
        c.stopService(new Intent(c, BlockerService.class));
    }
    @Override public CharSequence onDisableRequested(Context c, Intent i) {
        return "System service cannot be disabled.";
    }
}
