package com.faceunlock.manager;

import android.app.*; import android.os.Bundle; import android.graphics.Typeface; import android.widget.*;

public class MainActivity extends Activity {
 private ProfileStore store; private LinearLayout list;
 @Override public void onCreate(Bundle b){super.onCreate(b);store=new ProfileStore(this);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(32,40,32,32);
 TextView t=new TextView(this);t.setText("Face Unlock");t.setTextSize(28);t.setTypeface(null,Typeface.BOLD);r.addView(t);
 TextView s=new TextView(this);s.setText("\n● Face Unlock: מוכן להגדרה\n● PIN: נשאר פעיל תמיד\n● Fail-Safe: פעיל\n● זיהוי מזויף: כבוי");s.setTextSize(17);r.addView(s);
 Button a=new Button(this);a.setText("הוסף פרופיל פנים");a.setOnClickListener(v->addProfile());r.addView(a);Button d=new Button(this);d.setText("מחק את רשימת הפרופילים");d.setOnClickListener(v->{store.clear();refresh();});r.addView(d);
 TextView h=new TextView(this);h.setText("\nפרופילים");h.setTextSize(20);r.addView(h);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);r.addView(list);setContentView(r);refresh();}
 private void addProfile(){EditText e=new EditText(this);e.setHint("שם הפנים, למשל: ישראל");new AlertDialog.Builder(this).setTitle("פרופיל חדש").setView(e).setPositiveButton("שמור",(d,w)->{String n=e.getText().toString().trim();if(!n.isEmpty()){store.add(n);refresh();}}).setNegativeButton("ביטול",null).show();}
 private void refresh(){list.removeAllViews();java.util.List<String> p=store.load();if(p.isEmpty()){TextView t=new TextView(this);t.setText("אין עדיין פרופילים.\nההרשמה האמיתית תתחבר ל-provider/HAL בשלב הבא.");t.setTextSize(16);list.addView(t);}else for(String n:p){TextView t=new TextView(this);t.setText("• "+n);t.setTextSize(18);t.setPadding(0,10,0,10);list.addView(t);}}
}
