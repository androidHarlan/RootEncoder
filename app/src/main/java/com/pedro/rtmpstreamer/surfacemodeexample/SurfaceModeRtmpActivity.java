package com.pedro.rtmpstreamer.surfacemodeexample;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.pedro.builder.rtmp.RtmpBuilderSurfaceMode;
import com.pedro.rtmpstreamer.R;
import net.ossrs.rtmp.ConnectCheckerRtmp;

/**
 * Unstable activity. See builder header.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SurfaceModeRtmpActivity extends AppCompatActivity
    implements ConnectCheckerRtmp, View.OnClickListener {

  private RtmpBuilderSurfaceMode rtmpBuilderSurfaceMode;
  private Button button;
  private EditText etUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_example);
    SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
    button = (Button) findViewById(R.id.b_start_stop);
    button.setOnClickListener(this);
    etUrl = (EditText) findViewById(R.id.et_rtp_url);
    etUrl.setHint(R.string.hint_rtmp);
    rtmpBuilderSurfaceMode = new RtmpBuilderSurfaceMode(surfaceView, this);
  }

  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onConnectionFailedRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
        rtmpBuilderSurfaceMode.stopStream();
        button.setText(R.string.start_button);
      }
    });
  }

  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SurfaceModeRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onClick(View view) {
    if (!rtmpBuilderSurfaceMode.isStreaming()) {
      if (rtmpBuilderSurfaceMode.prepareAudio() && rtmpBuilderSurfaceMode.prepareVideo()) {
        button.setText(R.string.stop_button);
        rtmpBuilderSurfaceMode.startStream(etUrl.getText().toString());
      } else {
        Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT)
            .show();
      }
    } else {
      button.setText(R.string.start_button);
      rtmpBuilderSurfaceMode.stopStream();
    }
  }
}