package servent.handler;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.CBTellMessage;
import servent.message.Message;
import servent.message.MessageUtil;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CBTellMessageHandler implements MessageHandler {

    private Message clientMessage;
    private SnapshotCollector snapshotCollector;
    private static Set<Message> receivedBroadcastsTell = Collections.newSetFromMap(new ConcurrentHashMap<Message, Boolean>());

    public CBTellMessageHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }


    @Override
    public void run() {

        boolean didPut = receivedBroadcastsTell.add(clientMessage);

        if (didPut){
            CBTellMessage ctm = (CBTellMessage) clientMessage;
            //New message for us. Rebroadcast it.
            AppConfig.timestampedStandardPrint("Rebroadcasting... " + receivedBroadcastsTell.size());

            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                //Same message, different receiver, and add us to the route table.
                MessageUtil.sendMessage(ctm.changeReceiver(neighbor));
            }
            CausalBroadcastShared.setSnapshotCollector(snapshotCollector);
            CausalBroadcastShared.addPendingMessage(clientMessage);
            CausalBroadcastShared.checkPendingMessages();

        }


    }
}
