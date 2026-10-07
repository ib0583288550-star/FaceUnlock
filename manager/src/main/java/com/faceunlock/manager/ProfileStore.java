package com.faceunlock.manager;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class ProfileStore {
 private final Context c;
 public ProfileStore(Context c){this.c=c.getApplicationContext();}
 public List<String> load(){List<String> out=new ArrayList<>(); try{JSONArray a=new JSONArray(c.getSharedPreferences("face_unlock",0).getString("profiles","[]")); for(int i=0;i<a.length();i++)out.add(a.getJSONObject(i).optString("name","Face "+(i+1)));}catch(Exception ignored){} return out;}
 public void add(String name){try{JSONArray a=new JSONArray(c.getSharedPreferences("face_unlock",0).getString("profiles","[]")); JSONObject o=new JSONObject();o.put("name",name);a.put(o);c.getSharedPreferences("face_unlock",0).edit().putString("profiles",a.toString()).apply();}catch(Exception ignored){}}
 public void clear(){c.getSharedPreferences("face_unlock",0).edit().putString("profiles","[]").apply();}
}
