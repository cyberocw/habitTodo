package com.cyberocw.habittodosecretary.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.util.TTSNoti;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by cyber on 2017-07-30.
 */

public class PlayRawAudio extends AsyncTask<Void, Void, Void> {
    String mPlayFileName;
    Context mCtx;
    public PlayRawAudio(Context ctx, String fileName) {
        mCtx = ctx;
        mPlayFileName = fileName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }
    @Override
    protected Void doInBackground(Void... params) {
        Log.d(this.toString(), " audiotrack  mPlayFileName="+ mPlayFileName);
        File fPlay = new File(mPlayFileName);
        if(!fPlay.isFile()){
            //Toast.makeText(, "couldn't found voice file", Toast.LENGTH_SHORT).show();
            Intent ttsIntent = new Intent(mCtx, TTSNoti.class);
            ttsIntent.putExtra("alaramTitle", "couldn't found voice file");
            ttsIntent.putExtra("alarmId", 1);
            mCtx.startService(ttsIntent);
            return null;
        }
        int bufferSize = AudioTrack.getMinBufferSize(Const.RECORDER.FREQUENCY, Const.RECORDER.CHANNEL_CONFIGURATION_OUT, Const.RECORDER.AUDIO_ENCODING);
        short[] audiodata = new short[bufferSize / 4];

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fPlay)));
            AudioTrack audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC, Const.RECORDER.FREQUENCY,
                    Const.RECORDER.CHANNEL_CONFIGURATION_OUT, Const.RECORDER.AUDIO_ENCODING, bufferSize,
            AudioTrack.MODE_STREAM);

            audioTrack.play();
            while (dis.available() > 0) {
                int i = 0;
                while (dis.available() > 0 && i < audiodata.length) {
                    audiodata[i] = dis.readShort();
                    i++;
                }
                audioTrack.write(audiodata, 0, audiodata.length);
            }
            dis.close();

        } catch (Throwable t) {
            Log.e("AudioTrack", "Playback Failed");
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        //stopPlaying();
    }
}

