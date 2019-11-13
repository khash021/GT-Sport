package tech.khash.gtsport.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import tech.khash.gtsport.Model.Score;
import tech.khash.gtsport.R;

public class ScoreListAdapter extends RecyclerView.Adapter<ScoreListAdapter.ScoreViewHolder> {

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
                holder.positionImage.setImageResource(R.drawable.up);
            } else if (positionDelta < 0) {
                holder.positionImage.setImageResource(R.drawable.down);
            }
        }

        Integer startPosition = score.getStartPosition();
        Integer finishPosition = score.getFinishPosition();
        if (startPosition != null && finishPosition != null) {
            String details = startPosition + " --> " + finishPosition;
            holder.positionDetailText.setText(details);
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
            }//if has penalty
        }//if null penalty

    }//onBindViewHolder

    @Override
    public int getItemCount() {
        if (scores == null) {
            return 0;
        }
        return scores.size();
    }//getItemCount

    //Inner class for the view holder
    class ScoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //our views
        final TextView drText, srText, dateText, positionText, positionDetailText, srDelta, drDelta,
        penaltyText;
        final ImageView cleanImage, positionImage, podiumImage;
        final LinearLayout penaltyLayout;

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

            itemView.setOnClickListener(this);
        }//ScoreViewHolder

        @Override
        public void onClick(View v) {
            //get the position
            int position = getLayoutPosition();
            listClickListener.onClick(position);
        }
    }//ScoreViewHolder


}//ScoreListAdapter
