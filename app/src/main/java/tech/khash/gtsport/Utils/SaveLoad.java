package tech.khash.gtsport.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import tech.khash.gtsport.Model.Score;

public class SaveLoad {

    public static final String PREF_KEY_ARRAY = "pref_key_array";

    public static void addScoreToDb(Context context, Score score) {
        //load the previous data, and add the new list to it
        ArrayList<Score> currentList = loadScoresDb(context);

        //if there is nothing in there, this will be null, so we instantiate it
        if (currentList == null) {
            currentList = new ArrayList<>();
        }//if

        //add the object to the top of the list (most recent one at the top)
        currentList.add(0, score);

        //save the updated list
        saveScoresDb(context, currentList);
    }//addToList


    public static void saveScoresDb(Context context, ArrayList<Score> inputArrayList) {
        //get reference to shared pref
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //create Gson object
        Gson gson = new Gson();

        //check for null input
        if (inputArrayList == null) {
            inputArrayList = new ArrayList<>();
        }

        //convert arraylist
        String json = gson.toJson(inputArrayList);

        //get the shared preference editor
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //add the new updated list. If it already exists, it just replaces the old one so we wont need to delete first
        editor.putString(PREF_KEY_ARRAY, json);
        editor.apply();
    }//saveScoresDb



    public static ArrayList<Score> loadScoresDb(Context context) {
        //create Gson object
        Gson gson = new Gson();
        //get reference to the shared pref
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        /*
        get the string from the preference (this will be empty string if there is no data in there
        yet). as a result the output array list will be null, so we need to check for this in the
        save array list when we pull the old data
         */
        String response = sharedPreferences.getString(PREF_KEY_ARRAY, "");
        //convert the json string back to Loc Array list and return it
        ArrayList<Score> outputArrayList = gson.fromJson(response,
                new TypeToken<List<Score>>() {
                }.getType());

        return outputArrayList;
    }//loadScoresDb

    public static void deleteDb(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_KEY_ARRAY).apply();
    }//removeAllFences

    private static int getScoreIndex (Context context, Score score) {
        ArrayList<Score> scores = loadScoresDb(context);

        int index = -1;
        int i = 0;
        for (Score s : scores) {
            if (s.equalsTo(score)) {
                index = i;
                return index;
            }//if
            i++;
        }//for
        return index;
    }//getLocIndex

    public static void replaceScoreInDb(Context context, Score newScore) {
        //find the old newScore and get its index
        int index  = getScoreIndex(context, newScore);
        if (index == -1) {
            //no such newScore exists
            return;
        }//if
        //load the database
        ArrayList<Score> scores = loadScoresDb(context);

        //replace the old Loc object with the new one in the ArrayList
        scores.set(index, newScore);

        //save the new list
        saveScoresDb(context, scores);
    }//replaceScoreInDb

}//SaveLoad
