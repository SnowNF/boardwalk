package net.zhuoweizhang.boardwalk.potato;

import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import net.zhuoweizhang.boardwalk.util.*;

import java.lang.Process;

public class ExtractRuntime implements Runnable {
	private Context context;
	private File runtimeDir;
	private String runtimeDirPath;
	private File tempDir;
	private String libDir="/data/data/net.zhuoweizhang.boardwalk/lib/";
	public ExtractRuntime(Context context) {
		this.context = context;
		this.runtimeDir = context.getDir("runtime", 0);
		this.runtimeDirPath=runtimeDir.getAbsolutePath();
		this.tempDir = new File(runtimeDirPath+"/temp");
	}

	private void extractAsset(String name) throws IOException {
		File outf = new File(runtimeDir, name);
		AssetsUtil.extractFileFromAssets(context, name, outf);
		outf.setExecutable(true);
	}

	public void run() {
		try {
			String[] link = new String[]{"ln","-s",libDir+"libbusybox.so",runtimeDirPath+"/busybox"};
			doExec(link);
			//extractAsset("busybox");
			extractTar("openjdk_8u265b01.tgz", new File(runtimeDirPath).getAbsolutePath());
			extractTar("glibc.tgz", new File(runtimeDirPath).getAbsolutePath());
			extractAsset("libboardwalk_preload.so");
			extractAsset("liblwjgl.so");
			extractAsset("libGLESv1_CM.so");
			extractAsset("libglshim.so");
			extractAsset("libgcc_s.so.1");
			extractAsset("lwjgl.jar");
			extractAsset("lwjgl_util.jar");
			extractAsset("librarylwjglopenal-20100824.jar");
				Looper.prepare();
			Toast.makeText(context,"extra runtime successfully",Toast.LENGTH_LONG).show();
				Looper.loop();
		} catch (Exception e) {
			e.printStackTrace();
				Looper.prepare();
			Toast.makeText(context,"fail to extra runtime: "+e.toString()+"\n please try to use root.",Toast.LENGTH_LONG).show();
				Looper.loop();
		}
	}


	public void extractTar(String tar, String out) throws Exception {
		tempDir.mkdirs();
		File tempOut = new File(tempDir, tar);
		tempOut.delete();
		AssetsUtil.extractFileFromAssets(context, tar, tempOut);
		File outFile = new File(out);
		outFile.mkdirs();
		String[] argsNew = new String[]{new File(runtimeDir, "busybox").getAbsolutePath(), "tar", "-xvf",
			tempOut.getAbsolutePath(), "-C", out};
		
		doExec(argsNew);
		tempOut.delete();
	}

	public static void doExec(String[] argsNew) throws Exception {
		Process p = new ProcessBuilder(argsNew).redirectErrorStream(true).start();
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		StringBuffer buf = new StringBuffer();
		while((line = in.readLine()) != null) {
			System.out.println(line);
			buf.append(line);
			buf.append('\n');
		}
		p.waitFor();
	}
}
