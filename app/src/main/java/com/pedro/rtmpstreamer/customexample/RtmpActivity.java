package com.pedro.rtmpstreamer.customexample;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.pedro.builder.rtmp.RtmpBuilder;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.encoder.input.video.EffectManager;
import com.pedro.rtmpstreamer.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.ossrs.rtmp.ConnectCheckerRtmp;

public class RtmpActivity extends AppCompatActivity
    implements Button.OnClickListener, ConnectCheckerRtmp {

  private Integer[] orientations = new Integer[] { 0, 90, 180, 270 };

  private RtmpBuilder rtmpBuilder;
  private Button bStartStop, bRecord;
  private EditText etUrl;
  private String currentDateAndTime = "";
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
      + "/rtmp-rtsp-stream-client-java");
  //options menu
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private ActionBarDrawerToggle actionBarDrawerToggle;
  private RadioGroup rgChannel;
  private Spinner spResolution, spOrientation;
  private CheckBox cbEchoCanceler, cbNoiseSuppressor, cbHardwareRotation;
  private EditText etVideoBitrate, etFps, etAudioBitrate, etSampleRate, etWowzaUser,
      etWowzaPassword;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_custom);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
    rtmpBuilder = new RtmpBuilder(surfaceView, this);
    prepareOptionsMenuViews();

    etUrl = (EditText) findViewById(R.id.et_rtp_url);
    bStartStop = (Button) findViewById(R.id.b_start_stop);
    bStartStop.setOnClickListener(this);
    bRecord = (Button) findViewById(R.id.b_record);
    bRecord.setOnClickListener(this);
    Button switchCamera = (Button) findViewById(R.id.switch_camera);
    switchCamera.setOnClickListener(this);
  }

  private void prepareOptionsMenuViews() {
    drawerLayout = (DrawerLayout) findViewById(R.id.activity_custom);
    navigationView = (NavigationView) findViewById(R.id.nv_rtp);
    navigationView.inflateMenu(R.menu.options_rtmp);
    actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.rtsp_streamer,
        R.string.rtsp_streamer) {

      public void onDrawerOpened(View drawerView) {
        actionBarDrawerToggle.syncState();
      }

      public void onDrawerClosed(View view) {
        actionBarDrawerToggle.syncState();
        rtmpBuilder.setVideoBitrateOnFly(
            Integer.parseInt(etVideoBitrate.getText().toString()) * 1024);
      }
    };
    drawerLayout.addDrawerListener(actionBarDrawerToggle);
    //checkboxs
    cbEchoCanceler =
        (CheckBox) navigationView.getMenu().findItem(R.id.cb_echo_canceler).getActionView();
    cbNoiseSuppressor =
        (CheckBox) navigationView.getMenu().findItem(R.id.cb_noise_suppressor).getActionView();
    cbHardwareRotation =
        (CheckBox) navigationView.getMenu().findItem(R.id.cb_hardware_rotation).getActionView();
    //radiobuttons
    RadioButton rbTcp =
        (RadioButton) navigationView.getMenu().findItem(R.id.rb_tcp).getActionView();
    rgChannel = (RadioGroup) navigationView.getMenu().findItem(R.id.channel).getActionView();
    rbTcp.setChecked(true);
    //spinners
    spResolution = (Spinner) navigationView.getMenu().findItem(R.id.sp_resolution).getActionView();
    spOrientation =
        (Spinner) navigationView.getMenu().findItem(R.id.sp_orientation).getActionView();

    ArrayAdapter<Integer> orientationAdapter =
        new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
    orientationAdapter.addAll(orientations);
    spOrientation.setAdapter(orientationAdapter);
    spOrientation.setSelection(0);

    ArrayAdapter<String> resolutionAdapter =
        new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
    resolutionAdapter.addAll(rtmpBuilder.getResolutions());
    spResolution.setAdapter(resolutionAdapter);
    //edittexts
    etVideoBitrate =
        (EditText) navigationView.getMenu().findItem(R.id.et_video_bitrate).getActionView();
    etFps = (EditText) navigationView.getMenu().findItem(R.id.et_fps).getActionView();
    etAudioBitrate =
        (EditText) navigationView.getMenu().findItem(R.id.et_audio_bitrate).getActionView();
    etSampleRate = (EditText) navigationView.getMenu().findItem(R.id.et_samplerate).getActionView();
    etVideoBitrate.setText("2500");
    etFps.setText("30");
    etAudioBitrate.setText("128");
    etSampleRate.setText("44100");
    etWowzaUser = (EditText) navigationView.getMenu().findItem(R.id.et_wowza_user).getActionView();
    etWowzaPassword =
        (EditText) navigationView.getMenu().findItem(R.id.et_wowza_password).getActionView();
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    actionBarDrawerToggle.syncState();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (!drawerLayout.isDrawerOpen(Gravity.START)) {
          drawerLayout.openDrawer(Gravity.START);
        } else {
          drawerLayout.closeDrawer(Gravity.START);
        }
        return true;
      case R.id.clear:
        rtmpBuilder.setEffect(EffectManager.CLEAR);
        return true;
      case R.id.grey_scale:
        rtmpBuilder.setEffect(EffectManager.GREYSCALE);
        return true;
      case R.id.sepia:
        rtmpBuilder.setEffect(EffectManager.SEPIA);
        return true;
      case R.id.negative:
        rtmpBuilder.setEffect(EffectManager.NEGATIVE);
        return true;
      case R.id.aqua:
        rtmpBuilder.setEffect(EffectManager.AQUA);
        return true;
      case R.id.posterize:
        rtmpBuilder.setEffect(EffectManager.POSTERIZE);
        return true;
      case R.id.microphone:
        if (!rtmpBuilder.isAudioMuted()) {
          item.setIcon(getResources().getDrawable(R.drawable.icon_microphone_off));
          rtmpBuilder.disableAudio();
        } else {
          item.setIcon(getResources().getDrawable(R.drawable.icon_microphone));
          rtmpBuilder.enableAudio();
        }
        return true;
      case R.id.camera:
        if (rtmpBuilder.isVideoEnabled()) {
          item.setIcon(getResources().getDrawable(R.drawable.icon_camera_off));
          rtmpBuilder.disableVideo();
        } else {
          item.setIcon(getResources().getDrawable(R.drawable.icon_camera));
          rtmpBuilder.enableVideo();
        }
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.b_start_stop:
        if (!rtmpBuilder.isStreaming()) {
          bStartStop.setText(getResources().getString(R.string.stop_button));
          String resolution =
              rtmpBuilder.getResolutions().get(spResolution.getSelectedItemPosition());
          String user = etWowzaUser.getText().toString();
          String password = etWowzaPassword.getText().toString();
          if (!user.isEmpty() && !password.isEmpty()) {
            rtmpBuilder.setAuthorization(user, password);
          }
          int width = Integer.parseInt(resolution.split("X")[0]);
          int height = Integer.parseInt(resolution.split("X")[1]);

          if (rtmpBuilder.prepareVideo(width, height, Integer.parseInt(etFps.getText().toString()),
              Integer.parseInt(etVideoBitrate.getText().toString()) * 1024,
              cbHardwareRotation.isChecked(), orientations[spOrientation.getSelectedItemPosition()])
              && rtmpBuilder.prepareAudio(
              Integer.parseInt(etAudioBitrate.getText().toString()) * 1024,
              Integer.parseInt(etSampleRate.getText().toString()),
              rgChannel.getCheckedRadioButtonId() == R.id.rb_stereo, cbEchoCanceler.isChecked(),
              cbNoiseSuppressor.isChecked())) {
            rtmpBuilder.startStream(etUrl.getText().toString());
          } else {
            //If you see this all time when you start stream,
            //it is because your encoder device dont support the configuration
            //in video encoder maybe color format.
            //If you have more encoder go to VideoEncoder or AudioEncoder class,
            //change encoder and try
            Toast.makeText(this, "Error preparing stream, This device cant do it",
                Toast.LENGTH_SHORT).show();
            bStartStop.setText(getResources().getString(R.string.start_button));
          }
        } else {
          bStartStop.setText(getResources().getString(R.string.start_button));
          rtmpBuilder.stopStream();
        }
        break;
      case R.id.b_record:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          if (!rtmpBuilder.isRecording()) {
            try {
              if (!folder.exists()) {
                folder.mkdir();
              }
              SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
              currentDateAndTime = sdf.format(new Date());
              rtmpBuilder.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
              bRecord.setText(R.string.stop_record);
              Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
              rtmpBuilder.stopRecord();
              bRecord.setText(R.string.start_record);
              Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
          } else {
            rtmpBuilder.stopRecord();
            bRecord.setText(R.string.start_record);
            Toast.makeText(this,
                "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
          }
        } else {
          Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
              Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.switch_camera:
        try {
          rtmpBuilder.switchCamera();
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
          rtmpBuilder.switchCamera();
        }
        break;
      default:
        break;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (rtmpBuilder.isStreaming()) {
      rtmpBuilder.stopStream();
      bStartStop.setText(getResources().getString(R.string.start_button));
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpBuilder.isRecording()) {
      rtmpBuilder.stopRecord();
      bRecord.setText(R.string.start_record);
      Toast.makeText(this,
          "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
          Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }
  }

  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onConnectionFailedRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
        rtmpBuilder.stopStream();
        bStartStop.setText(getResources().getString(R.string.start_button));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
            && rtmpBuilder.isRecording()) {
          rtmpBuilder.stopRecord();
          bRecord.setText(R.string.start_record);
          Toast.makeText(RtmpActivity.this,
              "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
              Toast.LENGTH_SHORT).show();
          currentDateAndTime = "";
        }
      }
    });
  }

  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
            && rtmpBuilder.isRecording()) {
          rtmpBuilder.stopRecord();
          bRecord.setText(R.string.start_record);
          Toast.makeText(RtmpActivity.this,
              "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
              Toast.LENGTH_SHORT).show();
          currentDateAndTime = "";
        }
      }
    });
  }

  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }
}
