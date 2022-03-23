package cli.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.BitcakeManager;
import servent.message.Transaction;
import servent.message.MessageType;
import servent.message.MessageUtil;

public class TransactionCommand implements CLICommand {

	private static final int TRANSACTION_COUNT = 5;
	private static final int BURST_WORKERS = 10;
	private static final int MAX_TRANSFER_AMOUNT = 10;

	private BitcakeManager bitcakeManager;

	public TransactionCommand(BitcakeManager bitcakeManager) {
		this.bitcakeManager = bitcakeManager;
	}

	private class TransactionBurstWorker implements Runnable{

		@Override
		public void run() {
			for (int i = 0; i < TRANSACTION_COUNT; i++) {
				Map<Integer,Integer> mojClock = CausalBroadcastShared.getVectorClock();
				Map<Integer,Integer> kopijaClocka = new ConcurrentHashMap<>();
				kopijaClocka.putAll(mojClock);

				int amount = 1 + (int)(Math.random() * MAX_TRANSFER_AMOUNT);


				for (Integer sused :
						AppConfig.myServentInfo.getNeighbors()) {
					Transaction broadcastMessage = new Transaction(MessageType.TRANSACTION,AppConfig.myServentInfo,AppConfig.getInfoById(sused),
							Integer.toString(amount),kopijaClocka,AppConfig.getInfoById(sused),bitcakeManager,true);

					MessageUtil.sendMessage(broadcastMessage);

				}

			}
		}
	}


	@Override
	public String commandName() {
		return "transaction_burst";
	}

	@Override
	public void execute(String args) {
		for (int i = 0; i < BURST_WORKERS; i++) {
			Thread t = new Thread(new TransactionBurstWorker());

			t.start();
		}
	}

}
