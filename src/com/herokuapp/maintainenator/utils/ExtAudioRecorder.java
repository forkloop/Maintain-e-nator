package com.herokuapp.maintainenator.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

/**
 * @link
 */
public class ExtAudioRecorder {

    private static final String TAG = "ExtAudioRecorder";

    private final short nChannels = 2;
    private final int sRate = 44100;
    private final short bSamples = 16;

    private AudioRecord audioRecorder = null;
    private String filePath = null;
    private long payload;
    private int bufSize;
    private boolean isRecording;

    public ExtAudioRecorder() {
        bufSize = AudioRecord.getMinBufferSize(sRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        Log.d(TAG, "bufSize: " + bufSize);
        audioRecorder = new AudioRecord(AudioSource.DEFAULT, sRate, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, bufSize);
    }

    /**
     */
    public void setOutputFile(String argPath) {
        filePath = argPath;
    }

    /**
     * For WAV header format, see @link https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
     */
    public void prepare() {
        Log.d(TAG, "Raw recording prepared.");
    }

    private void writeHeader(FileOutputStream out) throws IOException {
        int bRate = sRate * bSamples * nChannels / 8;
        long payload2 = payload + 36;
        byte[] header = new byte[44];
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (payload2 & 0xff);
        header[5] = (byte) ((payload2 >> 8) & 0xff);
        header[6] = (byte) ((payload2 >> 16) & 0xff);
        header[7] = (byte) ((payload2 >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) nChannels;
        header[23] = 0;
        header[24] = (byte) (sRate & 0xff);
        header[25] = (byte) ((sRate >> 8) & 0xff);
        header[26] = (byte) ((sRate >> 16) & 0xff);
        header[27] = (byte) ((sRate >> 24) & 0xff);
        header[28] = (byte) (bRate & 0xff);
        header[29] = (byte) ((bRate >> 8) & 0xff);
        header[30] = (byte) ((bRate >> 16) & 0xff);
        header[31] = (byte) ((bRate >> 24) & 0xff);
        header[32] = (byte) (nChannels * bSamples / 8);
        header[33] = 0;
        header[34] = bSamples;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (payload & 0xff);
        header[41] = (byte) ((payload >> 8) & 0xff);
        header[42] = (byte) ((payload >> 16) & 0xff);
        header[43] = (byte) ((payload >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    /**
     */
    public void release() {
        audioRecorder.release();
    }

    /**
     * Starts the recording. Call after prepare().
     */
    public void start() {
        payload = 0;
        isRecording = true;
        audioRecorder.startRecording();
        new Thread (new Runnable() {
            @Override
            public void run() {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(filePath + ".tmp");
                } catch (FileNotFoundException fnfe) {
                    isRecording = false;
                    audioRecorder.stop();
                    fnfe.printStackTrace();
                }
                if (out != null) {
                    int status = 0;
                    byte[] buffer = new byte[bufSize];
                    while (isRecording) {
                    status = audioRecorder.read(buffer, 0, bufSize);
                        if (status != AudioRecord.ERROR_INVALID_OPERATION) {
                            try {
                                out.write(buffer, 0, status);
                                //payload += bufSize;
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "Reading exception: " + status);
                        }
                    }
                    try {
                        out.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     */
    public void stop() {
        if (isRecording) {
            isRecording = false;
            audioRecorder.stop();
            copyWAVFile();
            File tmpFile = new File(filePath + ".tmp");
            tmpFile.delete();
        }
    }

    private void copyWAVFile() {
        FileInputStream in = null;
        FileOutputStream out = null;
        payload = 0;
        try {
            in = new FileInputStream(filePath + ".tmp");
            out = new FileOutputStream(filePath);
            payload = in.getChannel().size();
            Log.d(TAG, "payload: " + payload);
            writeHeader(out);
            byte[] buffer = new byte[bufSize];
            int status = 0;
            while ((status = in.read(buffer, 0, bufSize)) != -1) {
                out.write(buffer, 0, status);
            }
            in.close();
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     */
    public void reset() {
    }

}//:~