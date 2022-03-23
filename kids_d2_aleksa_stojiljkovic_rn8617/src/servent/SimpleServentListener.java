package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.*;
import servent.message.Message;
import servent.message.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

    private volatile boolean working = true;
    private SnapshotCollector snapshotCollector;

    public SimpleServentListener(SnapshotCollector snapshotCollector) {
        this.snapshotCollector = snapshotCollector;
    }

    /*
     * Thread pool for executing the handlers. Each client will get it's own handler thread.
     */
    private final ExecutorService threadPool = Executors.newWorkStealingPool();

    @Override
    public void run() {
        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort());
            /*
             * If there is no connection after 1s, wake up and see if we should terminate.
             */
            listenerSocket.setSoTimeout(1000);
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
            System.exit(0);
        }

        while (working) {
            try {
                /*
                 * This blocks for up to 1s, after which SocketTimeoutException is thrown.
                 */
                Socket clientSocket = listenerSocket.accept();

                //GOT A MESSAGE! <3
                Message clientMessage = MessageUtil.readMessage(clientSocket);
                MessageHandler messageHandler = new NullHandler(clientMessage);

                /*
                 * Each message type has it's own handler.
                 * If we can get away with stateless handlers, we will,
                 * because that way is much simpler and less error prone.
                 */
//                AppConfig.timestampedStandardPrint(clientMessage.getMessageType().toString());
                switch (clientMessage.getMessageType()) {
                    case PING:
                        messageHandler = new PingHandler(clientMessage);
                        break;
                    case PONG:
                        messageHandler = new PongHandler(clientMessage);
                        break;
                    case BROADCAST:
                        //Broadcast handler will rebroadcast the msg if we are not a clique.
                        messageHandler = new BroadcastHandler(clientMessage, !AppConfig.IS_CLIQUE);
                        break;
                    case TRANSACTION:
                        messageHandler = new TransactionHandler(clientMessage,this.snapshotCollector);
                        break;
                    case MARKER:
//                        AppConfig.timestampedStandardPrint("USAO SAM U MARKEREEEEE");
                        messageHandler = new CBMarkerMessageHandler(clientMessage, this.snapshotCollector);
                        break;
					case TELL:
						messageHandler = new CBTellMessageHandler(clientMessage,this.snapshotCollector);
						break;
                    case SUM_HELP_MESSAGE:
                        messageHandler = new SumHelpHandler(clientMessage);
                        break;
                    case SUM_RESULT_MESSAGE:
                        messageHandler = new SumResultHandler(clientMessage);
                        break;
                }

                threadPool.submit(messageHandler);
            } catch (SocketTimeoutException timeoutEx) {
                //Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        this.working = false;
    }

}
