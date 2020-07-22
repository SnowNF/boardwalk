package net.zhuoweizhang.boardwalk.util;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.*;
import java.io.File;

/**
 * Created by 周开平 on 2017/4/2 22:43.
 * qq 275557625@qq.com
 * 作用：解决Android 6.0以上系统的权限问题
 */

public class PermissionUtils {

    private static String[] PERMISSIONS_STORAGE = {
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static void GrantExternalRW(Activity activity) {
		File f = new File("/sdcard/");
		if(f.canWrite()){
			
		}
		else{
			Toast.makeText(activity,"无存储权限，请给予存储权限",Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int storagePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(PERMISSIONS_STORAGE, 1);
                //return;
            }
        }
        //Toast.makeText(activity,"获取存储权限成功",Toast.LENGTH_LONG).show();
    }}
}
