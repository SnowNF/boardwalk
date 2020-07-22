package net.zhuoweizhang.boardwalk;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.text.method.ScrollingMovementMethod;

/**
 * an activity that launches after app exit.
 * Currently used to display ads.
*/

public class PostExitActivity extends Activity {

	private Handler mHandler=null;
	BufferedReader bfr;
	String s;
	
	//private ProgressBar progressBar;
	public static boolean doLaunch = false;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// this is probably a serious abuse of the Android activity lifecycle, but:
		// LauncherActivity sets doLaunch to true when launching PostExitActivity
		// in this mode we chain to MainActivity
		// when MainActivity dies, doLaunch is no longer true in the new JVM created after death
		// so we show an ad instead
		if (doLaunch) {
			launch();
			doLaunch = false;
		} else {
			doLoadLog();
		}
	}

	

	private void launch() {
		startActivityForResult(new Intent(this, MainActivity.class), 1234);
	}

	private void doLoadLog(){
		setContentView(R.layout.postexitactivity);
		Toast.makeText(this,"正在读取日志",Toast.LENGTH_LONG).show();
		
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
		TextView tv =(TextView) findViewById(R.id.postexitactivityTextView);
		tv.setText(s);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
	}
        };

        new Thread(){
            @Override
            public void run() {
                super.run();
                Message msg=new Message();
                
				String Temp = new String("");
		try{
		    bfr = new BufferedReader(new FileReader("/sdcard/potato_output.txt"));
		}catch(Exception e){
			Toast.makeText(PostExitActivity. this,"日志不存在",Toast.LENGTH_LONG).show();
			finish();
		}
		try {
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{}
			while ((Temp = bfr.readLine()) != null)
 {
            s = s+"\n"+Temp;
			}
		} catch (IOException e) {
			Toast.makeText(PostExitActivity. this,"读取失败"+e.toString(),Toast.LENGTH_LONG).show();
			finish();
		}
				mHandler.sendMessage(msg);
            }
        }.start();

    }
}
