package com.anon.kiosk;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.*;
import android.widget.FrameLayout;

public class BlockerService extends Service {
    private WindowManager wm;
    private View overlay;
    private final long[] taps = new long[5];
    private int tapCount = 0;
    private boolean unlocked = false;
    private final Handler h = new Handler(Looper.getMainLooper());

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // foreground
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                "k", "System", NotificationManager.IMPORTANCE_MIN);
            ch.setShowBadge(false);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "k")
                .setContentTitle("System")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setOngoing(true)
                .build();
            startForeground(1, n);
        }

        if (overlay == null) showOverlay();
        else if ("REASSERT".equals(intent != null ? intent.getAction() : "")) {
            // already on top — just keep
        }

        // watchdog: если overlay слетел — вернуть
        h.removeCallbacksAndMessages(null);
        h.postDelayed(new Runnable() {
            @Override public void run() {
                if (!unlocked && (overlay == null || overlay.getParent() == null)) {
                    showOverlay();
                }
                h.postDelayed(this, 500);
            }
        }, 500);

        return START_STICKY;
    }

    private void showOverlay() {
        if (overlay != null) {
            try { wm.removeView(overlay); } catch (Exception ignored) {}
            overlay = null;
        }

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        overlay = new FrameLayout(this);
        overlay.setBackgroundColor(Color.BLACK);
        overlay.setFocusableInTouchMode(true);
        overlay.requestFocus();

        overlay.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                long now = System.currentTimeMillis();
                taps[tapCount % 5] = now;
                tapCount++;
                if (tapCount >= 5) {
                    long oldest = taps[(tapCount - 5) % 5];
                    if (now - oldest < 3000) {
                        unlocked = true;
                        h.removeCallbacksAndMessages(null);
                        stopSelf();
                        return false;
                    }
                }
            }
            return true;
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
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.OPAQUE);

        try { wm.addView(overlay, lp); } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        h.removeCallbacksAndMessages(null);
        if (overlay != null && wm != null) {
            try { wm.removeView(overlay); } catch (Exception ignored) {}
        }
        overlay = null;
        super.onDestroy();
    }
}
