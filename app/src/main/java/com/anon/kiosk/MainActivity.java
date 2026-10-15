package com.anon.kiosk;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        // 1. Overlay permission
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "1/3 Grant overlay", Toast.LENGTH_LONG).show();
            startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())));
            return;
        }

        // 2. Device admin (blocks uninstall)
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, KioskAdminReceiver.class);
        if (dpm != null && !dpm.isAdminActive(admin)) {
            Toast.makeText(this, "2/3 Activate admin", Toast.LENGTH_LONG).show();
            Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
            i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required for system service");
            startActivity(i);
            return;
        }

        // 3. Accessibility
        Toast.makeText(this, "3/3 Enable accessibility, then press home", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));

        // Start overlay service
        Intent svc = new Intent(this, BlockerService.class);
        svc.setAction("START");
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc);
        else startService(svc);

        finish();
    }
}
