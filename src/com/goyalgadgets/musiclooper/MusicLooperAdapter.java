package com.goyalgadgets.musiclooper;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicLooperAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private Handler handler;
	private static LayoutInflater inflater = null;
	private Bitmap bmp;

	public MusicLooperAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		this.activity = a;
		data = d;
		handler = new Handler();
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, ViewGroup parent) {

		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.playlist_item, null);
		TextView title = (TextView) vi.findViewById(R.id.songTitle);
		TextView artist = (TextView) vi.findViewById(R.id.artist);
		ImageView image = (ImageView) vi.findViewById(R.id.list_image);

		String path = MusicLooperActivity.songsList.get(position).get("songPath");
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		System.out.println(path);
		try {
			mmr.setDataSource(path);
		} catch (IllegalArgumentException e) {
			System.out.println("Exeption");
		}
		String MP3artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		String MP3title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

		if (mmr.getEmbeddedPicture() != null)
			bmp = BitmapFactory.decodeByteArray(mmr.getEmbeddedPicture(), 0, mmr.getEmbeddedPicture().length);
		else
			bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.adele);
		image.setImageBitmap(bmp);
		
		artist.setText(MP3artist);
		if (MP3title != null)
			title.setText(MP3title);
		else
			title.setText(MusicLooperActivity.songsList.get(position).get("songTitle"));
		return vi;

	}
}