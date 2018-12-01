package com.videooverlay;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playOverlay();
    }

    private void playOverlay(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StartPlayingOverlay(getString(R.string.content_url));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void StartPlayingOverlay(String url) {
        if (!Settings.canDrawOverlays(this)) {
            Intent request = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(request, REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, VideoPlayerOverviewService.class);
            intent.putExtra("data", url);
            startService(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check if received result code
        //is equal our requested code for draw permission
        if (requestCode == REQUEST_CODE) {
            StartPlayingOverlay(getString(R.string.content_url));
        }
    }
}
