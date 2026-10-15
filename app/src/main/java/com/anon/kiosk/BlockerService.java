package com.anon.kiosk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class BlockerService extends Service {
    private WindowManager wm;
    private View overlay;
    private final long[] taps = new long[5];
    private int tapCount = 0;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // минимально видимая нотификация (обязательна для foreground service)
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                "k", "System", NotificationManager.IMPORTANCE_MIN);
            ch.setShowBadge(false);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);

            Notification n = new Notification.Builder(this, "k")
                .setContentTitle("System")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setOngoing(true)
                .build();
            startForeground(1, n);
        }

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.BLACK);
        overlay.setFocusableInTouchMode(true);
        overlay.requestFocus();

        // ловим тапы — 5 быстрых = выход
        overlay.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                long now = System.currentTimeMillis();
                taps[tapCount % 5] = now;
                tapCount++;
                if (tapCount >= 5) {
                    long oldest = taps[(tapCount - 5) % 5];
                    if (now - oldest < 3000) {
                        stopSelf();
                        return false;
                    }
                }
            }
            return true; // в остальных случаях глотаем всё
        });

        int type = (Build.VERSION.SDK_INT >= 26)
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.OPAQUE);

        wm.addView(overlay, lp);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (overlay != null && wm != null) {
            try { wm.removeView(overlay); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }
}