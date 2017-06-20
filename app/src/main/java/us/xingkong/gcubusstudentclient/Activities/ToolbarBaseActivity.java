package us.xingkong.gcubusstudentclient.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import us.xingkong.gcubusstudentclient.Others.ActivityCollector;
import us.xingkong.gcubusstudentclient.R;


/**
 * Created by SeaLynn0 on 2017/6/20.
 */

public abstract class ToolbarBaseActivity extends AppCompatActivity {
    Toolbar toolbar;
    FrameLayout viewContent;
    ActionBar actionbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ActivityCollector.addActivity(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewContent = (FrameLayout) findViewById(R.id.viewContent);
        LayoutInflater.from(ToolbarBaseActivity.this).inflate(getContentView(), viewContent);
        actionbar = getSupportActionBar();
        init(savedInstanceState);
    }

    protected abstract int getContentView();

    protected abstract void init(Bundle savedInstanceState);
}
