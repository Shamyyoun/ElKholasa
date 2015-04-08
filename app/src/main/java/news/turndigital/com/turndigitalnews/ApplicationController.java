package news.turndigital.com.turndigitalnews;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import utils.ArrayHelper;
import utils.Constants;
import utils.EnumLanguages;

/**
 * Created by Ahmed on 27-Jun-14.
 */
public class ApplicationController extends Application {
    public static final int SEARCH_LIMIT = 20;
    public static final int ARTICLES_IN_TAGS_LIMIT = 20;

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
        ECOMMERCE_TRACKER
    }
    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    private static ApplicationController sInstance;

    /**
     * @return ApplicationController singleton instance
     */
    public static ApplicationController getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        CalligraphyConfig.initDefault("fonts/andlso.ttf", R.attr.fontPath);
        sInstance = this;

    }

    // creates tracker if not created and returns it
    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
                    .newTracker(R.xml.app_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics
                    .newTracker(R.xml.global_tracker) : analytics
                    .newTracker(R.xml.ecommerce_tracker);
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }


    public boolean IsNetworkAvaliable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnected();
        return isConnected;
    }

    public void updateLanguage(Context context, EnumLanguages language) {

        String idioma = "ar_EG";
        switch (language) {
            case ARABIC:
                idioma = "ar_EG";
                break;
            case ENGLISH:
                idioma = "en_US";
                break;
            default:
                break;
        }

        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        StoreLanguagePreference(context, language);

        context.getResources().updateConfiguration(config, null);
    }

    public EnumLanguages getLanguage(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_language), Context.MODE_PRIVATE);
        int choice = sharedPref.getInt(getString(R.string.lang),
                Integer.MAX_VALUE);
        switch (choice) {
            case 0:
                return EnumLanguages.ARABIC;
            case 1:
                return EnumLanguages.ENGLISH;
            case Integer.MAX_VALUE:
                return null;
            default:
                break;
        }
        return null;

    }

    private void StoreLanguagePreference(Context context, EnumLanguages language) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_language), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        switch (language) {
            case ARABIC:
                editor.putInt(getString(R.string.lang), 0);
                break;
            case ENGLISH:
                editor.putInt(getString(R.string.lang), 1);
                break;
            default:
                break;
        }
        editor.commit();
    }


    public static void saveCategoryList(ArrayList<Integer> categoryList, Context context) {
        ArrayHelper arrayHelper = new ArrayHelper(context);
        arrayHelper.saveIntegerArray(Constants.SHARED_PREF_CATEGORY_KEY, categoryList);
    }

    public static void saveProvidersList(ArrayList<Integer> providersList, Context context) {
        ArrayHelper arrayHelper = new ArrayHelper(context);
        arrayHelper.saveIntegerArray(Constants.SHARED_PREF_PROVIDERS_KEY, providersList);
    }

    public static void saveAllProvidersSelected(boolean selected, Context context) {
        ArrayHelper arrayHelper = new ArrayHelper(context);
        arrayHelper.saveBooleanValue(Constants.SHARED_PREF_ALL_SELECTED_KEY, selected);
    }

    public static void saveHideTutorial(boolean show, Context context) {
        ArrayHelper arrayHelper = new ArrayHelper(context);
        arrayHelper.saveBooleanValue(Constants.SHARED_PREF_SHOW_TUTORIAL_KEY, show);
    }


    public static ArrayList<Integer> getCategoryList(Context context) {

        ArrayHelper arrayHelper = new ArrayHelper(context);
        ArrayList<Integer> integerList = null;
        try {
            integerList = arrayHelper.getIntegerArray(Constants.SHARED_PREF_CATEGORY_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return integerList;
    }

    public static ArrayList<Integer> getProvidersList(Context context) {

        ArrayHelper arrayHelper = new ArrayHelper(context);
        ArrayList<Integer> integerList = null;
        try {
            integerList = arrayHelper.getIntegerArray(Constants.SHARED_PREF_PROVIDERS_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return integerList;
    }

    public static boolean isAllProvidersSelected(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        boolean value = prefs.getBoolean(Constants.SHARED_PREF_ALL_SELECTED_KEY, true);
        editor.commit();

        return value;
    }

    public static boolean getHideTutorial(Context context) {
        ArrayHelper arrayHelper = new ArrayHelper(context);
        return  arrayHelper.getBoolean(Constants.SHARED_PREF_SHOW_TUTORIAL_KEY);
    }

}
