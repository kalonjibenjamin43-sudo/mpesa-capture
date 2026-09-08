package com.locintel.mpesabridge;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class MpesaNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        if (n == null) return;
        Bundle e = n.extras;
        CharSequence titleCs = e.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence bigCs = e.getCharSequence(Notification.EXTRA_BIG_TEXT);
        CharSequence textCs = e.getCharSequence(Notification.EXTRA_TEXT);
        String title = titleCs == null ? "" : titleCs.toString();
        String body = bigCs != null ? bigCs.toString() : (textCs == null ? "" : textCs.toString());
        String combined = (title + "\n" + body).trim();
        String upper = combined.toUpperCase();

        // Filtre métier : évite d'envoyer toutes les notifications personnelles à Odoo.
        boolean looksMpesa = upper.contains("REASON:") && upper.contains("REF:") && upper.contains("AMOUNT:")
                && (upper.contains("BUY GOODS") || upper.contains("M-PESA") || upper.contains("MPESA"));
        if (!looksMpesa || !BridgeClient.configured(this)) return;

        new Thread(() -> {
            try {
                BridgeClient.send(this, title, combined, sbn.getPackageName());
            } catch (Exception ex) {
                getSharedPreferences(BridgeClient.PREFS, MODE_PRIVATE)
                        .edit().putString(BridgeClient.KEY_LAST, "ERREUR — " + ex.getMessage()).apply();
            }
        }).start();
    }
}
