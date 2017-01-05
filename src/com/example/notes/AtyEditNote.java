package com.example.notes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.notes.db.NotesDB;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AtyEditNote extends ListActivity {
	
	private int noteId=-1; //－1表示是添加操作，其他的代表是修改原有的note
	public static final  String EXTRA_NOTE_ID="noteId";
	public static final  String EXTRA_NOTE_Name="noteName";
	public static final  String EXTRA_NOTE_CONTENT="noteContent";
	public static final int REQUEST_CODE_GET_PHOTO=1;
	public static final int REQUEST_CODE_GET_VIEDO=2;
	private EditText etName, etContent;
	private MediaAdapter adapter;
	private NotesDB db;
	private SQLiteDatabase dbRead, dbWrite;
	private String currentPath=null;
	private NotificationManager nm;
	private SoundPool sp;  //播短声音
	private int soundId;  
	
	
	private OnClickListener btnClickHandler=new OnClickListener() {
		Intent i;
		File file;
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			sp.play(soundId, 1, 1, 0, 1, 1);
			switch (v.getId()) {
			case R.id.btnAddPhoto:
				i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				file=new File(getMediaDir(), "RUSH_"+System.currentTimeMillis()+".jpg");
				if(!file.exists()){
					try {
						file.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				currentPath=file.getAbsolutePath();
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				startActivityForResult(i, REQUEST_CODE_GET_PHOTO);
				break;
			case R.id.btnAddVideo:
				i=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				file=new File(getMediaDir(), "RUSH_"+System.currentTimeMillis()+".mp4");
				if(!file.exists()){
					try {
						file.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				currentPath=file.getAbsolutePath();
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));		
				startActivityForResult(i, REQUEST_CODE_GET_VIEDO);
				break;
			case R.id.btnSave:  //保存分为两步骤，第一步是保存日志，第二步保存媒体
				saveMedia(saveNote());
				setResult(RESULT_OK);
				Notification n=new Notification(R.drawable.q2, "New log saved", System.currentTimeMillis());
				n.setLatestEventInfo(AtyEditNote.this, "New log saved", "Click to write new log", PendingIntent.getActivity(AtyEditNote.this, 1, getIntent(), 0));
				nm.notify(R.layout.aty_edit_note, n);
				Toast.makeText(AtyEditNote.this, "Saving succeeded", Toast.LENGTH_LONG).show();
				Intent back=new Intent(AtyEditNote.this, MainActivity.class);
				startActivity(back);
				break;
			case R.id.btnCancel:
				Intent ii=new Intent(AtyEditNote.this, MainActivity.class);
				startActivity(ii);
				setResult(RESULT_CANCELED);  //自带的
				finish();
				break;

			default:
				break;
			}
		}
	};
	
	protected void onListItemClick(android.widget.ListView l, View v, int position, long id) {
		MediaListCellData data=adapter.getItem(position);
		Intent i;
		switch (data.type) {
		case MediaType.PHOTO:
			i=new Intent(this, AtyPhotoViewer.class);
			i.putExtra(AtyPhotoViewer.EXTRA_PATH, data.path);	
			startActivity(i);
			break;
		case MediaType.VIDEO:
			i=new Intent(this, AtyVideoViewer.class);
			i.putExtra(AtyVideoViewer.EXTRA_PATH, data.path);
			startActivity(i);
			break;
		default:
			break;
		}
		super.onListItemClick(l, v, position, id);
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_edit_note);
		
		//=================声音=================
		sp=new SoundPool(1, AudioManager.STREAM_MUSIC	, 0);
		soundId=sp.load(this, R.raw.a5, 1);
		
		nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE); //固定搭配
		nm.cancel(R.layout.aty_edit_note); //点击取消通知
		db=new NotesDB(this);
		dbRead=db.getReadableDatabase(); //db有开也有关，在destory的时候要把它关掉
		dbWrite=db.getWritableDatabase();
		
		adapter=new MediaAdapter(this);
		setListAdapter(adapter);
		
		noteId=getIntent().getIntExtra(EXTRA_NOTE_ID, -1); 
		etName=(EditText) findViewById(R.id.etName);
		etContent=(EditText) findViewById(R.id.etContent);
		//上面的-1是指没有找到数据时返回的默认值。
		//Returns: the value of an item that previously added with putExtra() or the default value if none was found.
		
		if(noteId>-1){  //代表是在修改当前日志
			etName.setText(getIntent().getStringExtra(EXTRA_NOTE_Name));
			etContent.setText(getIntent().getStringExtra(EXTRA_NOTE_CONTENT));
			Cursor cursor=dbRead.query(NotesDB.TABLE_NAME_MEDIA, null, NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID+"=?", new String[]{noteId+""}, null, null, null);
			while(cursor.moveToNext()){
				adapter.add(new MediaListCellData(cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_MEDIA_PATH)), cursor.getInt(cursor.getColumnIndex(NotesDB.COLUMN_NAME_ID))));
			}
			adapter.notifyDataSetChanged();  //因为adapter中的内容变化了
		}else{  //新建日志
			//do nothing
		}
		
		//======================================================================
		findViewById(R.id.btnSave).setOnClickListener(btnClickHandler);
		findViewById(R.id.btnCancel).setOnClickListener(btnClickHandler);
		findViewById(R.id.btnAddPhoto).setOnClickListener(btnClickHandler);
		findViewById(R.id.btnAddVideo).setOnClickListener(btnClickHandler);	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case REQUEST_CODE_GET_PHOTO:
		case REQUEST_CODE_GET_VIEDO:
			if(resultCode==RESULT_OK){
				adapter.add(new MediaListCellData(currentPath));
				adapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public File getMediaDir(){
		
		File dir=new File(Environment.getExternalStorageDirectory(), "NotesMedia");
		if(!dir.exists()){
			dir.mkdir();
		}
		return dir;
	}
	
	public void saveMedia(int noteId){
		MediaListCellData data;
		ContentValues cv;
		for(int i=0; i<adapter.getCount(); i++){
			data=adapter.getItem(i);
			if(data.id<=-1){  //新媒体
				cv=new ContentValues();
				cv.put(NotesDB.COLUMN_NAME_MEDIA_PATH, data.path);
				cv.put(NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID, noteId);
				dbWrite.insert(NotesDB.TABLE_NAME_MEDIA, null, cv);
			}
		}
	}
	
	public int saveNote(){  //返回note的id
		ContentValues cv=new ContentValues();  //先把数据写到cv,再用dbWrite包装cv
		System.out.println(etName.getText().toString());
		if(etName.getText().equals("")){
			cv.put(NotesDB.COLUMN_NAME_NOTE_NAME, "***No Title***");
		}else{
			cv.put(NotesDB.COLUMN_NAME_NOTE_NAME, etName.getText().toString());
		}
		cv.put(NotesDB.COLUMN_NAME_NOTE_CONTENT, etContent.getText().toString());
		cv.put(NotesDB.COLUMN_NAME_NOTE_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
		if (noteId>-1) {
			dbWrite.update(NotesDB.TABLE_NAME_NOTES, cv, NotesDB.COLUMN_NAME_ID+"=?", new String[]{noteId+""});
			return noteId;
		}else{  //dbWrite.insert()会返回插入的rowId
			return (int) dbWrite.insert(NotesDB.TABLE_NAME_NOTES, null, cv);
		}
		
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		dbRead.close();
		dbWrite.close();
		super.onDestroy();
	}
	
	
	static class MediaAdapter extends BaseAdapter{  //baseadapter适合那种需要自定义布局(比如包含了icon)的那种
		
		//======这个是自己写的==========
		public void add(MediaListCellData data){
			list.add(data);
		}
		
		
		public MediaAdapter(Context context){
			this.context=context;
		}
		private Context context;
		private List<MediaListCellData> list=new ArrayList<AtyEditNote.MediaListCellData>();
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public MediaListCellData getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView==null){
				convertView=LayoutInflater.from(context).inflate(R.layout.media_list_cell, null);
			}
			MediaListCellData data=getItem(position);
			ImageView ivIcon=(ImageView) convertView.findViewById(R.id.ivIcon);
			TextView tvPath=(TextView) convertView.findViewById(R.id.tvPath);
			ivIcon.setImageResource(data.iconId);
			tvPath.setText(data.path);
			
			return convertView;
		}
		
	}
	
	
	
	//静态内部类，当外部类需要使用内部类，而内部类无需外部类资源，并且内部类可以单独创建的时候会考虑采用静态内部类
	static class MediaListCellData{  //这个类相当于BaseAdapter中的一个基本类型
		
		int type=0;
		int id=-1;
		String path="";
		int iconId=R.drawable.ic_launcher;
		
		public MediaListCellData(String path, int id){
			this(path);
			this.id=id;
		}
		public MediaListCellData(String path){
			this.path=path;
			if(path.endsWith(".jpg")||path.endsWith(".png")){
				iconId=R.drawable.icon_photo;
				type=MediaType.PHOTO;
			}else if (path.endsWith(".mp4")) {
				iconId=R.drawable.icon_video;
				type=MediaType.VIDEO;
			}
		}
	}
	
	static class MediaType{
		static final int PHOTO=1;
		static final int VIDEO=2;
	}
	
}
