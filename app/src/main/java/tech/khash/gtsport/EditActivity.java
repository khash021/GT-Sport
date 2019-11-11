package tech.khash.gtsport;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.Utils.SaveLoad;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EditActivity.class.getSimpleName();

    private Score currentScore;
    private Boolean isClean = null;
    private EditText editstart, editFinish;
    private TextView textPositionDelta, textDr, textSr, textDrDelta, textSrDelta;
    private ImageView imageDr, imageSr, imagePosition;
    private Integer startPosition = null, finishPosition = null, positionDelta = null;
    private boolean unsavedChanges = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        if (getIntent().hasExtra(ListActivity.INTENT_EXTRA_SCORE)) {
            String scoreGson = getIntent().getStringExtra(ListActivity.INTENT_EXTRA_SCORE);
            Gson gson = new Gson();
            currentScore = gson.fromJson(scoreGson, Score.class);
        } else {
            finish();
        }

        //find views
        textDr = findViewById(R.id.text_dr);
        textSr = findViewById(R.id.text_sr);
        textDrDelta = findViewById(R.id.text_dr_delta);
        textSrDelta = findViewById(R.id.text_sr_delta);
        imageDr = findViewById(R.id.image_dr);
        imageSr = findViewById(R.id.image_sr);

        textPositionDelta = findViewById(R.id.text_position);
        imagePosition = findViewById(R.id.image_position_delta);

        editstart = findViewById(R.id.edit_start);
        editFinish = findViewById(R.id.edit_finish);

        //populate views
        textDr.setText(currentScore.getDrString());
        textSr.setText(currentScore.getSrString());

        Integer drDelta = currentScore.getDrDelta();
        if (drDelta != null) {
            if (drDelta != 0) {
                textDrDelta.setText(String.valueOf(drDelta));
            }
            if (drDelta > 0) {
                imageDr.setImageResource(R.drawable.up);
            } else if (drDelta < 0) {
                imageDr.setImageResource(R.drawable.down);
            }
        }

        Integer srDelta = currentScore.getSrDelta();
        if (srDelta != null) {
            if (srDelta != 0) {
                textSrDelta.setText(String.valueOf(srDelta));
            }
            if (srDelta > 0) {
                imageSr.setImageResource(R.drawable.up);
            } else if (srDelta < 0) {
                imageSr.setImageResource(R.drawable.down);
            }
        }

        Integer start = currentScore.getStartPosition();
        if (start != null) {
            editstart.setText(String.valueOf(start));
        }

        Integer finish = currentScore.getFinishPosition();
        if (finish != null) {
            editFinish.setText(String.valueOf(finish));
        }

        Integer posDelta = currentScore.getPositionDelta();
        if (posDelta != null) {
            textPositionDelta.setText(String.valueOf(posDelta));
            if (posDelta > 0) {
                imagePosition.setImageResource(R.drawable.up);
            } else if (posDelta < 0) {
                imagePosition.setImageResource(R.drawable.down);
            }
        }

        //listeners
        CheckBox cleanBox = findViewById(R.id.check_clean);
        cleanBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isClean = isChecked;
                unsavedChanges = true;
            }
        });
        Boolean clean = currentScore.isClean();
        if (clean != null) {
            cleanBox.setChecked(clean);
        }

        editstart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    startPosition = Integer.valueOf(s.toString());
                    if (finishPosition != null) {
                        positionDelta = startPosition - finishPosition;
                        textPositionDelta.setText(String.valueOf(positionDelta));
                    }
                    unsavedChanges = true;
                } catch (Exception e) {
                    Log.d(TAG, "Error", e);
                }
            }
        });
        editstart.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == 0 || actionId== EditorInfo.IME_ACTION_DONE) {
                    editFinish.requestFocus();
                }
                return false;
            }
        });

        editFinish.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    finishPosition = Integer.valueOf(s.toString());
                    if (startPosition != null) {
                        positionDelta = startPosition - finishPosition;
                        textPositionDelta.setText(String.valueOf(positionDelta));
                        unsavedChanges = true;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error", e);
                }
            }
        });

        //add this listener so when the user presses enter, this gets called and we can hide the keyboard
        editstart.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //check for the enter key
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    //enter key has been pressed and we hide the keyboard
                    hideKeyboard();
                    //return true to let it know we handled the event
                    return true;
                }
                return false;
            }
        });

        editFinish.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //check for the enter key
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    //enter key has been pressed and we hide the keyboard
                    hideKeyboard();
                    //return true to let it know we handled the event
                    return true;
                }
                return false;
            }
        });

        editstart.setCursorVisible(false);
        editFinish.setCursorVisible(false);

        editstart.setOnClickListener(this);
        editFinish.setOnClickListener(this);
        textPositionDelta.setOnClickListener(this);
        textDr.setOnClickListener(this);
        textSr.setOnClickListener(this);
        textDrDelta.setOnClickListener(this);
        textSrDelta.setOnClickListener(this);
        imageDr.setOnClickListener(this);
        imageSr.setOnClickListener(this);
        imagePosition.setOnClickListener(this);
    }//onCreate

    @Override
    public void onBackPressed() {
        if (!unsavedChanges) {
            super.onBackPressed();
        } else {
            showUnsavedChangesDialog();
        }
    }//onBackPressed

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }//onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (unsavedChanges) {
                    showUnsavedChangesDialog();
                    //We return true, so the normal behavior doesn't continue (i.e. return to home screen)
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
            case R.id.action_save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }//switch
    }//onOptionsItemSelected

    private void save() {
        //get data
        if (isClean != null) {
            currentScore.setClean(isClean);
        }

        if (positionDelta != null) {
            currentScore.setPositionDelta(positionDelta);
        }

        if (startPosition != null) {
            currentScore.setStartPosition(startPosition);
        }

        if (finishPosition != null) {
            currentScore.setFinishPosition(finishPosition);
        }
        SaveLoad.replaceScoreInDb(this, currentScore);

        Intent intent = new Intent(EditActivity.this, ListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.edit_start:
                unsavedChanges = true;
                editstart.setCursorVisible(true);
                return;
            case R.id.edit_finish:
                unsavedChanges = true;
                editFinish.setCursorVisible(true);
                return;
            case R.id.text_dr:
            case R.id.text_sr:
            case R.id.text_sr_delta:
            case R.id.text_dr_delta:
            case R.id.image_dr:
            case R.id.image_sr:
            case R.id.text_position:
            case R.id.image_position:
                editFinish.setCursorVisible(false);
                editstart.setCursorVisible(false);
                return;
        }
    }//onClick

    //Helper method for showing the dialog for unsaved data
    private void showUnsavedChangesDialog() {
        //create the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //add message and button functionality
        builder.setMessage("Discard your changes and quit?")
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //make the sdiscard boolean false and go to back pressed to follow normal hierarchical back
                        unsavedChanges = false;
                        //continue with back button
                        onBackPressed();
                    }
                })
                .setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }//showUnsavedChangesDialog

    //This method hides the soft keyboard and is used when the user clicks Enter on the soft keyboard
    private void hideKeyboard() {
        //check to make sure no view has focus
        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }//hideKeyboard

}//class