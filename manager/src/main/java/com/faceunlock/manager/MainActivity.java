package com.faceunlock.manager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.hardware.biometrics.BiometricManager;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 50, 40, 40);
        TextView title = new TextView(this);
        title.setText("Face Unlock\n\nשלב 1 — בדיקת המערכת");
        title.setTextSize(24);
        root.addView(title);
        TextView status = new TextView(this);
        status.setText("\nPIN נשאר זמין תמיד.\n\nFail-Safe: פעיל\nFace provider: נבדק דרך Android\n\nעדיין לא מבוצע שום Unlock מזויף או עקיפה של Keyguard.");
        status.setTextSize(18);
        root.addView(status);
        setContentView(root);
    }
}
