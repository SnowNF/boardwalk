package net.zhuoweizhang.boardwalk.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/* Must not have Android dependencies. Reflection is OK.
 * Stuff that links directly to Android stuff goes in DroidUtils
 */
public class PlatformUtils {


	/** @return number of logical CPUs on this device */
	public static int getNumCores() {
		// http://stackoverflow.com/questions/10133570/availableprocessors-returns-1-for-dualcore-phones

		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				return Pattern.matches("cpu[0-9]+", pathname.getName());
			}
		};

		try {
			File dir = new File("/sys/devices/system/cpu/");
			return dir.listFiles(filter).length;
		} catch (Exception e) {
			return Math.max(1, Runtime.getRuntime().availableProcessors());
		}
	}

}
