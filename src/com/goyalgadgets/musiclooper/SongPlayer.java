package com.goyalgadgets.musiclooper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.simonvt.numberpicker.NumberPicker;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SongPlayer extends Activity implements NumberPicker.OnValueChangeListener {

	private int position;
	private String path;
	private String title;
	private Context context = this;
	private ImageButton button;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private SeekBar seekBar;
	private final Handler handler = new Handler();
	private TextView songTitleLabel;
	private TextView songArtistLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	private Utilities utils;
	private Bitmap bmp;
	private boolean paused = false;
	private String time;
	private NumberPicker np1;
	private NumberPicker np2;
	private NumberPicker np3;
	private NumberPicker np4;
	private TextView songPos;
	private boolean havePlayed = false;
	private ImageView image;
	private boolean displayed = false;
	private boolean exited = false;
	private Button gapTime;
	private int gap = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		// getActionBar().setDisplayHomeAsUpEnabled(true);

		title = getIntent().getStringExtra("title");
		path = getIntent().getStringExtra("path");
		position = getIntent().getIntExtra("position", -1);
		image = (ImageView) findViewById(R.id.list_image);

		np1 = (NumberPicker) findViewById(R.id.numberPicker1);
		np2 = (NumberPicker) findViewById(R.id.numberPicker2);
		np3 = (NumberPicker) findViewById(R.id.numberPicker3);
		np4 = (NumberPicker) findViewById(R.id.numberPicker4);

		songPos = (TextView) findViewById(R.id.songPos);

		utils = new Utilities();
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(path);
		String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		String titleOfSong = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		byte[] cover = mmr.getEmbeddedPicture();
		if (cover != null)
			bmp = BitmapFactory.decodeByteArray(cover, 0, cover.length);
		else
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.adele);
		image.setImageBitmap(bmp);

		button = (ImageButton) findViewById(R.id.btnPlay);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		gapTime = (Button) findViewById(R.id.button);

		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songArtistLabel = (TextView) findViewById(R.id.artist);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		if (titleOfSong != null)
			songTitleLabel.setText(titleOfSong);
		else
			songTitleLabel.setText(title);
		songArtistLabel.setText(artist);

		try {
			File file = new File(path);
			FileInputStream inputStream = new FileInputStream(file);
			MusicLooperActivity.mediaPlayer.reset();
			MusicLooperActivity.mediaPlayer.setDataSource(inputStream.getFD());
			inputStream.close();
			MusicLooperActivity.mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// MusicLooperActivity.mediaPlayer.start();

		time = utils.milliSecondsToTimer(MusicLooperActivity.mediaPlayer.getDuration());

		np1.setMaxValue(Integer.parseInt(time.substring(0, time.indexOf(":"))));
		np1.setMinValue(0);
		np1.setFocusable(true);
		np1.setFocusableInTouchMode(true);

		if (Integer.parseInt(time.substring(0, time.indexOf(":"))) == 0) {
			np2.setMaxValue(Integer.parseInt(time.substring(time.indexOf(":") + 1)));
		} else {
			np2.setMaxValue(59);
		}
		np2.setMinValue(0);
		np2.setFocusable(true);
		np2.setFocusableInTouchMode(true);
		np2.setFormatter(NumberPicker.getTwoDigitFormatter());

		np3.setMaxValue(Integer.parseInt(time.substring(0, time.indexOf(":"))));
		np3.setMinValue(0);
		np3.setFocusable(true);
		np3.setFocusableInTouchMode(true);
		np3.setValue(np3.getMaxValue());

		np4.setMaxValue(Integer.parseInt(time.substring(time.indexOf(":") + 1)));
		np4.setMinValue(0);
		np4.setFocusable(true);
		np4.setFocusableInTouchMode(true);
		np4.setFormatter(NumberPicker.getTwoDigitFormatter());
		np4.setValue(np4.getMaxValue());

		np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (newVal < np1.getMaxValue())
					np2.setMaxValue(59);
				else if (newVal == np1.getMaxValue())
					np2.setMaxValue(Integer.parseInt(time.substring(time.indexOf(":") + 1)));
			}
		});

		np3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (newVal < np3.getMaxValue())
					np4.setMaxValue(59);
				else if (newVal == np3.getMaxValue())
					np4.setMaxValue(Integer.parseInt(time.substring(time.indexOf(":") + 1)));
			}
		});

		songTotalDurationLabel.setText(utils.milliSecondsToTimer(MusicLooperActivity.mediaPlayer.getDuration()));
		seekBar = (SeekBar) findViewById(R.id.songProgressBar);
		seekBar.setMax((np3.getValue() * 60 * 1000 + np4.getValue() * 1000) - (np1.getValue() * 60 * 1000 + np2.getValue() * 1000));
		startPlayProgressUpdater();

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if (MusicLooperActivity.mediaPlayer != null) {
					MusicLooperActivity.mediaPlayer.seekTo(seekBar.getProgress());
					songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(MusicLooperActivity.mediaPlayer.getCurrentPosition()));
				}

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if (MusicLooperActivity.mediaPlayer != null) {
					MusicLooperActivity.mediaPlayer.seekTo(seekBar.getProgress());
					songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(MusicLooperActivity.mediaPlayer.getCurrentPosition()));
				}
			}

		});

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				havePlayed = true;
				if ((np1.getValue() * 60 * 1000 + np2.getValue() * 1000) >= (np3.getValue() * 60 * 1000 + np4.getValue() * 1000)) {
					Toast.makeText(context, "Start time must be greater than end time!", Toast.LENGTH_LONG).show();
					return;
				}
				displayed = false;
				// check for already playing
				if (MusicLooperActivity.mediaPlayer.isPlaying()) {
					if (MusicLooperActivity.mediaPlayer != null) {
						MusicLooperActivity.mediaPlayer.pause();
						// Changing button image to play button
						button.setImageResource(R.drawable.btn_play);
						paused = true;
					}
				} else {
					// Resume song
					if (MusicLooperActivity.mediaPlayer != null) {
						if (paused) {
							MusicLooperActivity.mediaPlayer.start();
							button.setImageResource(R.drawable.btn_pause);
						} else {
							MusicLooperActivity.mediaPlayer.seekTo(np1.getValue() * 60 * 1000 + np2.getValue() * 1000);
							MusicLooperActivity.mediaPlayer.start();
							button.setImageResource(R.drawable.btn_pause);
						}
					}
				}

			}
		});

		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ((np3.getValue() * 60 * 1000 + np4.getValue() * 1000) >= (MusicLooperActivity.mediaPlayer.getCurrentPosition() + 3000)) {
					MusicLooperActivity.mediaPlayer.seekTo(MusicLooperActivity.mediaPlayer.getCurrentPosition() + 3000);
				} else {
					MusicLooperActivity.mediaPlayer.seekTo(np3.getValue() * 60 * 1000 + np4.getValue() * 1000);
				}
				songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(MusicLooperActivity.mediaPlayer.getCurrentPosition()));
			}

		});

		btnPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ((np1.getValue() * 60 * 1000 + np2.getValue() * 1000) <= (MusicLooperActivity.mediaPlayer.getCurrentPosition() - 3000)) {
					MusicLooperActivity.mediaPlayer.seekTo(MusicLooperActivity.mediaPlayer.getCurrentPosition() - 3000);
				} else {
					MusicLooperActivity.mediaPlayer.seekTo(np1.getValue() * 60 * 1000 + np2.getValue() * 1000);
				}
				songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(MusicLooperActivity.mediaPlayer.getCurrentPosition()));
			}

		});

		gapTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				show();
			}

		});

	}

	public void startPlayProgressUpdater() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (!displayed)
								if ((np1.getValue() * 60 * 1000 + np2.getValue() * 1000) >= (np3.getValue() * 60 * 1000 + np4.getValue() * 1000)) {
									Toast.makeText(context, "Start time must be greater than end time!", Toast.LENGTH_LONG).show();
									MusicLooperActivity.mediaPlayer.pause();
									// Changing button image to play button
									button.setImageResource(R.drawable.btn_play);
									paused = true;
									displayed = true;
								}
							songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(MusicLooperActivity.mediaPlayer.getCurrentPosition()));
							seekBar.setProgress(MusicLooperActivity.mediaPlayer.getCurrentPosition());
							if (!displayed && MusicLooperActivity.mediaPlayer.isPlaying()) {
								if ((np3.getValue() * 60 * 1000 + np4.getValue() * 1000) <= MusicLooperActivity.mediaPlayer.getCurrentPosition()) {
									loop();
								}
								if ((np1.getValue() * 60 * 1000 + np2.getValue() * 1000) >= MusicLooperActivity.mediaPlayer.getCurrentPosition()) {
									MusicLooperActivity.mediaPlayer.seekTo((np1.getValue() * 60 * 1000 + np2.getValue() * 1000));
								}
							}
						}

					});
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (exited) {
						return;
					}
				}
			}
		}).start();
	}

	private void loop() {
		MusicLooperActivity.mediaPlayer.pause();
		songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer((np3.getValue() * 60 * 1000 + np4.getValue() * 1000)));
		try {
			Thread.sleep(gap * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MusicLooperActivity.mediaPlayer.seekTo((np1.getValue() * 60 * 1000 + np2.getValue() * 1000));
		MusicLooperActivity.mediaPlayer.start();
		startPlayProgressUpdater();
	}

	@Override
	public void onBackPressed() {
		if (havePlayed) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you stop looping the current song?").setCancelable(false)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							MusicLooperActivity.mediaPlayer.pause();
							// Changing button image to play button
							button.setImageResource(R.drawable.btn_play);
							paused = false;
							exited = true;
							Intent intent = new Intent(SongPlayer.this, MusicLooperActivity.class);
							startActivity(intent);
							finish();
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).show();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

		Log.i("value is", "" + newVal);

	}

	public void show() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Get the layout inflater
		LayoutInflater inflater = this.getLayoutInflater();

		View view = inflater.inflate(R.layout.dialog, null);
		final NumberPicker np = (NumberPicker) view.findViewById(R.id.gapPicker);
		np.setMaxValue(1000);
		np.setMinValue(0);
		np.setFocusable(true);
		np.setFocusableInTouchMode(true);
		np.setValue(gap);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view).setTitle("Enter Gap Time")
		// Add action buttons
				.setPositiveButton("Set time", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						gap = np.getValue();
						System.out.println(gap);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();

	}
}
// final Dialog dialog = new Dialog(context);
// dialog.setContentView(R.layout.custom_dialog);
// dialog.setTitle("Music Looper Information");
//
// // set the custom dialog components - text, image and button
// TextView text = (TextView) dialog.findViewById(R.id.text);
// text.setText("Hello! In order to use this application, you must select a song from your phone by clicking the  'Select File' button. Once you have selected your song, be sure to enter a start time and end time and then click the 'Play' button! You can also enter in a gap time, which is the time in between loops. If you do not specify a gap time, it will automatically be set to half a second. If you want to change anything after you click the 'Play' button, you must press the 'Reset' button located in the menu. If you want to exit the application, press the 'Back' button and if you want to play the music in the background while you are doing other things, press the 'Home' button. Thank you and be sure to leave a 5-Star review!");
// ImageView image = (ImageView) dialog.findViewById(R.id.image);
// image.setImageResource(android.R.drawable.ic_menu_info_details);
// dialog.show();
// return true;