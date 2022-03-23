package app;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

import app.snapshot_bitcake.CBBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.*;

/**
 * This class contains shared data for the Causal Broadcast implementation:
 * <ul>
 * <li> Vector clock for current instance
 * <li> Commited message list
 * <li> Pending queue
 * </ul>
 * As well as operations for working with all of the above.
 * 
 * @author bmilojkovic
 *
 */
public class CausalBroadcastShared {

	private static Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>();
	private static List<Message> commitedCausalMessageList = new CopyOnWriteArrayList<>();
	private static Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
	private static Object pendingMessagesLock = new Object();
	private static SnapshotCollector snapshotCollector;
	
	public static void initializeVectorClock(int serventCount) {
		for(int i = 0; i < serventCount; i++) {
			vectorClock.put(i, 0);
		}
	}
	
	public static void incrementClock(int serventId) {
		vectorClock.computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

			@Override
			public Integer apply(Integer key, Integer oldValue) {
				return oldValue+1;
			}
		});
	}
	
	public static Map<Integer, Integer> getVectorClock() {
		return vectorClock;
	}
	
	public static List<Message> getCommitedCausalMessages() {
		List<Message> toReturn = new CopyOnWriteArrayList<>(commitedCausalMessageList);
		
		return toReturn;
	}
	
	public static void addPendingMessage(Message msg) {
		pendingMessages.add(msg);
	}
	
	public static void commitCausalMessage(Message newMessage) {
		commitedCausalMessageList.add(newMessage);
		incrementClock(newMessage.getOriginalSenderInfo().getId());
		
		checkPendingMessages();
	}
	
	private static boolean otherClockGreater(Map<Integer, Integer> clock1, Map<Integer, Integer> clock2) {
		if (clock1.size() != clock2.size()) {
			throw new IllegalArgumentException("Clocks are not same size how why");
		}
		
		for(int i = 0; i < clock1.size(); i++) {
			if (clock2.get(i) > clock1.get(i)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void checkPendingMessages() {
		boolean gotWork = true;
		
		while (gotWork) {
			gotWork = false;
			
			synchronized (pendingMessagesLock) {
				Iterator<Message> iterator = pendingMessages.iterator();
				
				Map<Integer, Integer> myVectorClock = getVectorClock();
				while (iterator.hasNext()) {
					Message pendingMessage = iterator.next();

					switch (pendingMessage.getMessageType()){
						case TRANSACTION:
							Transaction causalPendingMessage = (Transaction) pendingMessage;

							if (causalPendingMessage.getCollector().getId() == AppConfig.myServentInfo.getId()) {
								if (!otherClockGreater(myVectorClock, causalPendingMessage.getSenderVectorClock())) {
									gotWork = true;

									AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
									commitedCausalMessageList.add(pendingMessage);
									incrementClock(pendingMessage.getOriginalSenderInfo().getId());

									//obrada
									int amount = Integer.parseInt(causalPendingMessage.getMessageText());
									snapshotCollector.getBitcakeManager().addSomeBitcakes(amount);

									iterator.remove();

									break;
								}
							} else {
								AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
								commitedCausalMessageList.add(pendingMessage);
								incrementClock(pendingMessage.getOriginalSenderInfo().getId());

								iterator.remove();
								break;

							}
						case MARKER:
							CBMarkerMessage markerMessage = (CBMarkerMessage) pendingMessage;
							if (!otherClockGreater(myVectorClock, markerMessage.getVectorClock())) {
								gotWork = true;

								AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
								commitedCausalMessageList.add(pendingMessage);
								incrementClock(pendingMessage.getOriginalSenderInfo().getId());


								CBBitcakeManager cbBitcakeManager = (CBBitcakeManager) snapshotCollector.getBitcakeManager();
								cbBitcakeManager.markerEvent(markerMessage.getCollector().getId(),snapshotCollector);

								iterator.remove();

								break;
							}
						case TELL:
							CBTellMessage tellMessage = (CBTellMessage) pendingMessage;
							if (tellMessage.getCollector().getId() == AppConfig.myServentInfo.getId()) {
								if (!otherClockGreater(myVectorClock, tellMessage.getVectorClock())) {
									gotWork = true;

									AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
									commitedCausalMessageList.add(pendingMessage);
									incrementClock(pendingMessage.getOriginalSenderInfo().getId());

									//obrada
									snapshotCollector.addCBSnapshotInfo(tellMessage.getOriginalSenderInfo().getId(),
											tellMessage.getCBSnapshotResult());

									iterator.remove();

									break;
								}
							} else {
								//saljemo dalje
								commitedCausalMessageList.add(pendingMessage);
								incrementClock(pendingMessage.getOriginalSenderInfo().getId());

								iterator.remove();
								break;

							}
					}

				}
			}
		}
		
	}

	public static void setSnapshotCollector(SnapshotCollector snapshotCollector) {
		CausalBroadcastShared.snapshotCollector = snapshotCollector;
	}
}
