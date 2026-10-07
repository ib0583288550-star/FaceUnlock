package com.faceunlock.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.biometrics.BiometricManager;
import android.os.Bundle;
import android.graphics.Typeface;
import android.widget.*;

public class MainActivity extends Activity {
 private ProfileStore store; private LinearLayout list; private TextView state; private TextView hardware;

 @Override public void onCreate(Bundle b){
  super.onCreate(b); store=new ProfileStore(this);
  LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.VERTICAL); r.setPadding(32,40,32,32);
  TextView t=new TextView(this); t.setText("Face Unlock"); t.setTextSize(28); t.setTypeface(null,Typeface.BOLD); r.addView(t);
  state=new TextView(this); state.setTextSize(17); r.addView(state);
  hardware=new TextView(this); hardware.setTextSize(16); r.addView(hardware);

  Button on=new Button(this); on.setText("הפעל Face Unlock"); on.setOnClickListener(v->{getPreferences(0).edit().putBoolean("enabled",true).apply(); refreshState();}); r.addView(on);
  Button off=new Button(this); off.setText("כבה Face Unlock"); off.setOnClickListener(v->{getPreferences(0).edit().putBoolean("enabled",false).apply(); refreshState();}); r.addView(off);
  Button check=new Button(this); check.setText("בדוק ביומטריה"); check.setOnClickListener(v->refreshHardware()); r.addView(check);
  Button a=new Button(this); a.setText("הוסף פרופיל פנים"); a.setOnClickListener(v->addProfile()); r.addView(a);
  Button d=new Button(this); d.setText("מחק את רשימת הפרופילים"); d.setOnClickListener(v->{store.clear();refresh();}); r.addView(d);

  TextView h=new TextView(this); h.setText("\nפרופילים"); h.setTextSize(20); r.addView(h);
  list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); r.addView(list);
  setContentView(r); refresh(); refreshState(); refreshHardware();
 }

 private void refreshState(){
  boolean on=getPreferences(0).getBoolean("enabled",false);
  state.setText("\n● Face Unlock: "+(on?"מופעל":"כבוי")+
   "\n● PIN: נשאר פעיל תמיד"+
   "\n● Fail-Safe: פעיל"+
   "\n● זיהוי מזויף: כבוי"+
   "\n● Provider/HAL: עדיין בבדיקה");
 }

 private void refreshHardware(){
  BiometricManager bm=getSystemService(BiometricManager.class);
  if(bm==null){hardware.setText("● ביומטריה: שירות לא זמין");return;}
  int r=bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
  String s;
  switch(r){
   case BiometricManager.BIOMETRIC_SUCCESS: s="זמינה ביומטריה חזקה"; break;
   case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE: s="לא נמצאה חומרת ביומטריה חזקה"; break;
   case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE: s="חומרת ביומטריה כרגע לא זמינה"; break;
   case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED: s="יש חומרה, אבל אין הרשמה ביומטרית"; break;
   default: s="מצב ביומטריה: "+r;
  }
  hardware.setText("● בדיקת Android: "+s);
 }

 private void addProfile(){
  EditText e=new EditText(this); e.setHint("שם הפרופיל, למשל: ישראל");
  new AlertDialog.Builder(this).setTitle("פרופיל חדש").setView(e)
   .setPositiveButton("שמור",(d,w)->{String n=e.getText().toString().trim();if(!n.isEmpty()){store.add(n);refresh();}})
   .setNegativeButton("ביטול",null).show();
 }

 private void refresh(){
  list.removeAllViews(); java.util.List<String> p=store.load();
  if(p.isEmpty()){TextView t=new TextView(this);t.setText("אין עדיין פרופילים.");t.setTextSize(16);list.addView(t);}
  else for(String n:p){TextView t=new TextView(this);t.setText("• "+n);t.setTextSize(18);t.setPadding(0,10,0,10);list.addView(t);}
 }
}
