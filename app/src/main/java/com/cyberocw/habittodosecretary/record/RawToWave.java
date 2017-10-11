package com.cyberocw.habittodosecretary.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.cyberocw.habittodosecretary.Const;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by cyber on 2017-07-30.
 */

public class RawToWave implements Runnable{
    private final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;  //안드로이드 녹음시 채널 상수값
    private final int WAVE_CHANNEL_MONO = 1;  //wav 파일 헤더 생성시 채널 상수값
    private final int HEADER_SIZE = 0x2c;
    private final int RECORDER_BPP = 16;
    private final int RECORDER_SAMPLERATE = 0xac44;

    private AudioRecord mAudioRecord;
    private boolean mIsRecording;
    private String mFileName;
    private final int BUFFER_SIZE;
    private BufferedInputStream mBIStream;
    private BufferedOutputStream mBOStream;
    private int mAudioLen = 0;
    File rawFile, waveFile;
    public RawToWave(File rawFile, File waveFile){
        this.rawFile = rawFile;
        this.waveFile = waveFile;
        BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    }

    @Override
    public void run() {
        try {
            rawToWave();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void rawToWave() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;
        try{

            mAudioLen = (int)rawFile.length();
            mBIStream = new BufferedInputStream(new FileInputStream(rawFile));

            mBOStream = new BufferedOutputStream(new FileOutputStream(waveFile));
            mBOStream.write(getFileHeader());
            //len = HEADER_SIZE;
            while ((read = mBIStream.read(buffer)) != -1) {
                mBOStream.write(buffer);
            }
            mBOStream.flush();
            mBIStream.close();
            mBOStream.close();
            Log.d(Const.DEBUG_TAG, "wave create ok ");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private byte[] getFileHeader() {
        byte[] header = new byte[HEADER_SIZE];
        int totalDataLen = mAudioLen + 40;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * WAVE_CHANNEL_MONO/8;
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = (byte)1;  // format = 1 (PCM방식)
        header[21] = 0;
        header[22] =  WAVE_CHANNEL_MONO;
        header[23] = 0;
        header[24] = (byte) (RECORDER_SAMPLERATE & 0xff);
        header[25] = (byte) ((RECORDER_SAMPLERATE >> 8) & 0xff);
        header[26] = (byte) ((RECORDER_SAMPLERATE >> 16) & 0xff);
        header[27] = (byte) ((RECORDER_SAMPLERATE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) RECORDER_BPP * WAVE_CHANNEL_MONO/8;  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte)(mAudioLen & 0xff);
        header[41] = (byte)((mAudioLen >> 8) & 0xff);
        header[42] = (byte)((mAudioLen >> 16) & 0xff);
        header[43] = (byte)((mAudioLen >> 24) & 0xff);
        return header;
    }
}
