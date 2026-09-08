package com.locintel.mpesabridge;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText url, token;
    private TextView status;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences(BridgeClient.PREFS, MODE_PRIVATE);
        int pad = (int)(18 * getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);

        TextView h = new TextView(this); h.setText("LOCINTEL M-Pesa Bridge"); h.setTextSize(24); h.setTypeface(null, Typeface.BOLD); root.addView(h);
        TextView sub = new TextView(this); sub.setText("Capture les notifications M-Pesa utiles et les transmet à Odoo sans lire toute la boîte SMS."); sub.setPadding(0,8,0,20); root.addView(sub);

        url = new EditText(this); url.setHint("https://votre-odoo.com"); url.setText(prefs.getString(BridgeClient.KEY_URL, "")); root.addView(url, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        token = new EditText(this); token.setHint("Jeton passerelle Odoo"); token.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); token.setText(prefs.getString(BridgeClient.KEY_TOKEN, "")); root.addView(token, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button save = new Button(this); save.setText("Enregistrer la configuration"); save.setOnClickListener(v -> save()); root.addView(save);
        Button access = new Button(this); access.setText("Autoriser l'accès aux notifications"); access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))); root.addView(access);
        Button test = new Button(this); test.setText("Tester la connexion avec un exemple M-Pesa"); test.setOnClickListener(v -> test()); root.addView(test);

        status = new TextView(this); status.setPadding(0,18,0,0); root.addView(status);
        setContentView(scroll);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void save() {
        String u = url.getText().toString().trim();
        if (!u.startsWith("https://")) { Toast.makeText(this, "Utilisez obligatoirement une URL HTTPS.", Toast.LENGTH_LONG).show(); return; }
        prefs.edit().putString(BridgeClient.KEY_URL, u).putString(BridgeClient.KEY_TOKEN, token.getText().toString().trim()).apply();
        Toast.makeText(this, "Configuration enregistrée.", Toast.LENGTH_SHORT).show(); refresh();
    }

    private void test() {
        save();
        String sample = "Buy Goods performed successfully to 9361 - MUVA ONLINE SHOPPING sent from 243816220471 - DIVINE MIZOLO MUDINGUNGU on 2026-09-05\nAmount:5.82 USD\nReason: 0816220471\nRef: TESTBRIDGE140";
        new Thread(() -> {
            try { String r = BridgeClient.send(this, "M-Pesa test", sample, getPackageName()); runOnUiThread(() -> { status.setText(r); Toast.makeText(this, "Test envoyé.", Toast.LENGTH_SHORT).show(); }); }
            catch (Exception ex) { runOnUiThread(() -> status.setText("Erreur : " + ex.getMessage())); }
        }).start();
    }

    private void refresh() { status.setText("Dernier état :\n" + prefs.getString(BridgeClient.KEY_LAST, "Aucun envoi pour le moment.")); }
}
