package net.zhuoweizhang.boardwalk.util;
import java.io.*;

public class runrootcommand
{
	public static void CloseSELinux(){
		ExecuteThread Selinux = new ExecuteThread("setenforce 0");
		Thread T1 = new Thread(Selinux);
		T1.start();
	}
	public static void HotReboot(){
		ExecuteThread Hotreboot = new ExecuteThread("am restart");
		Thread T2 = new Thread(Hotreboot);
		T2.start();
	}
}

class ExecuteThread implements Runnable
{
	String command;
	ExecuteThread(String command){
		this.command=command;
	}

	@Override
	public void run()
	{
        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        StringBuffer wifiConf = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream
				.writeBytes(command+"\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(
				dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                wifiConf.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
          //  Log.d("shell命令执行结果：",process.exitValue()+"");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
}
