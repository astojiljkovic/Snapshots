package cli.command;

import java.math.BigInteger;

import app.AppConfig;
import servent.message.MessageUtil;
import servent.message.SumHelpMessage;

public class SumCommand implements CLICommand {

	private static BigInteger cuurentHelpResult = null;
	
	public static void reportHelpResult(BigInteger helpResult) {
		cuurentHelpResult = helpResult;
	}
	
	@Override
	public String commandName() {
		return "sum";
	}

	@Override
	public void execute(String args) {
		BigInteger number = null;
		try {
			number = new BigInteger(args);
		} catch (NumberFormatException e) {
			System.err.println("Bad number format: " + args);
			return;
		}
		
		BigInteger mid = number.divide(new BigInteger("2"));
		
		int myId = AppConfig.myServentInfo.getId();
		int friendId = (myId % 2 == 0) ? myId + 1 : myId - 1;
		MessageUtil.sendMessage(new SumHelpMessage(
				AppConfig.myServentInfo, AppConfig.getInfoById(friendId), mid));

		BigInteger sum = BigInteger.ZERO;
		
		for (BigInteger val = mid; val.compareTo(number) == -1; val = val.add(BigInteger.ONE) ) {
			sum = sum.add(val);
		}
		
		while (cuurentHelpResult == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Final result: " + sum.add(cuurentHelpResult));
		
		cuurentHelpResult = null;
	}

}
