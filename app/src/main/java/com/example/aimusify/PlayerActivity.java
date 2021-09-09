package com.example.aimusify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.material.circularreveal.CircularRevealFrameLayout;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class PlayerActivity extends AppCompatActivity {

    private RelativeLayout parentRelativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private ImageView imageview;
    private Button nextBtn, previousBtn, btnff, btnfr, pausePlayBtn;
    private RelativeLayout lowerRelativeLayout;
    private Button voiceEnabledBtn;

    private TextView songNameTxt, txtsstart, txtsstop;

    private MediaPlayer myMediaPlayer;
    private int position;
    private ArrayList<File> mySongs;
    private String mSongName;

    SeekBar seekmusic;
    Thread updateseekbar;
    BarVisualizer visualizer;


    private String mode = "ON";

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        runtimePermission();
        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);

        pausePlayBtn = findViewById(R.id.play_pause_btn);
        nextBtn = findViewById(R.id.btnnext);
        previousBtn = findViewById(R.id.previous_btn);
        songNameTxt = findViewById(R.id.songName);
        imageview = findViewById(R.id.imageview);
        lowerRelativeLayout = findViewById(R.id.lowerRl);
        voiceEnabledBtn = findViewById(R.id.voice_enabled_btn);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        txtsstart = findViewById(R.id.txtsstart);
        txtsstop = findViewById(R.id.txtsstop);
        seekmusic = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(PlayerActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        validateReceiveValuesAndStartPlaying();
        imageview.setBackgroundResource(R.drawable.music);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matchesFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matchesFound != null) {
                    if (mode.equals("ON")) {
                        keeper = matchesFound.get(0);

                        if (keeper.equals("pause the song") || keeper.equals("pause song") || keeper.equals("pause") || keeper.equals("stop") || keeper.equals("stop the song")) {
                            playPauseSong();
                            Toast.makeText(PlayerActivity.this, "" + keeper, Toast.LENGTH_LONG).show();

                        } else if (keeper.equals("play the song") || keeper.equals("play song") || keeper.equals("play") || keeper.equals("start") || keeper.equals("start song") || keeper.equals("start the song")) {
                            playPauseSong();
                            Toast.makeText(PlayerActivity.this, "" + keeper, Toast.LENGTH_LONG).show();

                        } else if (keeper.equals("play the next song") || keeper.equals("play next song") || keeper.equals("play next") || keeper.equals("next") || keeper.equals("next song") || keeper.equals("start next song") || keeper.equals("start next")) {
                            playNextSong();
                            Toast.makeText(PlayerActivity.this, "" + keeper, Toast.LENGTH_LONG).show();

                        } else if (keeper.equals("play the previous song") || keeper.equals("play previous song") || keeper.equals("play previous") || keeper.equals("previous") || keeper.equals("previous song") || keeper.equals("start previous song") || keeper.equals("start previous")) {
                            playPreviousSong();
                            Toast.makeText(PlayerActivity.this, "" + keeper, Toast.LENGTH_LONG).show();

                        }
                    }

                    if (mode.equals("OFF")) {
                        keeper = matchesFound.get(0);
                        Toast.makeText(PlayerActivity.this, "Voice Mode OFF! You cannot access command " + keeper, Toast.LENGTH_LONG).show();

                    }
                }

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        keeper = "";
                        break;

                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;
                }
                return false;
            }
        });

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseSong();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myMediaPlayer.getCurrentPosition() > 0 || myMediaPlayer.getCurrentPosition() == 0) {
                    playPreviousSong();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMediaPlayer.getCurrentPosition() > 0) {
                    playNextSong();
                }
            }
        });

        //next listener
        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextBtn.performClick();
            }
        });

        int audiosessionId = myMediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

        voiceEnabledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("ON")) {
                    mode = "OFF";
                    voiceEnabledBtn.setText("Voice Enabled Mode - OFF");
                    lowerRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    mode = "ON";
                    voiceEnabledBtn.setText("Voice Enabled Mode - ON");
                    lowerRelativeLayout.setVisibility(View.GONE);

                }

            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMediaPlayer.isPlaying()) {
                    myMediaPlayer.seekTo(myMediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMediaPlayer.isPlaying()) {
                    myMediaPlayer.seekTo(myMediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });


        updateseekbar = new Thread() {
            @Override
            public void run() {
                int totalDuration = myMediaPlayer.getDuration();
                int currentpostion = 0;

                while (currentpostion < totalDuration) {
                    try {
                        sleep(500);
                        currentpostion = myMediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentpostion);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();

                    }

                }
            }
        };

        seekmusic.setMax(myMediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    myMediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                } else {
                    // the event was fired from code and you shouldn't call player.seekTo()
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myMediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(myMediaPlayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(myMediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


    }


    private void validateReceiveValuesAndStartPlaying() {
        if (myMediaPlayer != null) {
            myMediaPlayer.stop();
            myMediaPlayer.reset();
            myMediaPlayer.release();

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();

            mySongs = (ArrayList) bundle.getParcelableArrayList("song");
            String songName = intent.getStringExtra("name");
            position = bundle.getInt("position", 0);
            songNameTxt.setSelected(true);
            Uri uri = Uri.parse(mySongs.get(position).toString());
            mSongName = mySongs.get(position).getName();

            songNameTxt.setText(mSongName);
            myMediaPlayer = MediaPlayer.create(PlayerActivity.this, uri);
            myMediaPlayer.start();

        } else {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();

            mySongs = (ArrayList) bundle.getParcelableArrayList("song");
            String songName = intent.getStringExtra("name");
            position = bundle.getInt("position", 0);
            songNameTxt.setSelected(true);
            Uri uri = Uri.parse(mySongs.get(position).toString());
            mSongName = mySongs.get(position).getName();

            songNameTxt.setText(mSongName);
//
//        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
//        retriever.setDataSource(String.valueOf(uri));
//        byte [] data = retriever.getEmbeddedPicture();
//
//        // convert the byte array to a bitmap
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        // do something with the image ...
//        imageview.setImageBitmap(bitmap);
//
//        retriever.release();

            myMediaPlayer = MediaPlayer.create(PlayerActivity.this, uri);
            myMediaPlayer.start();
        }
    }

    private void runtimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(PlayerActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package: " + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    private void playPauseSong() {
        imageview.setBackgroundResource(R.drawable.music);

        if (myMediaPlayer.isPlaying()) {
            pausePlayBtn.setBackgroundResource(R.drawable.ic_play);
            myMediaPlayer.pause();
        } else {
            pausePlayBtn.setBackgroundResource(R.drawable.ic_pause);
            myMediaPlayer.start();

        }
    }

    private void playNextSong() {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        position = ((position + 1) % mySongs.size());

        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(PlayerActivity.this, uri);
        mSongName = mySongs.get(position).getName();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();
//
//        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
//        retriever.setDataSource(String.valueOf(uri));
//        byte [] data = retriever.getEmbeddedPicture();
//
//        // convert the byte array to a bitmap
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        // do something with the image ...
//        imageview.setImageBitmap(bitmap);
//
//        retriever.release();

        startAnimation(imageview);
        String endTime = createTime(myMediaPlayer.getDuration());
        txtsstop.setText(endTime);

        int audiosessionId = myMediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

        imageview.setBackgroundResource(R.drawable.music);

        if (myMediaPlayer.isPlaying()) {
            pausePlayBtn.setBackgroundResource(R.drawable.ic_pause);

        } else {
            pausePlayBtn.setBackgroundResource(R.drawable.ic_play);
            imageview.setBackgroundResource(R.drawable.music);
        }

    }

    private void playPreviousSong() {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        position = ((position - 1) < 0 ? (mySongs.size() - 1) : (position - 1));

        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(PlayerActivity.this, uri);

        mSongName = mySongs.get(position).getName();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();

//        FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
//        retriever.setDataSource(String.valueOf(uri));
//        byte [] data = retriever.getEmbeddedPicture();
//
//        // convert the byte array to a bitmap
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        // do something with the image ...
//        imageview.setImageBitmap(bitmap);
//
//        retriever.release();
//
        startAnimation(imageview);
        String endTime = createTime(myMediaPlayer.getDuration());
        txtsstop.setText(endTime);


        int audiosessionId = myMediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }
        imageview.setBackgroundResource(R.drawable.music);

        if (myMediaPlayer.isPlaying()) {
            pausePlayBtn.setBackgroundResource(R.drawable.ic_pause);

        } else {
            pausePlayBtn.setBackgroundResource(R.drawable.ic_play);
            imageview.setBackgroundResource(R.drawable.music);
        }

    }

    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageview, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int minute = duration / 1000 / 60;
        int second = duration / 1000 % 60;

        time += minute + ":";

        if (second < 10) {
            time += "0";
        }
        time += second;

        return time;
    }


}