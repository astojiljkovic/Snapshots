package app.snapshot_bitcake;

import java.io.Serializable;

public class CBSnapshotResult implements Serializable {

    private static final long serialVersionUID = 8939516333227254439L;

    private final int serventId;
    private final int recordedAmount;

    public CBSnapshotResult(int serventId, int recordedAmount) {
        this.serventId = serventId;
        this.recordedAmount = recordedAmount;
    }


    public int getRecordedAmount() {
        return recordedAmount;
    }
}
