package com.zm.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.zm.emulator.detector.EmuCheckUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tv);
        newCheck();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.source_menu, menu);
        return true;
    }
    private void newCheck(){
        StringBuilder mSb = new StringBuilder();
        mSb.append("----start new detect process----\n");
        mSb.append("hasQemuSocket:"+ EmuCheckUtil.hasQemuSocket()+"\n");
        mSb.append("hasQemuPipe:"+ EmuCheckUtil.hasQemuPipe()+"\n");
        mSb.append("hasQemuTrace:"+ EmuCheckUtil.hasQemuTrace()+"\n");
        mSb.append("hasQEmuDrivers:"+ EmuCheckUtil.hasQEmuDrivers()+"\n");
        mSb.append("hasQEmuFiles:"+ EmuCheckUtil.hasQEmuFiles()+"\n");
        mSb.append("hasGenyFiles:"+ EmuCheckUtil.hasGenyFiles()+"\n");
        mSb.append("hasAdbInEmulator:"+ EmuCheckUtil.hasAdbInEmulator()+"\n");
        mSb.append("isEmulatorFromCpu:"+ EmuCheckUtil.isEmulatorFromCpu()+"\n");
        //Log.i("zm","getQEmuDriverFileString:"+ EmuCheckUtil.getQEmuDriverFileString()+"\n");

        mSb.append("final result:"+EmuCheckUtil.getFinalResult());
        mSb.append("\n----end process----");
        mTextView.setText(mSb.toString());

    }
    private void oldCheck(){
        StringBuilder mSb = new StringBuilder();
        mSb.append("----start old detect process----\n");
        boolean result = EmulatorCheckUtil.getSingleInstance().readSysProperty(this, new EmulatorCheckCallback() {
            @Override
            public void findEmulator(String emulatorInfo) {
                mSb.append("\n"+emulatorInfo);
                mSb.append("\n----end process----");
                mTextView.setText(mSb.toString());
            }
        });
        Log.i("zm","final result:"+result);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newMenu:
                newCheck();
                break;
            case R.id.oldMenu:
                oldCheck();
                break;
        }
        return true;
    }
}