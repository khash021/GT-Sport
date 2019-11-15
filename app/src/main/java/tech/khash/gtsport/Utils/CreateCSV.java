package tech.khash.gtsport.Utils;

import java.util.ArrayList;

import tech.khash.gtsport.Model.Score;

public class CreateCSV {

    public static String getCsv(ArrayList<Score> scores) {
        String output = "Dr-rating, DR-delta, Sr-Rating, Sr-Delta, Start Position, End Position, Position-Delta, Clean, Fia, Epoch\n";
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

        long epoch = score.getEpoch();
        String output = score.getDr() + "," + drDeltaString + "," + score.getSr() + "," +
                srDeltaString + "," + startPositionString + "," + finishPositionString + "," +
                positionDeltaString + "," + cleanString + "," + fiaString + "," + epoch + "\n";
        return output;
    }//createLine
}
