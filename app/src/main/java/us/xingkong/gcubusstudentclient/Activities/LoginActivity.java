package us.xingkong.gcubusstudentclient.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import us.xingkong.gcubusstudentclient.Others.ActivityCollector;
import us.xingkong.gcubusstudentclient.R;

public class LoginActivity extends AppCompatActivity {

    Button login;
    ImageView yzm;

    EditText et_username;
    EditText et_password;
    EditText et_yzm;

    String Cookie = "";
    public static String name = null;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (yzm != null) {
                    yzm.setImageBitmap((Bitmap) msg.obj);
                } else {
                    Toast.makeText(LoginActivity.this, "获取验证码失败！", Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == 2) {
                editor = pref.edit();
//                editor.putString("username", String.valueOf(et_username.getText()));
//                editor.putString("password", String.valueOf(et_password.getText()));  //可能会用来验证密码是否更改
                editor.putString("name", name);
                editor.putBoolean("autoLogin", true);
                editor.putBoolean("isNotActive", false);
                editor.apply();
                Toast.makeText(LoginActivity.this, name + "同学登陆成功！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == 3) {
                Toast.makeText(LoginActivity.this, "登陆失败！", Toast.LENGTH_SHORT).show();
                getYZM();
            }
            setEnable(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);

        pref = this.getSharedPreferences("userData",MODE_PRIVATE);
        boolean isAutoLogin = pref.getBoolean("autoLogin", false);
        if (isAutoLogin) {
            name = pref.getString("name", null);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        yzm = (ImageView) findViewById(R.id.yzm);
        yzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getYZM();
            }
        });

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_yzm = (EditText) findViewById(R.id.et_yzm);

        login = (Button) findViewById(R.id.bt_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Login();
            }
        });


        getYZM();
    }

    private void getYZM() {
        Cookie = "";
        new Thread() {
            @Override
            public void run() {

                Bitmap get = getYZMHTTP();
                if (get != null) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = get;
                    handler.sendMessage(msg);
                } else {
                    System.out.println("FAIL");
                }
            }
        }.start();
    }

    private void setEnable(boolean en) {
        et_password.setEnabled(en);
        et_username.setEnabled(en);
        et_yzm.setEnabled(en);
        login.setEnabled(en);
    }

    private void Login() {
        setEnable(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    //System.out.println("http://www.xingkong.us/home/index.php/Home/index/login?xh=" + et_username.getText() + "&pw=" + et_password.getText() + "&code=" + et_yzm.getText());

                    JSONObject obj = connect("http://www.xingkong.us/home/index.php/Home/index/login?xh=" + et_username.getText() + "&pw=" + et_password.getText() + "&code=" + et_yzm.getText());

                    int Status = Integer.valueOf(obj.get("Status").toString());


                    if (Status != 200) {
                        handler.sendEmptyMessage(3);
                    } else {
                        name = obj.getString("xm");
                        handler.sendEmptyMessage(2);
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(3);
                }
            }
        }.start();
    }

    private Bitmap getYZMHTTP() {
        Bitmap result = null;
        try {
            JSONObject obj = connect("http://www.xingkong.us/home/index.php/Home/index/pre_login");
            if (Integer.valueOf(obj.get("Status").toString()) == 200) {
                String url = obj.getString("url");
                URL u = new URL(url);

                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestProperty("Cookie", Cookie);
                Cookie += (getCookie(conn));

                InputStream in = conn.getInputStream();

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int b;
                while ((b = in.read()) != -1) {
                    buffer.write(b);
                }

                buffer.flush();
                byte[] data = buffer.toByteArray();


                result = BitmapFactory.decodeByteArray(data, 0, data.length);

                conn.getInputStream().close();


            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private JSONObject connect(String url) throws IOException, JSONException {
        JSONObject result;

        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestProperty("Cookie", Cookie);
        Cookie += (getCookie(conn));
        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder buffer = new StringBuilder();
        int b;
        while ((b = reader.read()) != -1) {
            buffer.append((char) b);
        }
        String tmp = buffer.toString();


        tmp = tmp.replaceFirst("jsonpReturn\\(", "");
        tmp = tmp.substring(0, tmp.lastIndexOf(");"));
        result = new JSONObject(tmp);

        in.close();
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    private String getCookie(HttpURLConnection conn) {
        String cookie = conn.getHeaderField("Set-Cookie");
        if (cookie != null)
            return cookie;
        else
            return "";
    }


}
