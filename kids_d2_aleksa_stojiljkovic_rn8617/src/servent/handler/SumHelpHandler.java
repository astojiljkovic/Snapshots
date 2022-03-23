package servent.handler;

import java.math.BigInteger;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.MessageUtil;
import servent.message.SumResultMessage;

public class SumHelpHandler implements MessageHandler {

	private Message receivedMessage;
	
	public SumHelpHandler(Message message) {
		this.receivedMessage = message;
	}
	
	@Override
	public void run() {
		if (receivedMessage.getMessageType() == MessageType.SUM_HELP_MESSAGE) {
			
			BigInteger sum = BigInteger.ZERO;
			BigInteger mid = new BigInteger(receivedMessage.getMessageText());
			
			for (BigInteger val = BigInteger.ONE; val.compareTo(mid) == -1; val = val.add(BigInteger.ONE) ) {
				sum = sum.add(val);
			}
			
			MessageUtil.sendMessage(new SumResultMessage(
					receivedMessage.getReceiverInfo(), receivedMessage.getOriginalSenderInfo(), sum));
			
		} else {
			AppConfig.timestampedErrorPrint("Wrong handler for sum help. " + receivedMessage);
		}
	}

}
