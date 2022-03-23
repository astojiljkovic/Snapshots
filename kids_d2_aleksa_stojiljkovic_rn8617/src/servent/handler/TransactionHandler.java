package servent.handler;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.Transaction;
import servent.message.Message;
import servent.message.MessageUtil;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the CAUSAL_BROADCAST message. Fairly simple, as we assume that we are
 * in a clique. We add the message to a pending queue, and let the check on the queue
 * take care of the rest.
 * @author bmilojkovic
 *
 */
public class TransactionHandler implements MessageHandler {

	private Message clientMessage;
	private SnapshotCollector snapshotCollector;
	private static Set<Message> receivedBroadcastsTransactions = Collections.newSetFromMap(new ConcurrentHashMap<Message, Boolean>());
	
	public TransactionHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
		this.clientMessage = clientMessage;
		this.snapshotCollector=snapshotCollector;
	}
	
	@Override
	public void run() {
		//Sanity check.

		boolean didPut = receivedBroadcastsTransactions.add(clientMessage);


		if (didPut){
			Transaction cbm = (Transaction) clientMessage;
			//New message for us. Rebroadcast it.
			AppConfig.timestampedStandardPrint("Rebroadcasting... " + receivedBroadcastsTransactions.size());

			for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
				//Same message, different receiver, and add us to the route table.
				MessageUtil.sendMessage(cbm.changeReceiver(neighbor));
			}

			CausalBroadcastShared.setSnapshotCollector(snapshotCollector);
			CausalBroadcastShared.addPendingMessage(clientMessage);
			CausalBroadcastShared.checkPendingMessages();

		}


		/*
			 * Same print as the one in BROADCAST handler.
			 * Kind of useless here, as we assume a clique.
			 */
			
			/*
			ServentInfo senderInfo = clientMessage.getOriginalSenderInfo();
			ServentInfo lastSenderInfo = clientMessage.getRoute().size() == 0 ?
					clientMessage.getOriginalSenderInfo() :
					clientMessage.getRoute().get(clientMessage.getRoute().size()-1);
			
			String text = String.format("Got %s from %s causally broadcast by %s\n",
					clientMessage.getMessageText(), lastSenderInfo, senderInfo);
			AppConfig.timestampedStandardPrint(text);
			*/
			
			/* 
			 * Uncomment the next line and comment out the two afterwards
			 * to see what happens when causality is broken.
			 */
//			CausalBroadcastShared.commitCausalMessage(clientMessage);


	}

}
