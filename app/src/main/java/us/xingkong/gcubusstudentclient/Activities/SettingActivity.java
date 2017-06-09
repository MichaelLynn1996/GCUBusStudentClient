package us.xingkong.gcubusstudentclient.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import us.xingkong.gcubusstudentclient.Others.ActivityCollector;
import us.xingkong.gcubusstudentclient.Others.Global;
import us.xingkong.gcubusstudentclient.R;

/**
 * Created by SeaLynn0 on 2017/6/8.
 */

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView logout;
    Switch sw_locate;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActivityCollector.addActivity(this);

        pref = this.getSharedPreferences("userData", MODE_PRIVATE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
                SettingActivity.this.finish();
            }
        });
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle(R.string.setting);
        }

        sw_locate = (Switch) findViewById(R.id.sw_locate);
        boolean isNotActive = pref.getBoolean("isNotActive", false);
        sw_locate.setChecked(isNotActive);
        sw_locate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor = pref.edit();
                    editor.putBoolean("isNotActive", true);
                    editor.apply();
                } else {
                    editor = pref.edit();
                    editor.putBoolean("isNotActive", false);
                    editor.apply();
                }
            }
        });


        logout = (TextView) findViewById(R.id.tv_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Global.makeDialog(SettingActivity.this, "提示", "是否注销登陆？", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                                editor = pref.edit();
                                editor.putBoolean("autoLogin", false);
                                editor.apply();
                                Toast.makeText(SettingActivity.this, "注销成功！", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                finish();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
