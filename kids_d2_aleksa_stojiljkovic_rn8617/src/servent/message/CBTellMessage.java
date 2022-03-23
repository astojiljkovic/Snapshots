package servent.message;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.CBSnapshotResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CBTellMessage extends BasicMessage {

    private Map<Integer,Integer> vectorClock;
    private ServentInfo sendingSnapTo;
    private CBSnapshotResult CBSnapshotResult;



    public CBTellMessage(MessageType messageType,ServentInfo senderInfo, ServentInfo receiverInfo, Map<Integer,Integer> vectorClock,
                         ServentInfo collector, CBSnapshotResult CBSnapshotResult) {
        super(messageType,senderInfo,receiverInfo,collector);
        this.vectorClock=vectorClock;
        this.CBSnapshotResult = CBSnapshotResult;
        this.sendingSnapTo=collector;

    }
    public CBTellMessage(MessageType messageType,ServentInfo senderInfo, ServentInfo receiverInfo, Map<Integer,Integer> vectorClock,
                         ServentInfo collector, CBSnapshotResult CBSnapshotResult,int messageId) {
        super(messageType,senderInfo,receiverInfo,collector,messageId);
        this.vectorClock=vectorClock;
        this.CBSnapshotResult = CBSnapshotResult;
        this.sendingSnapTo=collector;

    }


    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            Message toReturn = new CBTellMessage(getMessageType(), getOriginalSenderInfo(),
                    newReceiverInfo,getVectorClock(),getSendingSnapTo(),getCBSnapshotResult(), getMessageId());

            return toReturn;
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");

            return null;
        }
    }

    public Map<Integer, Integer> getVectorClock() {
        return vectorClock;
    }

    public ServentInfo getSendingSnapTo() {
        return sendingSnapTo;
    }

    public app.snapshot_bitcake.CBSnapshotResult getCBSnapshotResult() {
        return CBSnapshotResult;
    }
}
