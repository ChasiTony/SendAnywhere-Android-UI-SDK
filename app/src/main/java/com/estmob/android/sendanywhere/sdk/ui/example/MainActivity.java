package com.estmob.android.sendanywhere.sdk.ui.example;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.estmob.sdk.transfer.SendAnywhere;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int RESULT_CODE_GET_CONTENT = 1024;
    private static final int CODE_PERMISSIONS = 100;
    private boolean permissionsGranted = false;

    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.buttonReceive) {
                receive();
            } else if (v.getId() == R.id.buttonSend) {
                send();
            } else if (v.getId() == R.id.buttonActivity) {
                showActivity();
            } else if (v.getId() == R.id.buttonTest) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            } else if (v.getId() == R.id.buttonSettings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (permissions.isEmpty()) {
                permissionsGranted = true;
            } else {
                String[] arr = permissions.toArray(new String[permissions.size()]);
                requestPermissions(arr, CODE_PERMISSIONS);
            }
        } else {
            permissionsGranted = true;
        }

        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonSend).setOnClickListener(onButtonClickListener);
        findViewById(R.id.buttonReceive).setOnClickListener(onButtonClickListener);
        findViewById(R.id.buttonActivity).setOnClickListener(onButtonClickListener);
        findViewById(R.id.buttonTest).setOnClickListener(onButtonClickListener);
        findViewById(R.id.buttonSettings).setOnClickListener(onButtonClickListener);

        processIntent(getIntent());
        new SdkPreferences(this).load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SendAnywhere.publishNearBy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SendAnywhere.closeNearBy();
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = intent.getData();
            if (uri != null && permissionsGranted) {
                SendAnywhere.startReceiveActivity(this, uri.toString());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE_GET_CONTENT && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            if (uri != null) {
                SendAnywhere.startSendActivity(this, new Uri[]{uri});
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        Uri[] uris = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); ++i) {
                            ClipData.Item item = clipData.getItemAt(i);
                            uris[i] = item.getUri();
                        }
                        SendAnywhere.startSendActivity(this, uris);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults != null && grantResults.length > 0) {
            boolean granted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                permissionsGranted = true;
                SendAnywhere.publishNearBy();
            } else {
                finish();
            }
        }
    }

    private void receive() {
        if (!permissionsGranted) {
            return;
        }
        SendAnywhere.startReceiveActivity(MainActivity.this);
    }

    private void send() {
        if (!permissionsGranted) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);;
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "audio/*", "video/*"});
        startActivityForResult(intent, RESULT_CODE_GET_CONTENT);
    }

    private void showActivity() {
        if (!permissionsGranted) {
            return;
        }
        SendAnywhere.showActivity(this);
    }
}