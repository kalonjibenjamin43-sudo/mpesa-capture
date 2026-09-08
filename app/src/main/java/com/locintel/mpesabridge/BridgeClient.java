package com.locintel.mpesabridge;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public final class BridgeClient {
    public static final String PREFS = "locintel_bridge";
    public static final String KEY_URL = "server_url";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_LAST = "last_status";

    private BridgeClient() {}

    public static String endpoint(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String base = p.getString(KEY_URL, "").trim();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/device_financing/mpesa/inbox";
    }

    public static boolean configured(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return p.getString(KEY_URL, "").startsWith("https://") && !p.getString(KEY_TOKEN, "").trim().isEmpty();
    }

    public static String send(Context ctx, String sender, String message, String packageName) throws Exception {
        if (!configured(ctx)) throw new IllegalStateException("Configurez d'abord l'URL HTTPS Odoo et le jeton.");
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        URL url = new URL(endpoint(ctx));
        HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(20000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        c.setRequestProperty("X-LOCINTEL-TOKEN", p.getString(KEY_TOKEN, ""));

        JSONObject json = new JSONObject();
        json.put("source", "android_notification");
        json.put("sender", sender == null ? packageName : sender);
        json.put("message", message);
        json.put("package", packageName);
        byte[] bytes = json.toString().getBytes("UTF-8");
        try (OutputStream os = c.getOutputStream()) { os.write(bytes); }

        int code = c.getResponseCode();
        BufferedReader br = new BufferedReader(new InputStreamReader(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) out.append(line);
        br.close();
        String status = "HTTP " + code + " — " + out;
        p.edit().putString(KEY_LAST, status).apply();
        if (code < 200 || code >= 300) throw new RuntimeException(status);
        return status;
    }
}
