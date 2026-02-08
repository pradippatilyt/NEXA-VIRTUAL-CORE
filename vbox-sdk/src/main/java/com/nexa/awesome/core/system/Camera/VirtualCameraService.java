package com.nexa.awesome.core.system.Camera;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import com.nexa.awesome.utils.compat.BuildCompat;

public class VirtualCameraService extends Service {
    private MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String videoPath = intent.getStringExtra("video_path");
            if (videoPath == null) {
                return START_NOT_STICKY;
            }
            boolean isNetwork = intent.getBooleanExtra("is_network", false);
            boolean enableAudio = intent.getBooleanExtra("enable_audio", false);

            stopMediaPlayer();
            setupVirtualCamera(videoPath, isNetwork, enableAudio);
        }
        return START_STICKY;
    }

    private void setupVirtualCamera(String videoPath, boolean isNetwork, boolean enableAudio) {
        try {
            if (isNetwork) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(videoPath);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mediaPlayer.prepareAsync();
            } else {
                mediaPlayer = MediaPlayer.create(this, Uri.parse(videoPath));
            }

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(enableAudio ? 1f : 0f, enableAudio ? 1f : 0f);

                // Global Fake Surface
                SurfaceTexture surfaceTexture = new SurfaceTexture(10);
                Surface surface = new Surface(surfaceTexture);
                BuildCompat.surface = surface;

                mediaPlayer.setSurface(surface);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e("VirtualCamera", "Error setting up virtual camera", e);
            stopSelf();
        }
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMediaPlayer();
        BuildCompat.surface = null;
    }
}