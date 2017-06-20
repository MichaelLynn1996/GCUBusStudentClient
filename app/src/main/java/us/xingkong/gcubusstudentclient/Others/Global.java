package us.xingkong.gcubusstudentclient.Others;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.text.DecimalFormat;

import us.xingkong.gcubusstudentclient.R;

/**
 * Created by SeaLynn0 on 2017/5/19.
 */

public class Global {

    public static String roundTo1(double num) {
        if (num == 0) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("#.0");
        return df.format(num);
    }

    public static void makeDialog(Activity context, int title, int message, DialogInterface.OnClickListener onOK, DialogInterface.OnClickListener onCancel) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        if (onOK != null)
            dialog.setPositiveButton(R.string.ok, onOK);

        if (onCancel != null)
            dialog.setNegativeButton(R.string.cancel, onCancel);
        dialog.show();
    }

}
