package com.cyberocw.habittodosecretary.record;

import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by cyber on 2017-07-30.
 */

public class PlayRawAudio extends AsyncTask<Void, Void, Void> {
    int frequency = 44100, channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    String mPlayFileName;
    public PlayRawAudio(String fileName) {
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

        int bufferSize = AudioTrack.getMinBufferSize(frequency,channelConfiguration, audioEncoding);
        short[] audiodata = new short[bufferSize / 4];

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fPlay)));
            AudioTrack audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC, frequency,
            channelConfiguration, audioEncoding, bufferSize,
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

