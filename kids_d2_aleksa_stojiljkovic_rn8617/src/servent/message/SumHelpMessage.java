package servent.message;

import java.math.BigInteger;

import app.ServentInfo;

public class SumHelpMessage extends BasicMessage {

	private static final long serialVersionUID = -4706019432302918783L;

	public SumHelpMessage(ServentInfo senderInfo, ServentInfo receiverInfo, BigInteger rangeMid) {
		super(MessageType.SUM_HELP_MESSAGE, senderInfo, receiverInfo, rangeMid.toString());
	}
}
