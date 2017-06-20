package us.xingkong.gcubusstudentclient.Activities;

import android.os.Bundle;
import android.view.View;

import us.xingkong.gcubusstudentclient.R;

/**
 * Created by SeaLynn0 on 2017/4/26.
 */

public class AboutActivity extends ToolbarBaseActivity {

    @Override
    protected int getContentView() {
        return R.layout.activity_about;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.this.finish();
            }
        });

        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle(R.string.about);
        }
    }
}
