package tech.khash.gtsport.Model;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Score {

    private int sr, dr;
    private long epoch;
    private String date;
    private Integer srDelta = null, drDelta = null;
    private Boolean isClean = null;
    private Integer positionDelta = null, startPosition = null, finishPosition = null;
    private Boolean hasPenalty = null;
    private Float penalty = null;


    public Score() { }

    public Score (int dr, int sr, long epoch) {
        this.dr = dr;
        this.sr = sr;
        this.epoch = epoch;
        SimpleDateFormat formatter = new SimpleDateFormat("MMM.dd.yyyy - HH:mm", Locale.getDefault());
        date = formatter.format(epoch);
    }

    //Getter methods
    public int getSr() {
        return sr;
    }

    public String getSrString() {
        return String.valueOf(sr);
    }

    public int getDr() {
        return dr;
    }

    public String getDrString() {
        return String.format(Locale.US, "%,d", dr);
    }

    public String getDate() {
        return date;
    }

    public Integer getSrDelta() {
        return srDelta;
    }

    public Integer getDrDelta() {
        return drDelta;
    }

    public Integer getPositionDelta() {
        return positionDelta;
    }

    public long getEpoch() {
        return epoch;
    }

    public Integer getStartPosition() {
        return startPosition;
    }

    public Integer getFinishPosition() {
        return finishPosition;
    }

    public Boolean getHasPenalty() {
        return hasPenalty;
    }

    public Float getPenalty() {
        return penalty;
    }

    //Setter Methods

    public void setSr(int sr) {
        this.sr = sr;
    }

    public void setDr(int dr) {
        this.dr = dr;
    }

    public void setSrDelta(int srDelta) {
        this.srDelta = srDelta;
    }

    public void setDrDelta(int drDelta) {
        this.drDelta = drDelta;
    }

    public void setClean(boolean clean) {
        isClean = clean;
    }

    public void setPositionDelta(int positionDelta) {
        this.positionDelta = positionDelta;
    }

    public void setStartPosition(Integer startPosition) {
        this.startPosition = startPosition;
    }

    public void setFinishPosition(Integer finishPosition) {
        this.finishPosition = finishPosition;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHasPenalty(boolean b) {
        hasPenalty = b;
    }

    public void setPenalty(float f) {
        penalty = f;
    }

    public Boolean isClean() {
        return isClean;
    }

    public boolean equalsTo(Score score) {
        return score.dr == this.dr && score.sr == this.sr;
    }
}
