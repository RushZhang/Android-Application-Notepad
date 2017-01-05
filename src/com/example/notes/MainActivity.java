package com.example.notes;

import com.example.notes.db.NotesDB;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	
	private SimpleCursorAdapter adapter=null; //使用数据库的时候通常都是用SimpleCursorAdapter
	private NotesDB db;
	private SQLiteDatabase dbRead, dbWrite;
	public static final int REQUEST_CODE_ADD_NOTE=1;
	public static final int REQUEST_CODE_EDIT_NOTE=2;
	
	
	//=======添加按钮===========
	private OnClickListener btnAddNote_clickHandler=new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			sp.play(soundId, 1, 1, 0, 1, 1);
			startActivityForResult(new Intent(MainActivity.this, AtyEditNote.class), REQUEST_CODE_ADD_NOTE); //这个request_code_add_note是个大于0的东西，还要重写onActivityResult的方法，具体怎么看定义
		}
	};
	
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 switch (requestCode) {
		case REQUEST_CODE_ADD_NOTE:
		case REQUEST_CODE_EDIT_NOTE:
			if(resultCode==Activity.RESULT_OK){
				refreshNotesListView();
			}
			break;

		default:
			break;
		}
		 
	 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getListView().setOnItemLongClickListener(ListViewItemLongClickListener);
		db=new NotesDB(this);
		dbRead=db.getReadableDatabase();
		dbWrite=db.getWritableDatabase();
		adapter=new SimpleCursorAdapter(this, R.layout.notes_list_cell, null, new String[]{NotesDB.COLUMN_NAME_NOTE_NAME, NotesDB.COLUMN_NAME_NOTE_DATE}, new int[]{R.id.tvName, R.id.tvDate});
		setListAdapter(adapter);
		refreshNotesListView(); //onCreate的时候也顺便查询全部
		findViewById(R.id.btnAddNote).setOnClickListener(btnAddNote_clickHandler);
		//==== 声音=======
		sp=new SoundPool(1, AudioManager.STREAM_MUSIC	, 0);
		soundId=sp.load(this, R.raw.a5, 1);
	}
	
	public void refreshNotesListView(){  //Cursor有点类似于JDBC里的resultset
		adapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES, null, null, null, null, null, null));
	}
	
	//＝＝＝＝＝＝重写onListItemClick==============
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		Cursor cursor=adapter.getCursor();
		cursor.moveToPosition(position);
		Intent intent=new Intent(MainActivity.this, AtyEditNote.class);
		intent.putExtra(AtyEditNote.EXTRA_NOTE_ID, cursor.getInt(cursor.getColumnIndex(NotesDB.COLUMN_NAME_ID)));
		intent.putExtra(AtyEditNote.EXTRA_NOTE_Name, cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_NAME)));
		intent.putExtra(AtyEditNote.EXTRA_NOTE_CONTENT, cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_CONTENT)));
		startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE);
		super.onListItemClick(l, v, position, id);
	}
	
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	
	
	
	
	
	
	
	
	
	private OnItemLongClickListener ListViewItemLongClickListener=new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
			// TODO Auto-generated method stub
			new AlertDialog.Builder(MainActivity.this).setTitle("Prompt").setMessage("Sure to delete?").setNegativeButton("Cancel", null).setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Cursor cursor=adapter.getCursor();
					cursor.moveToPosition(position);
					int itemId=cursor.getInt(cursor.getColumnIndex("_id"));
					dbWrite.delete(NotesDB.TABLE_NAME_NOTES, "_id=?", new String[]{itemId+""});
					dbWrite.delete(NotesDB.TABLE_NAME_MEDIA, "_id=?", new String[]{itemId+""});
					Toast.makeText(MainActivity.this, "Deleting succeeded", Toast.LENGTH_LONG).show();
					refreshNotesListView();
				}

			}).show();
			return true; //告诉操作系统有长按
		}
	};
	
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menuItemAbout:
			new AlertDialog.Builder(this).setTitle("About").setMessage("This is my first Android application, together with SQLite, realized the functions of writing logs and saving photos/videos").setPositiveButton("Close", null).show();
			break;
		
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	//==========后退事件============
	private int clickCount=0;
	private long lastClickTime=0;
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
//		super.onBackPressed();
//		if(clickCount++<1) Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
//		else finish();
		if(lastClickTime<=0){
			Toast.makeText(this, "Press again to quit", Toast.LENGTH_LONG).show();
			lastClickTime=System.currentTimeMillis();
		}else{
			long currentClickTime=System.currentTimeMillis();
			if(currentClickTime-lastClickTime<1000){
				finish();
			}else{
				Toast.makeText(this, "Press again to quit", Toast.LENGTH_LONG).show();
				lastClickTime=currentClickTime;
			}
		}	
	}
	
	//==========sound事件============
	private SoundPool sp;  //播短声音
	private int soundId;  
	
}
