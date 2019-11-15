package tech.khash.gtsport;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.core.app.ShareCompat;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.Utils.DocAsynkLoader;
import tech.khash.gtsport.Utils.SaveLoad;

import static tech.khash.gtsport.Utils.CreateCSV.getCsv;
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
            case R.id.action_export:
                String body = getCsv(scores);
                String title = "GT Sport export - ";
                long millis = Calendar.getInstance().getTimeInMillis();
                SimpleDateFormat formatter = new SimpleDateFormat("MMM.dd.yyyy - HH:mm", Locale.getDefault());
                String date = formatter.format(millis);
                title += date;
                ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setChooserTitle("Export GT Sport data")
                        .setSubject(title)
                        .setText(body)
                        .startChooser();
            default:
                return super.onOptionsItemSelected(item);
        }//switch
    }//onOptionsItemSelected

    private void getData() {
        //load the page in the background
        DocAsynkLoader docLoader = new DocAsynkLoader(new DocAsynkLoader.AsyncResponse() {
            @Override
            public void processFinish(Document doc) {
                //extract the data
                getSource(doc);
            }
        });
        //start task and show progressbar
        progressBar.setVisibility(View.VISIBLE);
        docLoader.execute("https://www.kudosprime.com/gts/stats.php?profile=7793649#dr");
    }//getData

    private void getSource(Document doc) {
        //remove progressbar
        progressBar.setVisibility(View.GONE);
        //check for errors the data is empty
        if (doc == null) {
            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
        } else {
            getScore(doc);
        }//if-else
    }//getSource

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

        score = new Score();
        score.setDr(16861);
        score.setSr(99);
        score.setDate("Nov.10.2019 - 13:41");
        score.setStartPosition(8);
        score.setFinishPosition(6);
        score.setPositionDelta(2);
        score.setDrDelta(679);
        score.setSrDelta(0);
        scores.add(score);

        score = new Score();
        score.setDr(16182);
        score.setSr(99);
        score.setDate("Nov.10.2019 - 13:21");
        score.setStartPosition(7);
        score.setFinishPosition(5);
        score.setPositionDelta(2);
        score.setDrDelta(769);
        score.setSrDelta(0);
        scores.add(score);

        score = new Score();
        score.setDr(15413);
        score.setSr(99);
        score.setDate("Nov.10.2019 - 13:01");
        score.setStartPosition(9);
        score.setFinishPosition(8);
        score.setPositionDelta(1);
        score.setClean(true);
        score.setDrDelta(344);
        score.setSrDelta(0);
        scores.add(score);

        score = new Score();
        score.setDr(15069);
        score.setSr(00);
        score.setDate("Nov.10.2019 - 12:21");
        score.setStartPosition(5);
        score.setFinishPosition(5);
        score.setPositionDelta(0);
        score.setDrDelta(210);
        score.setSrDelta(0);
        scores.add(score);

        score = new Score();
        score.setDr(14859);
        score.setSr(99);
        score.setDate("Nov.10.2019 - 12:01");
        score.setStartPosition(5);
        score.setFinishPosition(4);
        score.setPositionDelta(1);
        score.setDrDelta(587);
        score.setSrDelta(0);
        scores.add(score);

        score = new Score();
        score.setDr(14272);
        score.setSr(99);
        score.setDate("Nov.10.2019 - 09:57");
        score.setStartPosition(2);
        score.setFinishPosition(1);
        score.setPositionDelta(1);
        score.setDrDelta(963);
        score.setSrDelta(0);
        score.setClean(true);
        scores.add(score);

        SaveLoad.saveScoresDb(this, scores);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(PREF_KEY_FIRST_TIME, false).apply();
    }
}//class
