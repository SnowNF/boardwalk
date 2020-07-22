package net.zhuoweizhang.boardwalk;

import android.content.Context;
import android.opengl.*;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.AttributeSet;
import android.widget.Toast;

public class BoardwalkGLSurfaceView extends GLSurfaceView {
	Context context;
	/**
	 * Standard View constructor. In order to render something, you
	 * must call {@link #setRenderer} to register a renderer.
	 */
	public BoardwalkGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		Toast.makeText(context,"GLSurfaceView的构造函数，\n"+context.toString()+"\n"+attrs.toString(),Toast.LENGTH_LONG).show();
	}


	public void surfaceDestroyed(SurfaceHolder holder) {
		MainActivity.isGLdestroyed=true;
		Toast.makeText(context,"GLSurfaceView炸了！",Toast.LENGTH_LONG).show();
	}
}
