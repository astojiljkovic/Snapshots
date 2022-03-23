package servent.handler;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.*;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CBMarkerMessageHandler implements MessageHandler {

    private Message clientMessage;
    private SnapshotCollector snapshotCollector;
    private static Set<Message> receivedBroadcastsMarkers = Collections.newSetFromMap(new ConcurrentHashMap<Message, Boolean>());

    public CBMarkerMessageHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {

        boolean didPut = receivedBroadcastsMarkers.add(clientMessage);

        if (didPut){
            CBMarkerMessage cbtm = (CBMarkerMessage) clientMessage;
            //New message for us. Rebroadcast it.
            AppConfig.timestampedStandardPrint("Rebroadcasting... " + receivedBroadcastsMarkers.size());

            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                //Same message, different receiver, and add us to the route table.
                MessageUtil.sendMessage(cbtm.changeReceiver(neighbor));
            }

            CausalBroadcastShared.setSnapshotCollector(snapshotCollector);
            CausalBroadcastShared.addPendingMessage(clientMessage);
            CausalBroadcastShared.checkPendingMessages();
        }

    }
}
