package com.herokuapp.maintainenator.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

/**
 * Based on @link
 */
public class ExtAudioRecorder {

    private static final String TAG = "ExtAudioRecorder";

    private final short nChannels = 1;
    private final int sRate = 44100;
    private final short bSamples = 16;
    // Recorder used for uncompressed recording
    private AudioRecord audioRecorder = null;

    // Output file path
    private String filePath = null;

    // File writer (only in uncompressed mode)
    private RandomAccessFile randomAccessWriter;

    private int payload;
    private int bufSize;
    private byte[] buffer;
    private boolean isRecording;

    public ExtAudioRecorder() {
        bufSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecorder = new AudioRecord(AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize);
    }

    /**
     * Sets output file path, call directly after construction/reset.
     * @param output file path
     */
    public void setOutputFile(String argPath) {
        filePath = argPath;
    }

    /**
     * Write WAV file header.
     * For WAV header format, see @link https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
     */
    public void prepare() {
        if (filePath != null) {
            try {
                randomAccessWriter = new RandomAccessFile(filePath, "rw");
                // Set file length to 0, to prevent unexpected behavior in case the file already existed
                randomAccessWriter.setLength(0);
                randomAccessWriter.writeBytes("RIFF");
                // Final file size not known yet, write 0
                randomAccessWriter.writeInt(0); 
                randomAccessWriter.writeBytes("WAVE");
                randomAccessWriter.writeBytes("fmt ");
                randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
                randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
                randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
                randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
                randomAccessWriter.writeInt(Integer.reverseBytes(sRate*bSamples*nChannels/8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
                randomAccessWriter.writeShort(Short.reverseBytes((short)(nChannels*bSamples/8))); // Block align, NumberOfChannels*BitsPerSample/8
                randomAccessWriter.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
                randomAccessWriter.writeBytes("data");
                randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
            }
            Log.d(TAG, "Uncompressed recording prepared.");
        }
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
        buffer = new byte[bufSize];
        isRecording = true;
        audioRecorder.startRecording();
        new Thread (new Runnable() {
            @Override
            public void run() {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(filePath);
                } catch (FileNotFoundException fnfe) {
                    isRecording = false;
                    audioRecorder.stop();
                    fnfe.printStackTrace();
                }
                if (out != null) {
                    int status = 0;
                    while (isRecording) {
                        status = audioRecorder.read(buffer, 0, bufSize);
                        if (status != AudioRecord.ERROR_INVALID_OPERATION) {
                            try {
                                out.write(buffer);
                                payload += bufSize;
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
            try {
                randomAccessWriter.seek(4);
                randomAccessWriter.writeInt(Integer.reverseBytes(36 + payload));
                randomAccessWriter.seek(40);
                randomAccessWriter.writeInt(Integer.reverseBytes(payload));
                randomAccessWriter.close();
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
            }
        }
    }

    /**
     */
    public void reset() {
        //TODO
    }

    /**
     */
    private short getLittleEndianShort(byte byte1, byte byte2) {
        return (short)(byte1 | (byte2 << 8));
    }

}//:~