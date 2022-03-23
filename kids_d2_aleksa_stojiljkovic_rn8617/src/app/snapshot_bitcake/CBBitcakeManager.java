package app.snapshot_bitcake;

import app.AppConfig;
import app.CausalBroadcastShared;
import servent.message.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CBBitcakeManager implements BitcakeManager {
    private final AtomicInteger currentAmount = new AtomicInteger(1000);

    public void takeSomeBitcakes(int amount) {
        currentAmount.getAndAdd(-amount);
    }

    public void addSomeBitcakes(int amount) {
        currentAmount.getAndAdd(amount);
    }

    public int getCurrentBitcakeAmount() {
        return currentAmount.get();
    }


    public int recordedAmount = 0;

    public void markerEvent(int collectorId, SnapshotCollector snapshotCollector) {
        recordedAmount = getCurrentBitcakeAmount();

        CBSnapshotResult CBsnapshotResult = new CBSnapshotResult(AppConfig.myServentInfo.getId(), recordedAmount);
        if (collectorId == AppConfig.myServentInfo.getId()) {
            snapshotCollector.addCBSnapshotInfo(
                    AppConfig.myServentInfo.getId(),
                    CBsnapshotResult);
//            CBMarkerMessage markerMessage = new CBMarker
////            moj casovnik
//            Map<Integer,Integer> mojClock = CausalBroadcastShared.getVectorClock();
//
//            //kopija mog casovnika
//            Map<Integer,Integer> kopijaClocka = new ConcurrentHashMap<>();
//
//            kopijaClocka.putAll(mojClock);
//
//            //ovde sada treba slati markere
//
//            Message marker = new CBMarkerMessage(AppConfig.myServentInfo,null,AppConfig.myServentInfo,kopijaClocka);
//
//
//            for (Integer sused :AppConfig.myServentInfo.getNeighbors()) {
////                Message markerPor = new CBMarkerMessage(AppConfig.myServentInfo,AppConfig.getInfoById(sused),AppConfig.myServentInfo,kopijaClocka,msgid);
//
//                MessageUtil.sendMessage(marker.changeReceiver(sused));
//                try {
//                    /**
//                     * This sleep is here to artificially produce some white node -> red node messages
//                     */
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

        } else {

            //moj casovnik
            Map<Integer, Integer> mojClock = CausalBroadcastShared.getVectorClock();

            //kopija mog casovnika
            Map<Integer, Integer> kopijaClocka = new ConcurrentHashMap<>();

            kopijaClocka.putAll(mojClock);

            Message tell = new CBTellMessage(MessageType.TELL, AppConfig.myServentInfo, null, kopijaClocka,
                    AppConfig.getInfoById(collectorId), CBsnapshotResult);

            for (Integer sused : AppConfig.myServentInfo.getNeighbors()) {
                tell = tell.changeReceiver(sused);
                MessageUtil.sendMessage(tell);
            }

//            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
//                Message clMarker = new LYMarkerMessage(AppConfig.myServentInfo, AppConfig.getInfoById(neighbor), collectorId);
//                MessageUtil.sendMessage(clMarker);
//                try {
//                    /**
//                     * This sleep is here to artificially produce some white node -> red node messages
//                     */
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            //napraviti tell poruke i broadcastovati je do komsija
//                Message tellMessage = new LYTellMessage(
//                        AppConfig.myServentInfo, AppConfig.getInfoById(collectorId), snapshotResult);

//                MessageUtil.sendMessage(tellMessage);
        }



//            LYSnapshotResult snapshotResult = new LYSnapshotResult(
//                    AppConfig.myServentInfo.getId(), recordedAmount, giveHistory, getHistory);

//            if (collectorId == AppConfig.myServentInfo.getId()) {
//                snapshotCollector.addLYSnapshotInfo(
//                        AppConfig.myServentInfo.getId(),
//                        snapshotResult);
//            } else {
//
//                Message tellMessage = new LYTellMessage(
//                        AppConfig.myServentInfo, AppConfig.getInfoById(collectorId), snapshotResult);
//
//                MessageUtil.sendMessage(tellMessage);
//            }
//
//            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
//                Message clMarker = new LYMarkerMessage(AppConfig.myServentInfo, AppConfig.getInfoById(neighbor), collectorId);
//                MessageUtil.sendMessage(clMarker);
//                try {
//                    /**
//                     * This sleep is here to artificially produce some white node -> red node messages
//                     */
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
    }

}
