package tech.khash.gtsport.Model;

import com.anychart.chart.common.dataentry.ValueDataEntry;

public class ScoreEntry extends ValueDataEntry {

    public ScoreEntry (String id, Number dr, Number epoch) {
        super(id, epoch);
        setValue("DR Rating", dr);
    }

    public ScoreEntry (String id, Number dr, Number sr, Number epoch) {
        super(id, epoch);
        setValue("DR Rating", dr);
        setValue("SR Rating", sr);
    }

    public ScoreEntry (String id, Number dr, Number sr, Number positionDelta, Number epoch) {
        super(id, epoch);
        setValue("DR Rating", dr);
        setValue("SR Rating", sr);
        setValue("Position Delta", positionDelta);
    }
}
