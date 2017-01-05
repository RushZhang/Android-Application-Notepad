package com.example.notes;

import android.app.Activity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class AtyVideoViewer extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		videoView=new VideoView(this);
		videoView.setMediaController(new MediaController(this));
		setContentView(videoView);
		String path=getIntent().getStringExtra(EXTRA_PATH);
		if(path!=null){
			videoView.setVideoPath(path);
		}else{
			finish();
		}
		
	}
	
	private VideoView videoView;
	
	public static final String EXTRA_PATH="path";
}
