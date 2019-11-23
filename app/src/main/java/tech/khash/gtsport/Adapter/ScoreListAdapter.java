package tech.khash.gtsport.Adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.R;

public class ScoreListAdapter extends RecyclerView.Adapter<ScoreListAdapter.ScoreViewHolder> {

    private static final String TAG = ScoreListAdapter.class.getSimpleName();

    //list of data
    private final ArrayList<Score> scores;
    //inflater used for creating the view
    private LayoutInflater inflater;
    //context
    private Context context;

    //Listener for long clicks
    private ListClickListener listClickListener;

    public interface ListClickListener {
        void onClick(int position);
    }//ListLongClickListener

    public ScoreListAdapter(Context context, ArrayList<Score> scores, ListClickListener listClickListener) {
        this.scores = scores;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.listClickListener = listClickListener;
    }


    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewItem = inflater.inflate(R.layout.list_item, parent, false);
        return new ScoreViewHolder(viewItem);
    }//onCreateViewHolder

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        //get score
        Score score = scores.get(position);

        holder.drText.setText(score.getDrString());
        holder.srText.setText(score.getSrString());
        holder.dateText.setText(score.getDate());

        //dr delta
        Integer drDelta = score.getDrDelta();
        if (drDelta != null) {
            holder.drDelta.setText(String.valueOf(drDelta));
            if (drDelta >= 0) {
                holder.drDelta.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                holder.drDelta.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        //sr delta
        Integer srDelta = score.getSrDelta();
        if (srDelta != null) {
            holder.srDelta.setText(String.valueOf(srDelta));
            if (srDelta >= 0) {
                holder.srDelta.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                holder.srDelta.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        //clean
        Boolean clean = score.isClean();
        if (clean != null && clean) {
            holder.cleanImage.setVisibility(View.VISIBLE);
        }

        //position
        Integer positionDelta = score.getPositionDelta();
        if (positionDelta != null) {
            holder.positionText.setText(String.valueOf(positionDelta));
            if (positionDelta > 0) {
                holder.positionImage.setVisibility(View.VISIBLE);
                holder.positionImage.setImageResource(R.drawable.up);
            } else if (positionDelta < 0) {
                holder.positionImage.setVisibility(View.VISIBLE);
                holder.positionImage.setImageResource(R.drawable.down);
            } else if (positionDelta == 0) {
                holder.positionImage.setVisibility(View.INVISIBLE);
            }
        }

        Integer startPosition = score.getStartPosition();
        Integer finishPosition = score.getFinishPosition();
        if (startPosition != null && finishPosition != null) {
            if (startPosition == finishPosition) {
                holder.positionDetailText.setText(String.valueOf(startPosition));
            } else {
                try {
                    String details = startPosition + "\u2192" + finishPosition;
                    int start = details.indexOf("\u2192");
                    int end = details.indexOf(String.valueOf(finishPosition));
                    SpannableString ss = new SpannableString(details);
                    ss.setSpan(new RelativeSizeSpan(2f), start, end, 0);
                    holder.positionDetailText.setText(ss);
                } catch (Exception e) {
                    Log.e(TAG, "Error Spanning text:", e);
                    String details = startPosition + " \u2192 " + finishPosition;
                    holder.positionDetailText.setText(details);
                }
            }
        }

        if (finishPosition != null) {
            if (finishPosition <= 3) {
                holder.podiumImage.setVisibility(View.VISIBLE);
            }
        }

        //penalty
        Boolean hasPenalty = score.getHasPenalty();
        if (hasPenalty != null) {
            if (hasPenalty) {
                Float penaltyFloat = score.getPenalty();
                if (penaltyFloat != null) {
                    holder.penaltyLayout.setVisibility(View.VISIBLE);
                    String result = String.format("%.3f", penaltyFloat);
                    result = "+" + result;
                    holder.penaltyText.setText(result);
                }//penalty float
            } else {
                holder.penaltyLayout.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.penaltyLayout.setVisibility(View.INVISIBLE);
        }

        Boolean fia = score.getFiaTournament();
        if (fia == null || !fia) {
            holder.rootView.setBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            holder.rootView.setBackgroundColor(context.getResources().getColor(R.color.fia_background));
        }

    }//onBindViewHolder

    @Override
    public int getItemCount() {
        if (scores == null) {
            return 0;
        }
        return scores.size();
    }//getItemCount

    //Inner class for the view holder
    class ScoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //our views
        final TextView drText, srText, dateText, positionText, positionDetailText, srDelta, drDelta,
                penaltyText;
        final ImageView cleanImage, positionImage, podiumImage;
        final LinearLayout penaltyLayout;
        final ConstraintLayout rootView;

        //constructor
        private ScoreViewHolder(View itemView) {
            super(itemView);
            //find view
            drText = itemView.findViewById(R.id.dr);
            srText = itemView.findViewById(R.id.sr);
            dateText = itemView.findViewById(R.id.text_date);
            cleanImage = itemView.findViewById(R.id.image_clean);
            positionImage = itemView.findViewById(R.id.image_position);
            positionText = itemView.findViewById(R.id.position);
            positionDetailText = itemView.findViewById(R.id.position_details);
            srDelta = itemView.findViewById(R.id.sr_delta);
            drDelta = itemView.findViewById(R.id.dr_delta);
            penaltyLayout = itemView.findViewById(R.id.penalty_container);
            podiumImage = itemView.findViewById(R.id.image_podium);
            penaltyText = itemView.findViewById(R.id.penalty_text);
            rootView = itemView.findViewById(R.id.root_view);

            itemView.setOnClickListener(this);
        }//ScoreViewHolder

        @Override
        public void onClick(View v) {
            //get the position
            int position = getLayoutPosition();
            listClickListener.onClick(position);
            Log.d(TAG, "Index: " + position);
        }
    }//ScoreViewHolder


}//ScoreListAdapter
