package wt23.ru.operator23;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.seu.magicfilter.utils.MagicFilterType;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.IOException;
import java.net.SocketException;

public class StreamActivity extends AppCompatActivity implements RtmpHandler.RtmpListener,
        SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {
    String battleID;
    int rp;

    private Button btnPublish;
    private Button btnSwitchCamera;
    private Button btnRecord;
    private Button btnSwitchEncoder;
    private Spinner lFilter, quality;

    private SharedPreferences sp;
    private String rtmpUrl = "rtmp://82.199.101.55:1935/wt23/...";
    private String recPath = Environment.getExternalStorageDirectory().getPath() + "/test.mp4";

    private SrsPublisher mPublisher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        battleID = getIntent().getStringExtra("battle_id");

        sp = getSharedPreferences("Yasea", MODE_PRIVATE);
        rtmpUrl = sp.getString("rtmpUrl", rtmpUrl);

        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText("rtmp://82.199.101.55:1935/wt23/" + battleID);

        btnPublish = (Button) findViewById(R.id.publish);
        btnSwitchCamera = (Button) findViewById(R.id.swCam);
        btnRecord = (Button) findViewById(R.id.record);
        btnSwitchEncoder = (Button) findViewById(R.id.swEnc);
        lFilter = (Spinner) findViewById(R.id.lFilter);
        quality = (Spinner) findViewById(R.id.quality);
        quality.setVisibility(View.GONE);

        mPublisher = new SrsPublisher((SrsCameraView) findViewById(R.id.glsurfaceview_camera));
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));

        mPublisher.setPreviewResolution(1280, 720);
        mPublisher.setOutputResolution(1280, 720);
        mPublisher.setVideoHDMode();
        mPublisher.setScreenOrientation(0);
        mPublisher.startCamera();


        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btnPublish.getText().toString().contentEquals("publish")) {
                    if (battleID != null) {
                        Toast.makeText(getApplicationContext(), "BattleID = " + battleID, Toast.LENGTH_SHORT).show();
                        Thread th = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                InetWork inetWork = new InetWork();
                                inetWork.startStreamBattle(battleID);
                                rp = inetWork.size;
                            }
                        });
                        th.start();
                        try {
                            th.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (rp == 200) {
                        Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), String.valueOf(rp), Toast.LENGTH_SHORT).show();
                    }

                    rtmpUrl = efu.getText().toString();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("rtmpUrl", rtmpUrl);
                    editor.apply();

                    mPublisher.startPublish(rtmpUrl);
                    mPublisher.startCamera();

                    if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
                        Toast.makeText(getApplicationContext(), "Use hard encoder", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Use soft encoder", Toast.LENGTH_SHORT).show();
                    }

                    btnPublish.setText("stop");
                    btnSwitchEncoder.setEnabled(false);
                    //quality.setEnabled(false);
                } else if (btnPublish.getText().toString().contentEquals("stop")) {
                    mPublisher.stopPublish();
                    mPublisher.stopRecord();
                    btnPublish.setText("publish");
                    btnRecord.setText("record");
                    btnSwitchEncoder.setEnabled(true);
                    //quality.setEnabled(true);
                    mPublisher.startCamera();
                }
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublisher.switchCameraFace((mPublisher.getCamraId() + 1) % Camera.getNumberOfCameras());
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnRecord.getText().toString().contentEquals("record")) {
                    if (mPublisher.startRecord(recPath)) {
                        btnRecord.setText("pause");
                    }
                } else if (btnRecord.getText().toString().contentEquals("pause")) {
                    mPublisher.pauseRecord();
                    btnRecord.setText("resume");
                } else if (btnRecord.getText().toString().contentEquals("resume")) {
                    mPublisher.resumeRecord();
                    btnRecord.setText("pause");
                }
            }
        });

        btnSwitchEncoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
                    mPublisher.switchToSoftEncoder();
                    btnSwitchEncoder.setText("hard encoder");
                } else if (btnSwitchEncoder.getText().toString().contentEquals("hard encoder")) {
                    mPublisher.switchToHardEncoder();
                    btnSwitchEncoder.setText("soft encoder");
                }
            }
        });

        /*String[] qualityList = {"640x360", "1280x720"};
        ArrayAdapter<String> adapterQualityList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, qualityList);
        quality.setAdapter(adapterQualityList);
        quality.setSelection(0);
        quality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mPublisher.stopCamera();
                        mPublisher.setVideoHDMode();
                        mPublisher.setPreviewResolution(640, 360);
                        mPublisher.setOutputResolution(360, 640);
                        mPublisher.startCamera();
                    case 1:
                        mPublisher.stopCamera();
                        mPublisher.setVideoHDMode();
                        mPublisher.setPreviewResolution(1280, 720);
                        mPublisher.setOutputResolution(720, 1280);
                        mPublisher.startCamera();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPublisher.stopCamera();
                mPublisher.setVideoHDMode();
                mPublisher.setPreviewResolution(640, 360);
                mPublisher.setOutputResolution(360, 640);
                mPublisher.startCamera();
            }
        });*/

        String[] filterList = {"COOL", "BEAUTY", "EARLYBIRD", "EVERGREEN", "N1977", "NOSTALGIA", "ROMANCE", "SUNRISE", "SUNSET", "TENDER", "TOASTER2", "VALENCIA", "WALDEN", "WARM", "Original"};
        ArrayAdapter<String> adapterFilterList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterList);
        lFilter.setAdapter(adapterFilterList);
        lFilter.setSelection(filterList.length - 1);
        lFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mPublisher.switchCameraFilter(MagicFilterType.COOL);
                        break;
                    case 1:
                        mPublisher.switchCameraFilter(MagicFilterType.BEAUTY);
                        break;
                    case 2:
                        mPublisher.switchCameraFilter(MagicFilterType.EARLYBIRD);
                        break;
                    case 3:
                        mPublisher.switchCameraFilter(MagicFilterType.EVERGREEN);
                        break;
                    case 4:
                        mPublisher.switchCameraFilter(MagicFilterType.N1977);
                        break;
                    case 5:
                        mPublisher.switchCameraFilter(MagicFilterType.NOSTALGIA);
                        break;
                    case 6:
                        mPublisher.switchCameraFilter(MagicFilterType.ROMANCE);
                        break;
                    case 7:
                        mPublisher.switchCameraFilter(MagicFilterType.SUNRISE);
                        break;
                    case 8:
                        mPublisher.switchCameraFilter(MagicFilterType.SUNSET);
                        break;
                    case 9:
                        mPublisher.switchCameraFilter(MagicFilterType.TENDER);
                        break;
                    case 10:
                        mPublisher.switchCameraFilter(MagicFilterType.TOASTER2);
                        break;
                    case 11:
                        mPublisher.switchCameraFilter(MagicFilterType.VALENCIA);
                        break;
                    case 12:
                        mPublisher.switchCameraFilter(MagicFilterType.WALDEN);
                        break;
                    case 13:
                        mPublisher.switchCameraFilter(MagicFilterType.WARM);
                        break;
                    case 14:
                    default:
                        mPublisher.switchCameraFilter(MagicFilterType.NONE);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        final Button btn = (Button) findViewById(R.id.publish);
        btn.setEnabled(true);
        mPublisher.resumeRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPublisher.pauseRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.stopEncode();
        mPublisher.stopRecord();
        btnRecord.setText("record");
        mPublisher.setScreenOrientation(newConfig.orientation);
        if (btnPublish.getText().toString().contentEquals("stop")) {
            mPublisher.startEncode();
        }
        mPublisher.startCamera();
    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
            mPublisher.stopRecord();
            btnPublish.setText("publish");
            btnRecord.setText("record");
            btnSwitchEncoder.setEnabled(true);
        } catch (Exception e1) {
            //
        }
    }

    @Override
    public void onRtmpConnecting(String msg) {
        writeConnecting(msg);
    }

    @Override
    public void onRtmpConnected(String msg) {
        writeConnecting(msg);
    }

    @Override
    public void onRtmpVideoStreaming() {

    }


    @Override
    public void onRtmpAudioStreaming() {

    }

    @Override
    public void onRtmpStopped() {
        writeConnecting("RTMP Stopped");
    }

    @Override
    public void onRtmpDisconnected() {
        writeConnecting("RTMP Disconnected");
    }

    private void writeVideoBitRate(String txt) {
        TextView videoBitRate = (TextView) findViewById(R.id.videoBitrate);
        videoBitRate.setText(txt);
    }

    private void writeAudioBitRate(String txt) {
        TextView audioBitRate = (TextView) findViewById(R.id.audioBitrate);
        audioBitRate.setText(txt);
    }

    private void writeVideoFPS(String txt) {
        TextView vidFPS = (TextView) findViewById(R.id.videoFps);
        vidFPS.setText(txt);
    }

    private void writeConnecting(String txt) {
        TextView tv = (TextView) findViewById(R.id.tvConnecting);
        tv.setText(txt);
    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        writeVideoFPS(String.valueOf(fps));
    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            writeVideoBitRate(String.valueOf(bitrate / 1000) + " kbps");
        } else {
            writeVideoBitRate(String.valueOf(rate) + " bps");
        }
    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            writeAudioBitRate(String.valueOf(bitrate / 1000) + " kbps");
        } else {
            writeAudioBitRate(String.valueOf(rate) + " bps");
        }
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        handleException(e);
    }

    @Override
    public void onNetworkWeak() {
        writeConnecting("Network WEAK");
    }

    @Override
    public void onNetworkResume() {
        writeConnecting("Network Resume");
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecordStarted(String msg) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRecordFinished(String msg) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }
}
