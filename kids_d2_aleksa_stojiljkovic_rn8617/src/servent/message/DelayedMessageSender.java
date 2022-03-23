package servent.message;

import app.AppConfig;
import app.ServentInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This worker sends a message asynchronously. Doing this in a separate thread
 * has the added benefit of being able to delay without blocking main or somesuch.
 *
 * @author bmilojkovic
 */
public class DelayedMessageSender implements Runnable {

    private Message messageToSend;
    private AtomicInteger brojac = new AtomicInteger(0);

    public DelayedMessageSender(Message messageToSend) {
        this.messageToSend = messageToSend;
    }

    public void run() {
        /*
         * A random sleep before sending.
         * It is important to take regular naps for health reasons.
         */
        try {
            Thread.sleep((long) (Math.random() * 1000) + 500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        ServentInfo receiverInfo = messageToSend.getReceiverInfo();

        if (MessageUtil.MESSAGE_UTIL_PRINTING) {
            AppConfig.timestampedStandardPrint("Sending message " + messageToSend);
        }

        while(true) {
			try {
				/*
				 * Similar sync block to the one in FifoSenderWorker, except this one is
				 * related to Lai-Yang. We want to be sure that message color is red if we
				 * are red. Just setting the attribute when we were making the message may
				 * have been to early.
				 * All messages that declare their own stuff (eg. LYTellMessage) will have
				 * to override setRedColor() because of this.
				 */


				Socket sendSocket = new Socket(receiverInfo.getIpAddress(), receiverInfo.getListenerPort());
				if (messageToSend.getMessageType() == MessageType.TRANSACTION) {
					Transaction msg = (Transaction) messageToSend;
					if (msg.isSEflag()) {
						msg.sendEffect();
						msg.setSEflag(false);
					}
				}
				ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
//				AppConfig.timestampedStandardPrint("PRE 64");
				oos.writeObject(messageToSend);
//				AppConfig.timestampedStandardPrint("POSLE 64");
				oos.flush();

				sendSocket.close();

				break;
//				treba napraviti sendEffect kao sto je u LY za poruku
//            messageToSend.sendEffect();

			} catch (IOException e) {
				brojac.getAndAdd(1);
				if(brojac.get() >= 10 ){
					break;
				}
				AppConfig.timestampedErrorPrint("Couldn't send message: " + messageToSend.toString());

			}
		}
    }

}
