/////////////////////////////////////////////////////////////////////////////
///
/// Example Android Application/Activity that allows processing WAV 
/// audio files with SoundTouch library
///
/// Copyright (c) Olli Parviainen
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: SoundTouch.java 210 2015-05-14 20:03:56Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////


package net.surina;

import java.io.File;

import net.surina.soundtouch.SoundTouch;
import net.surina.soundtouchexample.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.text.TextWatcher;
import android.text.Editable;

import java.io.IOException;
import android.media.MediaPlayer;

public class ExampleActivity extends Activity implements OnClickListener 
{
	private TextView textViewConsole = null;
	private EditText editSourceFile = null;
	private EditText editOutputFile = null;
	private EditText editTempo = null;
	private EditText editPitch = null;
	private EditText editSpeed = null;
	private SeekBar  mSeekBar1 = null;
	private SeekBar  mSeekBar2 = null;
	private SeekBar  mSeekBar3 = null;
	private Button   mBtnPlaySource = null;
	private Button   mBtnProcPlay = null;
	private Button   mBtnProcess = null;
	
	StringBuilder consoleText = new StringBuilder();

	// MediaPlayer
	MediaPlayer mPlayer = null;

	// 是否播放
	private boolean mPlaying = false;
	
	/// Called when the activity is created
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_example);
		
		textViewConsole = (TextView)findViewById(R.id.textViewResult);
		editSourceFile = (EditText)findViewById(R.id.editTextSrcFileName);
		editOutputFile = (EditText)findViewById(R.id.editTextOutFileName);

		editTempo = (EditText)findViewById(R.id.editTextTempo);
		editPitch = (EditText)findViewById(R.id.editTextPitch);
		editSpeed = (EditText)findViewById(R.id.editTextSpeed);

		mSeekBar1 = (SeekBar)findViewById(R.id.seekBar1);
		mSeekBar2 = (SeekBar)findViewById(R.id.seekBar2);
		mSeekBar3 = (SeekBar)findViewById(R.id.seekBar3);

		mBtnPlaySource = (Button)findViewById(R.id.btnPlaySource);
		mBtnProcPlay = (Button)findViewById(R.id.btnProcPlay);
		mBtnProcess = (Button)findViewById(R.id.buttonProcess);
		mBtnPlaySource.setOnClickListener(this);
		mBtnProcPlay.setOnClickListener(this);
		mBtnProcess.setOnClickListener(this);

		mPlayer = new MediaPlayer();

		editTempo.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					int value = Integer.valueOf(editTempo.getText().toString()).intValue();
					if (value >= 50 || value <= 200) {
						mSeekBar1.setProgress(value - 50);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		editPitch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					float value = Float.valueOf(editPitch.getText().toString()).intValue();
					if (value >= -12.0f || value <= 12.0f) {
						mSeekBar2.setProgress((int)((value + 12.0f) * 100));
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		editSpeed.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					int value = Integer.valueOf(editSpeed.getText().toString()).intValue();
					if (value >= 50 || value <= 200) {
						mSeekBar3.setProgress(value - 50);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				editTempo.setText(String.valueOf(progress + 50));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ExampleActivity.this, "按住seekBar", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ExampleActivity.this, "放开seekBar", Toast.LENGTH_SHORT).show();
			}
		});
		mSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float value = (float)progress/100.0f - 12.0f;
				String str = String.format("%.2f", value);
				editPitch.setText(str);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ExampleActivity.this, "按住seekBar", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ExampleActivity.this, "放开seekBar", Toast.LENGTH_SHORT).show();
			}
		});
		mSeekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				editSpeed.setText(String.valueOf(progress + 50));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ExampleActivity.this, "按住seekBar", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//Toast.makeText(ExampleActivity.this, "放开seekBar", Toast.LENGTH_SHORT).show();
			}
		});

		// Check soundtouch library presence & version
		checkLibVersion();
	}
	
	
		
	/// Function to append status text onto "console box" on the Activity
	public void appendToConsole(final String text)
	{
		// run on UI thread to avoid conflicts
		runOnUiThread(new Runnable() 
		{
		    public void run() 
		    {
				consoleText.append(text);
				consoleText.append("\n");
				textViewConsole.setText(consoleText);
		    }
		});
	}
	

	
	/// print SoundTouch native library version onto console
	protected void checkLibVersion()
	{
		String ver = SoundTouch.getVersionString();
		appendToConsole("SoundTouch native library version = " + ver);
	}

	private void initMediaPlayer() {
		String inFileName = editSourceFile.getText().toString();
		try {
			mPlayer.setDataSource(inFileName);
			mPlayer.prepare();
			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mediaPlayer) {
					mBtnPlaySource.setText("Play Source");
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class PlayProcThread extends Thread {
		public void run() {
		}
	}

	/// Button click handler
	@Override
	public void onClick(View arg0) 
	{
		switch (arg0.getId())
		{
			case R.id.btnPlaySource:
				if (!mPlaying) {
					initMediaPlayer();
					mPlayer.start();
					mBtnPlaySource.setText("Stop Play Source");
					mPlaying = true;
				} else {
					mPlayer.stop();
					mPlayer.reset();
					mBtnPlaySource.setText("Play Source");
					mPlaying = false;
				}
				break;
			case R.id.btnProcPlay:
				break;
			case R.id.buttonProcess:
				// button "process" pushed
				process();
				break;
			default:
				break;
		}
		
	}

	
	/// Play audio file
	protected void playWavFile(String fileName)
	{
		File file2play = new File(fileName);
		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(file2play), "audio/wav");
		startActivity(i);		
	}
	
				

	/// Helper class that will execute the SoundTouch processing. As the processing may take
	/// some time, run it in background thread to avoid hanging of the UI.
	protected class ProcessTask extends AsyncTask<ProcessTask.Parameters, Integer, Long>
	{
		/// Helper class to store the SoundTouch file processing parameters
		public final class Parameters
		{
			String inFileName;
			String outFileName;
			float tempo;
			float pitch;
			float speed;
		}

		
		/// Function that does the SoundTouch processing
		public final long doSoundTouchProcessing(Parameters params) 
		{
			
			SoundTouch st = new SoundTouch();
			st.setTempo(params.tempo);
			st.setPitchSemiTones(params.pitch);
			st.setSpeed(params.speed);
			Log.i("SoundTouch", "process file " + params.inFileName);
			long startTime = System.currentTimeMillis();
			int res = st.processFile(params.inFileName, params.outFileName);
			long endTime = System.currentTimeMillis();
			float duration = (endTime - startTime) * 0.001f;
			
			Log.i("SoundTouch", "process file done, duration = " + duration);
			appendToConsole("Processing done, duration " + duration + " sec.");
			if (res != 0)
			{
				String err = SoundTouch.getErrorString();
				appendToConsole("Failure: " + err);
				return -1L;
			}

			playWavFile(params.outFileName);
			return 0L;
		}


		
		/// Overloaded function that get called by the system to perform the background processing
		@Override	
		protected Long doInBackground(Parameters... aparams) 
		{
			return doSoundTouchProcessing(aparams[0]);
		}
		
	}


	/// process a file with SoundTouch. Do the processing using a background processing
	/// task to avoid hanging of the UI
	protected void process()
	{
		try 
		{
			ProcessTask task = new ProcessTask();
			ProcessTask.Parameters params = task.new Parameters();
			// parse processing parameters
			//params.inFileName = editSourceFile.getText().toString();
			params.inFileName = "/sdcard/Download/test.mp3";
			params.outFileName = editOutputFile.getText().toString();
			params.tempo = 0.01f * Float.parseFloat(editTempo.getText().toString());
			params.pitch = Float.parseFloat(editPitch.getText().toString());
			params.speed = 0.01f * Float.parseFloat(editSpeed.getText().toString());

			// update UI about status
			appendToConsole("Process audio file :" + params.inFileName +" => " + params.outFileName);
			appendToConsole("Tempo = " + params.tempo);
			appendToConsole("Pitch adjust = " + params.pitch);
			appendToConsole("Speed = " + params.speed);
			
			Toast.makeText(this, "Starting to process file " + params.inFileName + "...", Toast.LENGTH_SHORT).show();

			// start SoundTouch processing in a background thread
			task.execute(params);
//			task.doSoundTouchProcessing(params);	// this would run processing in main thread
			
		}
		catch (Exception exp)
		{
			exp.printStackTrace();
		}
	
	}
}