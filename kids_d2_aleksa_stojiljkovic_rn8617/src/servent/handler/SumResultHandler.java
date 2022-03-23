package servent.handler;

import java.math.BigInteger;

import app.AppConfig;
import cli.command.SumCommand;
import servent.message.Message;
import servent.message.MessageType;

public class SumResultHandler implements MessageHandler {

	private Message receivedMessage;
	
	public SumResultHandler(Message message) {
		this.receivedMessage = message;
	}
	
	@Override
	public void run() {
		if (receivedMessage.getMessageType() == MessageType.SUM_RESULT_MESSAGE) {
			BigInteger helpResult = new BigInteger(receivedMessage.getMessageText());
			SumCommand.reportHelpResult(helpResult);
		} else {
			AppConfig.timestampedErrorPrint("Wrong handler for sum result. " + receivedMessage);
		}

	}

}
