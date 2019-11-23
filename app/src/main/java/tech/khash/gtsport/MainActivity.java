package tech.khash.gtsport;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.Utils.CreateCSV;
import tech.khash.gtsport.Utils.SaveLoad;

import static tech.khash.gtsport.Utils.SaveLoad.deleteDb;
import static tech.khash.gtsport.Utils.SaveLoad.loadScoresDb;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<Score> scores;
    private ImageView imageDr, imageSr;
    private TextView textDr, textSr, textDeltaDr, textDeltaSr;
    private Score currentScore, lastScore;
    private ProgressBar progressBar;
    private static final String PREF_KEY_FIRST_TIME = "pref-key-first-time";

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean fistTime = sharedPreferences.getBoolean(PREF_KEY_FIRST_TIME, true);
        if (fistTime) {
            //add data
            addData();
        }

        Log.d(TAG, "\u2192");

        scores = new ArrayList<>();
        scores = loadScoresDb(this);

        progressBar = findViewById(R.id.progress_bar);

        imageDr = findViewById(R.id.image_dr);
        imageSr = findViewById(R.id.image_sr);

        textDr = findViewById(R.id.text_dr);
        textSr = findViewById(R.id.text_sr);
        textDeltaDr = findViewById(R.id.text_dr_delta);
        textDeltaSr = findViewById(R.id.text_sr_delta);

        ImageButton button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });

        //get the data
        getData();
    }//onCreate

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }//onStop

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }//onResume

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }//onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                getData();
                return true;
            case R.id.action_delete:
                showDeleteAllDialog(this);
                return true;
            case R.id.action_list:
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_chart:
                Intent chartIntent = new Intent(MainActivity.this, MPChartActivity.class);
                startActivity(chartIntent);
                return true;
            case R.id.action_export:
