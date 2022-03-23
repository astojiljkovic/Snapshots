package app.snapshot_bitcake;

import app.AppConfig;
import app.CausalBroadcastShared;
import servent.message.CBMarkerMessage;
import servent.message.Message;
import servent.message.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CBSnapshotCWorker implements SnapshotCollector {


    private volatile boolean working = true;
    private BitcakeManager bitcakeManager;
    private AtomicBoolean collecting = new AtomicBoolean(false);

    private Map<Integer, CBSnapshotResult> collectedCBValues = new ConcurrentHashMap<>();

    public CBSnapshotCWorker() {
        this.bitcakeManager = new CBBitcakeManager();
    }

    @Override
    public BitcakeManager getBitcakeManager() {
        return bitcakeManager;
    }

    @Override
    public void addCBSnapshotInfo(int id, CBSnapshotResult cbSnapshotResult) {
        if(!collectedCBValues.containsKey(id)){
            collectedCBValues.put(id,cbSnapshotResult);
        }
    }


    @Override
    public void run() {
        while (working){
            /*
             * Not collecting yet - just sleep until we start actual work, or finish
             */
            while (collecting.get() == false) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (working == false) {
                    return;
                }
            }

            /*
             * Collecting is done in three stages:
             * 1. Send messages asking for values
             * 2. Wait for all the responses
             * 3. Print result
             */

            //1 send asks
            ((CBBitcakeManager)bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(),this);

            //moj casovnik
            Map<Integer,Integer> mojClock = CausalBroadcastShared.getVectorClock();

            //kopija mog casovnika
            Map<Integer,Integer> kopijaClocka = new ConcurrentHashMap<>();

            kopijaClocka.putAll(mojClock);

            //ovde sada treba slati markere

            Message marker = new CBMarkerMessage(AppConfig.myServentInfo,null,AppConfig.myServentInfo,kopijaClocka);
            int msgid = marker.getMessageId();


            for (Integer sused :AppConfig.myServentInfo.getNeighbors()) {
                Message markerPor = new CBMarkerMessage(AppConfig.myServentInfo,AppConfig.getInfoById(sused),AppConfig.myServentInfo,kopijaClocka,msgid);
                MessageUtil.sendMessage(markerPor);
                try {
                    /**
                     * This sleep is here to artificially produce some white node -> red node messages
                     */
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            boolean waiting = true;
            while(waiting){

                if(collectedCBValues.size() == AppConfig.getServentCount()){
                    waiting=false;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (working == false) {
                    return;
                }

            }

            int sum=0;

            for (Map.Entry<Integer, CBSnapshotResult> nodeResult : collectedCBValues.entrySet()) {
                sum += nodeResult.getValue().getRecordedAmount();
                AppConfig.timestampedStandardPrint("Recorded bitcake amount for " + nodeResult.getKey() + " = " + nodeResult.getValue().getRecordedAmount());
            }

            AppConfig.timestampedStandardPrint("System bitcake count: " + sum);

            collectedCBValues.clear(); //reset for next invocation
            collecting.set(false);

        }
    }



    @Override
    public void startCollecting() {
        boolean oldValue = this.collecting.getAndSet(true);

        if (oldValue == true) {
            AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
        }

    }

    @Override
    public void stop() {
        working = false;
    }


}
