package csvreader;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask{

	private Main main;
    public MyTimerTask(Main main) {
    	this.main = main;
    }

    @Override
    public void run() {
    	(main).Update();
    }
}
