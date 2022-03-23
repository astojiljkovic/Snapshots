package servent.message;

import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;

/**
 * Has all the fancy stuff from {@link BasicMessage}, with an
 * added vector clock.
 * 
 * Think about the repercussions of invoking <code>changeReceiver</code> or
 * <code>makeMeASender</code> on this without overriding it.
 * @author bmilojkovic
 *
 */
public class Transaction extends BasicMessage {

	private static final long serialVersionUID = 7952273798396080816L;
	private Map<Integer, Integer> senderVectorClock;
	private ServentInfo collector;
	private boolean SEflag;
	private transient BitcakeManager bitcakeManager;
	
	public Transaction(MessageType type, ServentInfo senderInfo, ServentInfo receiverInfo, String messageText,
					   Map<Integer, Integer> senderVectorClock, ServentInfo collector, BitcakeManager bitcakeManager, boolean SEflag, int messageId) {
		super(type, senderInfo, receiverInfo, messageText,messageId);
		
		this.senderVectorClock = senderVectorClock;
		this.collector=collector;
		this.bitcakeManager=bitcakeManager;
		this.SEflag=SEflag;

	}
	public Transaction(MessageType messageType, ServentInfo senderInfo, ServentInfo receiverInfo, String messageText,
					   Map<Integer, Integer> senderVectorClock, ServentInfo sendingBitcakeToInfo, BitcakeManager bitcakeManager, boolean sendEffectFlag) {
		super(messageType, senderInfo, receiverInfo, messageText);

		this.senderVectorClock = senderVectorClock;
		this.collector = sendingBitcakeToInfo;
		this.bitcakeManager = bitcakeManager;
		this.SEflag = sendEffectFlag;
	}


	@Override
	public void sendEffect() {
		int amount = Integer.parseInt(getMessageText());
		bitcakeManager.takeSomeBitcakes(amount);

	}

	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

			Message toReturn = new Transaction(getMessageType(), getOriginalSenderInfo(),
					newReceiverInfo, getMessageText(),getSenderVectorClock(),getCollector(),getBitcakeManager(),false, getMessageId());

			return toReturn;
		} else {
			AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");

			return null;
		}
	}

	public Map<Integer, Integer> getSenderVectorClock() {
		return senderVectorClock;
	}

	public ServentInfo getCollector() {
		return collector;
	}

	public boolean isSEflag() {
		return SEflag;
	}

	public void setSEflag(boolean SEflag) {
		this.SEflag = SEflag;
	}

	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}


}
