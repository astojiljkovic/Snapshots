package servent.message;

import app.AppConfig;
import app.ServentInfo;

import java.util.Map;

public class CBMarkerMessage extends BasicMessage {

    private Map<Integer,Integer> vectorClock;

    public CBMarkerMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo, ServentInfo collector,
                           Map<Integer,Integer>vectorClock) {
        super(MessageType.MARKER, originalSenderInfo, receiverInfo, collector);

        this.vectorClock=vectorClock;
    }

    public CBMarkerMessage(ServentInfo senderInfo,ServentInfo receiverInfo,ServentInfo collector,
                           Map<Integer,Integer>vectorClock,int messageid){
        super(MessageType.MARKER,senderInfo,receiverInfo,collector,messageid);

        this.vectorClock=vectorClock;
    }


//    public CBMarkerMessage(ServentInfo senderInfo, ServentInfo receiverInfo,)


    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            Message toReturn = new CBMarkerMessage( getOriginalSenderInfo(),
                    newReceiverInfo,getCollector(),getVectorClock(), getMessageId());

            return toReturn;
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");

            return null;
        }
    }

    public Map<Integer, Integer> getVectorClock() {
        return vectorClock;
    }
}
