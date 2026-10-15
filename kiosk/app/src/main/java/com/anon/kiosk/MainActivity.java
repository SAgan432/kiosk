package com.anon.kiosk;

import android.app.Activity;
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

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Grant overlay permission, then reopen", Toast.LENGTH_LONG).show();
            startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())));
            return;
        }

        Intent svc = new Intent(this, BlockerService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }

        Toast.makeText(this, "Enable accessibility service in settings", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));

        finish();
    }
}