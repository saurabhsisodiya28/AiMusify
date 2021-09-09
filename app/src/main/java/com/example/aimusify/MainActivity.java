package com.example.aimusify;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.aimusify.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String[] itemsAll;
    private ListView mSongsList;
    private MediaPlayer myMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongsList = findViewById(R.id.songsList);
        appExternalStoragePermission();

    }

    public void appExternalStoragePermission(){
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        displayAudioSongsName();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response)
                    {

                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> readOnlyAudioSongs(File file)
    {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] allFiles = file.listFiles();
        for (File individualFile: allFiles)
        {
            if (individualFile.isDirectory() && !individualFile.isHidden())
            {
                arrayList.addAll(readOnlyAudioSongs(individualFile));
            }
            else {
                if (individualFile.getName().endsWith(".mp3") || individualFile.getName().endsWith(".aac") || individualFile.getName().endsWith(".wav") || individualFile.getName().endsWith(".wma"))
                {
                    arrayList.add(individualFile);
                }
            }
        }

        return arrayList;
    }

     void displayAudioSongsName() {
         final ArrayList<File> audioSongs = readOnlyAudioSongs(Environment.getExternalStorageDirectory());
         itemsAll = new String[audioSongs.size()];

         for (int songCounter = 0; songCounter < audioSongs.size(); songCounter++) {
             itemsAll[songCounter] = audioSongs.get(songCounter).getName();
         }

         customAdapter customAdapter = new customAdapter();
         mSongsList.setAdapter(customAdapter);

         mSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                     String songName = (String) mSongsList.getItemAtPosition(position);
                     startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                             .putExtra("song", audioSongs)
                             .putExtra("name", songName)
                             .putExtra("position", position));

             }
         });

     }


         class customAdapter extends BaseAdapter {

             @Override
             public int getCount() {
                 return itemsAll.length;
             }

             @Override
             public Object getItem(int position) {
                 return null;
             }

             @Override
             public long getItemId(int position) {
                 return 0;
             }

             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 View myView = getLayoutInflater().inflate(R.layout.list_item, null);
                 TextView textSong = myView.findViewById(R.id.txtSongName);
                 textSong.setSelected(true);
                 textSong.setText(itemsAll[position]);
                 return myView;
             }
         }
}

