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

    private void startBlocker() {
        Intent svc = new Intent(this, BlockerService.class);
        svc.setAction("START");
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc);
        else startService(svc);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1. Overlay
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "1/3: Allow overlay", Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open overlay settings", Toast.LENGTH_LONG).show();
            }
            return;
        }

        // 2. Device Admin
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, KioskAdminReceiver.class);
        if (dpm != null && !dpm.isAdminActive(admin)) {
            Toast.makeText(this, "2/3: Activate admin", Toast.LENGTH_LONG).show();
            try {
                Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required for system service");
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open admin dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }

        // 3. Accessibility — открываем настройки, но не блокируемся
        // (юзер сам включит, потом вернётся — overlay поднимется)

        // всё выдано — запускаем блокировку
        startBlocker();

        Toast.makeText(this, "Starting system service...", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // onResume сработает сразу после onCreate
    }
}
