package tech.khash.gtsport;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import tech.khash.gtsport.Adapter.ScoreListAdapter;
import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.Utils.SaveLoad;

public class ListActivity extends AppCompatActivity implements ScoreListAdapter.ListClickListener {

    RecyclerView recyclerView;
    ScoreListAdapter adapter;
    ArrayList<Score> scores;

    public static final String INTENT_EXTRA_SCORE = "intent-extra-score";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Set the menu_main icon
        ActionBar actionbar = getSupportActionBar();
        //Enable app bar home button
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);

        scores = SaveLoad.loadScoresDb(this);
        recyclerView = findViewById(R.id.recycler_view);

        adapter = new ScoreListAdapter(this, scores, this);
        recyclerView.setAdapter(adapter);
        // Give the RecyclerView a horizontal layout manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        //Add divider between items using the DividerItemDecoration
        DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(decoration);
    }//onCreate

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ListActivity.this, MainActivity.class);
        startActivity(intent);
    }//onBackPressed

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }//onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_chart:
                Intent chartIntent = new Intent(ListActivity.this, MPChartActivity.class);
                startActivity(chartIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }//switch
    }//onOptionsItemSelected

    @Override
    public void onClick(int position) {
        Score score = scores.get(position);

        Gson gson = new Gson();
        String scoreGson = gson.toJson(score);
        Intent intent = new Intent(ListActivity.this, EditActivity.class);
        intent.putExtra(INTENT_EXTRA_SCORE, scoreGson);
        startActivity(intent);
    }
}
