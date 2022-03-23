package servent.message;

import java.math.BigInteger;

import app.ServentInfo;

public class SumResultMessage extends BasicMessage {

	private static final long serialVersionUID = -1121732534784647779L;

	public SumResultMessage(ServentInfo senderInfo, ServentInfo receiverInfo, BigInteger result) {
		super(MessageType.SUM_RESULT_MESSAGE, senderInfo, receiverInfo, result.toString());
	}
}