//                String body = getCsv(scores);
//                String title = "GT Sport export - ";
//                long millis = Calendar.getInstance().getTimeInMillis();
//                SimpleDateFormat formatter = new SimpleDateFormat("MMM.dd.yyyy - HH:mm", Locale.getDefault());
//                String date = formatter.format(millis);
//                title += date;
//                ShareCompat.IntentBuilder.from(this)
//                        .setType("text/plain")
//                        .setChooserTitle("Export GT Sport data")
//                        .setSubject(title)
//                        .setText(body)
//                        .startChooser();
                CreateCSV.saveCsvFileStorage(this,scores, CreateCSV.SHARE_FILE);
                return true;
            case R.id.action_create_file:
                CreateCSV.saveCsvFileStorage(this,scores, CreateCSV.SAVE_FILE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }//switch
    }//onOptionsItemSelected

    private void getData() {
        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(this);
        String url = "https://www.kudosprime.com/gts/stats.php?profile=7793649#dr";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //get rid of progress bar
                        progressBar.setVisibility(View.GONE);
                        //Convert to Document and get data
                        Document document = Jsoup.parse(response);
                        getScore(document);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: cod-" +
                        error.networkResponse.statusCode, Toast.LENGTH_SHORT).show();
            }
        });
        //set tag to cancel on stop
        stringRequest.setTag(TAG);
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
        //start task and show progressbar
        progressBar.setVisibility(View.VISIBLE);
    }//getData

    private void getScore(Document doc) {
        if (doc == null) {
            return;
        }
        //get the element containing the scores
        Elements scores = doc.getElementsByClass("gtcom_stats_main");

        if (scores != null && scores.size() > 0) {
            Element scoreElement = scores.get(0);
            String s = scoreElement.toString();

            //extract the scores from the html text
            int dr = getDr(s);
            int sr = getSr(s);

            //create a time and Score object
            long epoch = Calendar.getInstance().getTimeInMillis();
            Score score = new Score(dr, sr, epoch);
            currentScore = score;
            updateScore(score);
        }//if
    }//getScore

    private void updateScore(Score score) {
        if (scores == null || scores.size() < 1) {
            //this is the firs one
            lastScore = score;
            //update database
            SaveLoad.addScoreToDb(this, score);
            //update local list
            scores = new ArrayList<>();
            scores = loadScoresDb(this);
        } else {
            //get the last score
            lastScore = scores.get(0);
            //check to see if anything has changed
            if (!currentScore.equalsTo(lastScore)) {
                //calculate delta
                int drDelta = currentScore.getDr() - lastScore.getDr();
                int srDelta = currentScore.getSr() - lastScore.getSr();
                //add delta to Score
                currentScore.setDrDelta(drDelta);
                currentScore.setSrDelta(srDelta);
                //update database
                SaveLoad.addScoreToDb(this, score);
                //update local list
                scores = new ArrayList<>();
                scores = loadScoresDb(this);
            } else {
                if (scores.size() > 1) {
                    lastScore = scores.get(1);
                } else {
                    lastScore = scores.get(0);
                }
            }//if-else : equals last one
        }//if-else: empty database
        displayResults(currentScore, lastScore);
    }//updateScore

    private void displayResults(Score currentScore, Score lastScore) {
        if (currentScore.equalsTo(lastScore)) {
            //show results, no delta required
            textDr.setText(currentScore.getDrString());
            textSr.setText(currentScore.getSrString());
            textDeltaDr.setText("");
            textDeltaSr.setText("");
            imageDr.setVisibility(View.INVISIBLE);
            imageSr.setVisibility(View.INVISIBLE);
            return;
        }

        textDr.setText(currentScore.getDrString());
        animateViewFadeIn(this, textDr);
        textSr.setText(currentScore.getSrString());
        animateViewFadeIn(this, textSr);

        //calculate delta
        int drDelta = currentScore.getDr() - lastScore.getDr();
        int srDelta = currentScore.getSr() - lastScore.getSr();

        //show delta
        String drDeltaString = String.format(Locale.US, "%,d", drDelta);
        textDeltaDr.setText(drDeltaString);
        animateViewFadeIn(this, textDeltaDr);
        textDeltaSr.setText(String.valueOf(srDelta));
        animateViewFadeIn(this, textDeltaSr);

        //show arrows only if it is not 0
        if (drDelta == 0) {
            imageDr.setVisibility(View.INVISIBLE);
        } else {
            imageDr.setVisibility(View.VISIBLE);
            if (drDelta > 0) {
                imageDr.setImageResource(R.drawable.up);
            } else {
                imageDr.setImageResource(R.drawable.down);
            }
            animateViewFadeIn(this, imageDr);
        }//if-else = 0

        if (srDelta == 0) {
            imageSr.setVisibility(View.INVISIBLE);
        } else {
            imageDr.setVisibility(View.VISIBLE);
            if (srDelta > 0) {
                imageSr.setImageResource(R.drawable.up);
            } else {
                imageSr.setImageResource(R.drawable.down);
            }
            animateViewFadeIn(this, imageSr);
        }//if-else = 0
    }//displayResults

    public static void animateViewFadeIn(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fadein);
        view.startAnimation(animation);
    }//animateViewFadeIn

    public static void animateViewFadeOut(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fadeout);
        view.startAnimation(animation);
    }//animateViewFadeIn

    private int getDr(String s) {
        int sportIndex = s.indexOf("Sport Mode Performances");
        s = s.substring(sportIndex);

        int spanIndex = s.indexOf("<span>");
        s = s.substring(spanIndex);

        int parIndex = s.indexOf("(in");
        String dr = (s.substring(s.indexOf("<span>") + 6, parIndex)).trim();

        try {
            int result = NumberFormat.getNumberInstance(Locale.US).parse(dr).intValue();
            return result;
        } catch (ParseException e) {
            Log.e(TAG, "error - number conversion", e);
            return -1;
        }
    }//getDr

    private int getSr(String s) {
        int sportIndex = s.indexOf("Sport Mode Sportmanship (SR)");
        s = s.substring(sportIndex);

        int spanIndex = s.indexOf("<span>");
        s = s.substring(spanIndex);

        int parIndex = s.indexOf('/');
        String sr = (s.substring(s.indexOf("<span>") + 6, parIndex)).trim();

        return Integer.valueOf(sr);
    }

    //Helper method for showing the dialog for deleting all data
    private void showDeleteAllDialog(final Context context) {
        //create the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //add message and button functionality
        builder.setMessage("Are you sure you want to dlete all data?")
                .setPositiveButton("Delete all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Delete all
                        deleteDb(context);
                        recreate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }//showUnsavedChangesDialog

    private void addData() {
        ArrayList<Score> scores = new ArrayList<>();
        Score score;
        String body = "17118,31,99,0,9,8,1,-1,-1,-1,-1,1573880626798\n" +
                "17087,941,99,0,7,2,5,1,-1,-1,-1,1573879404700\n" +
                "16146,724,99,0,5,4,1,-1,-1,-1,-1,1573797770950\n" +
                "15422,763,99,0,6,4,2,1,-1,-1,-1,1573796595818\n" +
                "14659,-801,99,0,9,14,-5,-1,-1,-1,-1,1573795396735\n" +
                "15460,-1046,99,0,18,17,1,-1,-1,-1,1,1573704904956\n" +
                "16506,-144,99,0,14,11,3,1,-1,-1,-1,1573628611736\n" +
                "16650,637,99,6,3,2,1,1,-1,-1,-1,1573626237630\n" +
                "16013,-581,93,-6,6,11,-5,-1,1,3.0,-1,1573624977304\n" +
                "16594,723,99,0,6,5,1,-1,-1,-1,-1,1573526571633\n" +
                "15871,371,99,0,8,7,1,1,-1,-1,-1,1573525383362\n" +
                "15500,-666,99,0,8,13,-5,-1,-1,-1,-1,1573524188630\n" +
                "16166,-17,99,0,15,10,5,1,-1,-1,-1,1573522963744\n" +
                "16183,-634,99,0,12,13,-1,-1,-1,-1,-1,1573520620585\n" +
                "16817,564,99,0,9,6,3,1,-1,-1,-1,1573519373467\n" +
                "16253,-430,99,0,8,11,-3,-1,-1,-1,0,1573518202853\n" +
                "16683,-902,99,0,10,15,-5,-1,-1,-1,-1,1573455705532\n" +
                "17585,701,99,5,8,5,3,1,-1,-1,-1,1573453316358\n" +
                "16884,23,94,-5,7,9,-2,-1,-1,-1,-1,1573449704271\n" +
                "16861,679,99,0,8,6,2,0,-1,-1,-1,0\n" +
                "16182,769,99,0,7,5,2,-1,-1,-1,-1,0\n" +
                "15413,344,99,0,9,8,1,1,-1,-1,-1,0\n" +
                "15069,210,0,0,5,5,0,-1,-1,-1,-1,0\n" +
                "14859,587,99,0,5,4,1,-1,-1,-1,-1,0\n" +
                "14272,963,99,0,2,1,1,1,-1,-1,-1,0";
        ArrayList<String> strings = new ArrayList<>();
        String newLine = System.getProperty("line.separator");
        String[] strings1 = body.split(newLine);
        for (int i = 0; i < strings1.length; i++) {
            strings.add(i, strings1[i]);
        }

        for (String s : strings) {
            //Dr-rating(0), DR-delta(1), Sr-Rating(2), Sr-Delta(3), Start Position(4), End Position(5),
            //Position-Delta(6), Clean(7), hasPenalty(8), penalty(9), Fia(10), Epoch(11)
            String[] line = s.split(",");
            score = new Score();

            //Dr
            score.setDr(Integer.valueOf(line[0]));

            //DR-delta
            score.setDrDelta(Integer.valueOf(line[1]));

            //SR
            score.setSr(Integer.valueOf(line[2]));

            //SR-delta
            score.setSrDelta(Integer.valueOf(line[3]));

            //start pos
            score.setStartPosition(Integer.valueOf(line[4]));

            //finish pos
            score.setFinishPosition(Integer.valueOf(line[5]));

            //position delta
            score.setPositionDelta(Integer.valueOf(line[6]));

            //clean
            score.setClean(getBooleanFromString(line[7]));

            //has Penalty
            score.setHasPenalty(getBooleanFromString(line[8]));

            //penalty
            score.setPenalty(getFloatFromString(line[9]));

            //fia
            score.setFiaTournament(getBooleanFromString(line[10]));

            //epoch
            score.setEpoch(Long.valueOf(line[11]));

            scores.add(score);
        }


        SaveLoad.saveScoresDb(this, scores);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(PREF_KEY_FIRST_TIME, false).apply();
    }

    private Boolean getBooleanFromString(String s) {
        if (s.equalsIgnoreCase("1")) {
            return true;
        } else if (s.equalsIgnoreCase("0")) {
            return false;
        } else {
            return null;
        }
    }//getBooleanFromString

    private Float getFloatFromString(String s) {
        if (s.equalsIgnoreCase("-1")) {
            return null;
        } else {
            return Float.valueOf(s);
        }
    }

    public static boolean hasFilePermission(Activity activity) {
        return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void getPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }


}//class
