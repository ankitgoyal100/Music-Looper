package com.goyalgadgets.musiclooper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.tjeannin.apprate.AppRate;

public class MusicLooperActivity extends ListActivity {

	private Context context;
	public static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	final String MEDIA_PATH = new String("/sdcard/");
	public static MediaPlayer mediaPlayer = new MediaPlayer();
	private Button selectFile;
	Cursor musiccursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.main);

		new AppRate(this).setShowIfAppHasCrashed(false).setMinLaunchesUntilPrompt(3).init();

		System.gc();
		String[] proj = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Video.Media.SIZE };
		musiccursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
		for (int i = 0; i < musiccursor.getCount(); i++) {
			musiccursor.moveToPosition(i);
			String filename = musiccursor.getString(musiccursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

			if (!filename.contains(".aac")) {
				HashMap<String, String> song = new HashMap<String, String>();
				song.put("songTitle", musiccursor.getString(musiccursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
				song.put("songPath", filename);
				if (!songsList.contains(song))
					songsList.add(song);
			}
		}
		//
		// File home = new File(MEDIA_PATH);
		// if (home.listFiles(new FileExtensionFilter()).length > 0) {
		// for (File file : home.listFiles(new FileExtensionFilter())) {
		// HashMap<String, String> song = new HashMap<String, String>();
		// MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		// mmr.setDataSource(file.getPath());
		// String titleOfSong =
		// mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		// song.put("songTitle", titleOfSong);
		// song.put("songPath", file.getPath());
		// if (!songsList.contains(song))
		// songsList.add(song);
		// }
		// }

		ListAdapter adapter = new MusicLooperAdapter(this, songsList);
		setListAdapter(adapter);

		selectFile = (Button) findViewById(R.id.button);
		selectFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent myintent = new Intent(MusicLooperActivity.this, FileDialog.class);
				startActivityForResult(myintent, 0);
			}

		});
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		HashMap<String, String> selection = new HashMap<String, String>();
		selection = songsList.get(position);

		Intent intent = new Intent(context, SongPlayer.class);
		intent.putExtra("position", position);
		intent.putExtra("title", selection.get("songTitle"));
		intent.putExtra("path", selection.get("songPath"));
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null || data.getExtras() == null)
			return;
		String path = data.getExtras().getString(FileDialog.RESULT_PATH);
		MediaPlayer temp = MediaPlayer.create(MusicLooperActivity.this, Uri.parse(path));
		if (temp == null) {
			Toast.makeText(this, "Must select a valid media file!", Toast.LENGTH_LONG).show();
			return;
		}
		if (data == null || data.getExtras() == null || FileDialog.RESULT_PATH == null)
			return;
		Intent intent = new Intent(context, SongPlayer.class);
		intent.putExtra("title", path.substring(path.lastIndexOf("/") + 1));
		intent.putExtra("path", path);
		startActivity(intent);
	}

	class FileExtensionFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp3") || name.endsWith(".MP3") || name.endsWith(".aac") || name.endsWith(".m4a") || name.endsWith(".ogg"));
		}
	}
}
