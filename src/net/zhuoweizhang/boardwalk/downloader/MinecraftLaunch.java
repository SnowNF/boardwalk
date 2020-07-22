package net.zhuoweizhang.boardwalk.downloader;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import net.zhuoweizhang.boardwalk.*;
import net.zhuoweizhang.boardwalk.model.*;
import net.zhuoweizhang.boardwalk.util.*;

public class MinecraftLaunch {

	public static File launcherDir;
	public static File tmpDir;
	public static File dexDir;
	public static File dexPackDir;

	public static List<String> javaVMCmd;

	public static List<String> libsToRename = Arrays.asList("vecmath");

	public static void init(File launcherDir) {
		MinecraftLaunch.launcherDir = launcherDir;
		File librariesDir = new File(launcherDir, "libraries");
		File overrideLibrariesDir = new File(launcherDir, "override_libraries");
		LibrariesRepository.setLocalLibPath(new File[] {overrideLibrariesDir, librariesDir}, librariesDir);
		dexDir = new File(launcherDir, "dexed_libraries");
		dexPackDir = new File(launcherDir, "dex_pack");
		LibrariesRepository.setLocalDexPath(new File[] {dexDir}, dexDir);
		MinecraftDownloader.versionsDir = new File(launcherDir, "versions");
		tmpDir = new File(launcherDir, "tmp");
		tmpDir.mkdirs();
	}

	public static void main(String[] args) throws Exception {
		init(new File(args[0]));
		System.out.println("Launcher dir: " + launcherDir + " tmpDir: " + tmpDir);
		if (args.length < 2) {
			System.out.println("Needs to specify version");
			return;
		}
		javaVMCmd = Arrays.asList("java", "-server", "-classpath", System.getProperty("java.class.path"));
		MinecraftDownloader.useMavenCentral = true;
		doPreDex(args[1], Arrays.asList("gson", "jinput", "jutils", "lwjgl", "lwjgl_util", "authlib", "realms"));
	}

	public static boolean canUseExistingDexPack(MinecraftVersion version) {
		File versionFile = MinecraftDownloader.getMinecraftVersionFile(version);
		return versionFile.exists();
	}

	
	public static File getDexPackFile(MinecraftVersion version) {
		return new File(dexPackDir, version.id + ".jar");
	}

	private static List<File> getLibsForVersion(MinecraftVersion version) {
		List<File> retval = new ArrayList<File>();
		for (DependentLibrary library: version.libraries) {
			String[] parts = library.name.split(":");
			File localPath = LibrariesRepository.getLocalPath(parts[0], parts[1], parts[2]);
			if (localPath != null) retval.add(localPath);
		}
		return retval;
	}

	public static String getClassPath(MinecraftVersion version) {
		StringBuilder builder = new StringBuilder();
		for (File f: getLibsForVersion(version)) {
			builder.append(f.getAbsolutePath());
			builder.append(":");
		}
		builder.append(MinecraftDownloader.getMinecraftVersionFile(version).getAbsolutePath());
		return builder.toString();
	}


	public static void doPreDex(String versionName, List<String> predexBlacklist) throws Exception {
		MinecraftVersion version = MinecraftDownloader.downloadVersionInfo(versionName);
		for (DependentLibrary library: version.libraries) {
			String[] parts = library.name.split(":");
			if (predexBlacklist.contains(parts[1])) continue;
			if (LibrariesRepository.needsDownload(parts[0], parts[1], parts[2])) {
				try {
					MinecraftDownloader.downloadLibrary(parts[0], parts[1], parts[2]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		for (DependentLibrary library: version.libraries) {
			String[] parts = library.name.split(":");
			if (predexBlacklist.contains(parts[1])) continue;
			File localPath = LibrariesRepository.getLocalPath(parts[0], parts[1], parts[2]);
			if (localPath == null) continue;
			File localDexPath = LibrariesRepository.getDexLocalPath(parts[0], parts[1], parts[2]);
			if (localDexPath != null) {
				System.out.println("Using pre-dexed " + library.name);
				continue;
			}
			File outputPath = LibrariesRepository.getDexTargetPath(parts[0], parts[1], parts[2]);
			runConvert(localPath, outputPath, /*libsToRename.contains(parts[1])*/true, null);
		}
		
	}

	public static void runConvert(File input, File output, boolean rename, String renameRules) throws Exception {
		// run Jarjar
		// run Dex
		if (renameRules == null) renameRules = "jarjarrules.txt";
		output.getParentFile().mkdirs();
		File renamed = null;
		if (rename) renamed = File.createTempFile(input.getName(), "renamed.jar", tmpDir);
		File dexed = File.createTempFile(input.getName(), "dexed.jar", tmpDir);
		try {
			if (rename) {
				runRename(new File(launcherDir, renameRules), input, renamed);
				runDex(Arrays.asList(renamed), dexed);
			} else {
				runDex(Arrays.asList(input), dexed);
			}
			dexed.renameTo(output);
		} finally {
			if (renamed != null) renamed.delete();
			dexed.delete();
		}
	}

	public static void runRename(File rulesFile, File jarjarIn, File jarjarOut) {
		System.out.println("Renaming: " + jarjarIn + " to " + jarjarOut);
		jarjarOut.delete();
		String[] jarjarArgs = {"process", rulesFile.getAbsolutePath(), jarjarIn.getAbsolutePath(), jarjarOut.getAbsolutePath()};
		try {
			//com.tonicsystems.jarjar.Main.main(jarjarArgs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void runDex(List<File> dexInFiles, File dexOut) {
		List<String> dexArgs = Arrays.asList("--num-threads=4", "--no-optimize", "--debug");
		if (!javaVMCmd.get(0).equals("dalvikvm")) dexArgs = Arrays.asList("--num-threads=4", "--debug");
		runDex(dexInFiles, dexOut, dexArgs);
	}

	public static void runDex(List<File> dexInFiles, File dexOut, List<String> extraArgs) {
		System.out.println("Running dex: " + dexInFiles + " out: " + dexOut);
		String[] dexArgs = {"--dex", "--output=" + dexOut.getAbsolutePath()};
		List<String> dexNewArgs = new ArrayList<String>(dexArgs.length + dexInFiles.size());
		dexNewArgs.addAll(Arrays.asList(dexArgs));
		if (extraArgs != null) dexNewArgs.addAll(extraArgs);
		for (File f: dexInFiles) {
			dexNewArgs.add(f.getAbsolutePath());
		}
		try {
			System.gc();
			runExt("net.zhuoweizhang.boardwalk.com.android.dx.command.Main", dexNewArgs);
			//com.android.dx.command.Main.main(dexNewArgs.toArray(dexArgs));
			System.out.println("Done executing dex");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void runExt(String className, List<String> args) throws IOException, InterruptedException {
		List<String> argsNew = new ArrayList<String>();
		argsNew.addAll(javaVMCmd);
		argsNew.add("-Xms128M");
		argsNew.add("-Xmx768M");
		argsNew.add("-Xss256K");
		argsNew.add(className);
		argsNew.addAll(args);
		Process p = new ProcessBuilder(argsNew).redirectErrorStream(true).start();
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		StringBuffer buf = new StringBuffer();
		while((line = in.readLine()) != null) {
			System.out.println(line);
			buf.append(line);
			buf.append('\n');
		}
		int retval = p.waitFor();
		if (retval != 0) {
			throw new RuntimeException("Dex returned " + retval + " with error: \n" + buf);
		}
	}
}
