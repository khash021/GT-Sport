package tech.khash.gtsport.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import tech.khash.gtsport.BuildConfig;
import tech.khash.gtsport.MainActivity;
import tech.khash.gtsport.Model.Score;

public class CreateCSV {
    public static final int SAVE_FILE = 1;
    public static final int SHARE_FILE = 2;

    public static void shareCSVFile(Activity activity, ArrayList<Score> scores) throws IOException {
        if (scores == null || scores.size() < 1) {
            return;
        }
        //create file
        long millis = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM.dd.yyyy-HHmm", Locale.getDefault());
        String date = formatter.format(millis);
        String title = "GT-Sport-Export-" + date;
        String fileName = title + ".csv";

        File file = createCsvFile(activity, fileName, scores);
        Uri fileUri = null;
        if (file.exists()) {
            Uri fileUri2 = Uri.fromFile(file);
            String packageName = activity.getApplicationContext().getPackageName();
            //fileUri = FileProvider.getUriForFile(activity.getBaseContext(), packageName, file);
            fileUri = FileProvider.getUriForFile(activity.getBaseContext(), "tech.khash.gtsport", file);
            Uri fileUri3 = FileProvider.getUriForFile(activity.getBaseContext(), ".provider", file);
        }

        if (fileUri != null) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            //sendIntent.setType("text/plain");
            sendIntent.setType("application/csv")
                    .putExtra(Intent.EXTRA_STREAM, fileUri)
                    .putExtra(android.content.Intent.EXTRA_EMAIL, "")
                    .putExtra(Intent.EXTRA_SUBJECT, title)
                    .putExtra(Intent.EXTRA_STREAM, fileUri)
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(sendIntent, "Send Mail"));
        }
    }//shareCSVFile

    private static File createCsvFile(Activity activity, String name, ArrayList<Score> scores) throws IOException {
        File file = new File(activity.getApplicationContext().getFilesDir(), name);
        FileWriter fw = new FileWriter(file.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("Dr-rating, DR-delta, Sr-Rating, Sr-Delta, Start Position, End Position, " +
                "Position-Delta, Clean, hasPenalty, penalty, Fia, Epoch\n");
        for (Score score : scores) {
            String s = createLine(score);
            bw.write(s);
        }
        bw.close();
        return file;
    }//createCsvFile

    public static void saveCsvFileStorage(Activity activity, ArrayList<Score> scores, int action) {
        if (!MainActivity.hasFilePermission(activity)) {
            MainActivity.getPermission(activity);
            if (MainActivity.hasFilePermission(activity)) {
                //create file
                createCsvFileExternal(activity, scores, action);
            } else {
                return;
            }
        } else {
            //create file
            createCsvFileExternal(activity, scores, action);
        }
    }//SaveCsvFileStorage

    private static void createCsvFileExternal(Activity activity, ArrayList<Score> scores, int action) {
        String state = Environment.getExternalStorageState();
        boolean externalStorageAvailable = false;
        boolean externalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            externalStorageAvailable = externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            externalStorageAvailable = true;
            externalStorageWriteable = false;
        } else {
            // Can't read or write
            externalStorageAvailable = externalStorageWriteable = false;
        }

        if (externalStorageAvailable && externalStorageWriteable) {
            File root = android.os.Environment.getExternalStorageDirectory();
            long millis = Calendar.getInstance().getTimeInMillis();
            SimpleDateFormat formatter = new SimpleDateFormat("MMM.dd.yyyy-HHmm", Locale.getDefault());
            String date = formatter.format(millis);
            String title = "GT-Sport-Export-" + date;
            String fileName = title + ".csv";
            try {
                File file = createCsvFileExternal(activity, root, fileName, scores);
                if (file == null) {
                    Toast.makeText(activity, "Error creating file", Toast.LENGTH_SHORT).show();
                } else {
                    if (action == SAVE_FILE) {
                        Toast.makeText(activity, "Created successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Uri uri = null;
                        try {
                            uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID, file);
                        } catch (Exception e) {
                            Toast.makeText(activity.getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                        }
                        if (uri != null) {
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            //sendIntent.setType("text/plain");
                            sendIntent.setType("application/csv")
                                    .putExtra(Intent.EXTRA_STREAM, uri)
                                    .putExtra(android.content.Intent.EXTRA_EMAIL, "")
                                    .putExtra(Intent.EXTRA_SUBJECT, title)
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(Intent.createChooser(sendIntent, "Send Mail"));
                        } else {
                            Toast.makeText(activity, "Error creating file", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity, "Error creating file", Toast.LENGTH_SHORT).show();
            }
        }
    }//createCsvFileExternal

    private static File createCsvFileExternal(Activity activity, File root, String name, ArrayList<Score> scores) throws IOException {
        File dir = new File(root.getAbsolutePath() + "/GT-Sport");
        dir.mkdir();
        File file = new File(dir, name);
        FileWriter fw = new FileWriter(file.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("Dr-rating, DR-delta, Sr-Rating, Sr-Delta, Start Position, End Position, " +
                "Position-Delta, Clean, hasPenalty, penalty, Fia, Epoch\n");
        for (Score score : scores) {
            String s = createLine(score);
            bw.write(s);
        }
        bw.close();
        return file;
    }//createCsvFile

    public static String getCsv(ArrayList<Score> scores) {
        String output = "Dr-rating, DR-delta, Sr-Rating, Sr-Delta, Start Position, End Position, " +
                "Position-Delta, Clean, hasPenalty, penalty, Fia, Epoch\n";
        for (Score score : scores) {
            output += createLine(score);
        }//for
        return output;
    }//getCsv

    private static String createLine(Score score) {
        Boolean clean = score.isClean();
        String cleanString = "";
        if (clean != null) {
            cleanString = (clean) ? "1" : "0";
        } else {
            cleanString = "-1";
        }

        Integer drDelta = score.getDrDelta();
        String drDeltaString = "";
        if (drDelta != null) {
            drDeltaString = String.valueOf(drDelta);
        } else {
            drDeltaString = "-1";
        }

        Integer srDelta = score.getSrDelta();
        String srDeltaString = "";
        if (srDelta != null) {
            srDeltaString = String.valueOf(srDelta);
        } else {
            srDeltaString = "-1";
        }

        Integer startPosition = score.getStartPosition();
        String startPositionString = "";
        if (startPosition != null) {
            startPositionString = String.valueOf(startPosition);
        } else {
            startPositionString = "-1";
        }

        Integer finishPosition = score.getFinishPosition();
        String finishPositionString = "";
        if (srDelta != null) {
            finishPositionString = String.valueOf(finishPosition);
        } else {
            finishPositionString = "-1";
        }

        Integer positionDelta = score.getPositionDelta();
        String positionDeltaString = "";
        if (positionDelta != null) {
            positionDeltaString = String.valueOf(positionDelta);
        } else {
            positionDeltaString = "-1";
        }

        Boolean fia = score.getFiaTournament();
        String fiaString = "";
        if (fia != null) {
            fiaString = (fia) ? "1" : "0";
        } else {
            fiaString = "-1";
        }

        Boolean hasPenalty = score.getHasPenalty();
        String hasPenaltyString = "";
        String penaltyString = "";
        if (hasPenalty != null) {
            hasPenaltyString = (hasPenalty) ? "1" : "0";
            Float penalty = score.getPenalty();
            if (penalty != null) {
                penaltyString = String.valueOf(penalty);
            }
        } else {
            hasPenaltyString = "-1";
            penaltyString = "-1";
        }


        long epoch = score.getEpoch();
        String output = score.getDr() + "," + drDeltaString + "," + score.getSr() + "," +
                srDeltaString + "," + startPositionString + "," + finishPositionString + "," +
                positionDeltaString + "," + cleanString + "," + hasPenaltyString + "," + penaltyString
                + "," + fiaString + "," + epoch + "\n";
        return output;
    }//createLine
}
