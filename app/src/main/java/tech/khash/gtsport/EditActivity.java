package tech.khash.gtsport;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;

import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.Utils.SaveLoad;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EditActivity.class.getSimpleName();

    private Score currentScore;
    private Boolean isClean = null, hasPenalty = null, isFia = null;
    private EditText editstart, editFinish, editPenalty;
    private TextView textPositionDelta, textDr, textSr, textDrDelta, textSrDelta;
    private ImageView imageDr, imageSr, imagePosition, imageFia, imageClean;
    private Integer startPosition = null, finishPosition = null, positionDelta = null;
    private boolean unsavedChanges = false;
    private Float penaltyFloat = null;
    private ConstraintLayout rootView;

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
        rootView = findViewById(R.id.root_view);

        textDr = findViewById(R.id.text_dr);
        textSr = findViewById(R.id.text_sr);
        textDrDelta = findViewById(R.id.text_dr_delta);
        textSrDelta = findViewById(R.id.text_sr_delta);
        imageDr = findViewById(R.id.image_dr);
        imageSr = findViewById(R.id.image_sr);
        imageFia = findViewById(R.id.image_fia);
        imageClean = findViewById(R.id.image_clean);

        textPositionDelta = findViewById(R.id.text_position);
        imagePosition = findViewById(R.id.image_position_delta);

        editstart = findViewById(R.id.edit_start);
        editFinish = findViewById(R.id.edit_finish);
        editPenalty = findViewById(R.id.edit_penalty);

        //populate views Image and Text
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

        //checkbox
        CheckBox fiaCheck = findViewById(R.id.check_fia);
        CheckBox cleanBox = findViewById(R.id.check_clean);
        final CheckBox penaltyCheck = findViewById(R.id.check_penalty);

        Boolean fia = currentScore.getFiaTournament();
        if (fia != null) {
            fiaCheck.setChecked(fia);
            if (fia) {
                imageFia.setVisibility(View.VISIBLE);
            }
        }

        Boolean clean = currentScore.isClean();
        if (clean != null) {
            cleanBox.setChecked(clean);
            if (clean) {
                imageClean.setVisibility(View.VISIBLE);
            }
        }

        final Boolean penalty = currentScore.getHasPenalty();
        if (penalty != null) {
            hasPenalty = penalty;
            penaltyCheck.setChecked(penalty);
            Float penaltyFloatScore = currentScore.getPenalty();
            if (penaltyFloatScore != null) {
                editPenalty.setText(String.valueOf(penaltyFloatScore));
                penaltyFloat = penaltyFloatScore;
            }
        }

        cleanBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideKeyboard();
                isClean = isChecked;
                unsavedChanges = true;
                if (isChecked) {
                    //fade-in
                    if (!checkVisibility(imageClean)) {
                        imageClean.setVisibility(View.VISIBLE);
                    }
                    MainActivity.animateViewFadeIn(EditActivity.this, imageClean);
                } else {
                    //fade-out
                    if (!checkVisibility(imageClean)) {
                        imageClean.setVisibility(View.VISIBLE);
                    }
                    MainActivity.animateViewFadeOut(EditActivity.this, imageClean);
                }
            }
        });

        fiaCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideKeyboard();
                isFia = isChecked;
                unsavedChanges = true;
                if (isChecked) {
                    //fade-in
                    if (!checkVisibility(imageFia)) {
                        imageFia.setVisibility(View.VISIBLE);
                    }
                    MainActivity.animateViewFadeIn(EditActivity.this, imageFia);
                } else {
                    //fade-out
                    if (!checkVisibility(imageFia)) {
                        imageFia.setVisibility(View.VISIBLE);
                    }
                    MainActivity.animateViewFadeOut(EditActivity.this, imageFia);
                }
            }
        });

        penaltyCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideKeyboard();
                hasPenalty = isChecked;
                unsavedChanges = true;

                //Change the color if it has penalty
                if (isChecked) {
                    penaltyCheck.setTextColor(getApplicationContext().getResources().getColor(R.color.red));
                    editPenalty.setCursorVisible(true);
                    editPenalty.requestFocus();
                    showKeyboard();
                } else {
                    editPenalty.setText(null);
                    editPenalty.setCursorVisible(false);
                    penaltyFloat = null;
                }
            }
        });


        //Edit Texts
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
                if (actionId == 0 || actionId == EditorInfo.IME_ACTION_DONE) {
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

        editPenalty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                unsavedChanges = true;
                try {
                    String string = s.toString().trim();
                    if (!TextUtils.isEmpty(string)) {
                        penaltyFloat = Float.valueOf(s.toString());
                        penaltyCheck.setChecked(true);
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
                    editFinish.setCursorVisible(false);
                    rootView.requestFocus();
                    //return true to let it know we handled the event
                    return true;
                }
                return false;
            }
        });

        editPenalty.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //check for the enter key
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    //enter key has been pressed and we hide the keyboard
                    hideKeyboard();
                    editPenalty.setCursorVisible(false);
                    rootView.requestFocus();
                    //return true to let it know we handled the event
                    return true;
                }
                return false;
            }
        });

        editstart.setCursorVisible(false);
        editFinish.setCursorVisible(false);
        editPenalty.setCursorVisible(false);

        editstart.setOnClickListener(this);
        editFinish.setOnClickListener(this);
        editPenalty.setOnClickListener(this);
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

        if (hasPenalty != null) {
            //has penalty is not null
            currentScore.setHasPenalty(hasPenalty);

            if (hasPenalty) {
                //true penalty
                try {
                    penaltyFloat = Float.valueOf(editPenalty.getText().toString());
                    currentScore.setPenalty(penaltyFloat);
                } catch (Exception e) {
                    currentScore.setPenalty(null);
                    Log.d(TAG, "Error", e);
                }

            } else {
                //false has penalty
                penaltyFloat = null;
                currentScore.setPenalty(penaltyFloat);
            }
        } else {
            //null penalty
            currentScore.setHasPenalty(null);
            currentScore.setPenalty(null);
        }//penalty

        if (isFia != null) {
            currentScore.setFiaTournament(isFia);
        }

        SaveLoad.replaceScoreInDb(this, currentScore);

        Intent intent = new Intent(EditActivity.this, ListActivity.class);
        startActivity(intent);
    }//save

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
            case R.id.edit_penalty:
                unsavedChanges = true;
                editPenalty.setCursorVisible(true);
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
                hideKeyboard();
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

    private void showKeyboard() {
        //check to make sure no view has focus
        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }//showKeyboard

    private boolean checkVisibility(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

}//class
