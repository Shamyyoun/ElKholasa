package utils;
// Change the package name to match your package

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ArrayHelper {
    Context context;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public ArrayHelper(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
    }

    /**
     * Converts the provided ArrayList<String>
     * into a JSONArray and saves it as a single
     * string in the apps shared preferences
     *
     * @param key   Preference key for SharedPreferences
     * @param array ArrayList<String> containing the list items
     */
    public void saveArray(String key, ArrayList<String> array) {
        JSONArray jArray = new JSONArray(array);
        editor.remove(key);
        editor.putString(key, jArray.toString());
        editor.commit();
    }

    public void saveIntegerArray(String key, ArrayList<Integer> array) {
        JSONArray jArray = new JSONArray(array);
        editor.remove(key);
        editor.putString(key, jArray.toString());
        editor.commit();
    }

    /**
     * Loads a JSONArray from shared preferences
     * and converts it to an ArrayList<String>
     *
     * @param key Preference key for SharedPreferences
     * @return ArrayList<String> containing the saved values from the JSONArray
     */
    public ArrayList<String> getArray(String key) throws JSONException {
        ArrayList<String> array = new ArrayList<String>();
        String jArrayString = prefs.getString(key, "NOPREFSAVED");


        JSONArray jArray = new JSONArray(jArrayString);
        for (int i = 0; i < jArray.length(); i++) {
            array.add(jArray.getString(i));
        }
        return array;

    }

    public ArrayList<Integer> getIntegerArray(String key) throws JSONException {
        ArrayList<Integer> array = new ArrayList<Integer>();
        String jArrayString = prefs.getString(key, "NOPREFSAVED");


        JSONArray jArray = new JSONArray(jArrayString);
        for (int i = 0; i < jArray.length(); i++) {
            array.add(jArray.getInt(i));
        }
        return array;

    }

    /*
     * used to save boolean value
     */
    public void saveBooleanValue(String key, boolean value) {
        editor.remove(key);
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key) {
        boolean value = prefs.getBoolean(key, false);
        return value;
    }
}
