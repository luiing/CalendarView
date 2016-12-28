package com.uis.calendarview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Map;

/**
 * @author uis
 * @version 1.0.1
 */

public class CalendarDialog {
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private View root;
    private TextView tv_date;
    private CalendarView calendarView;

    private boolean pressBack = true;
    private boolean touchBack = true;
    private OnDayListener mListener;
    private String DateFormat = "yyyy-MM";
    private long selectedMillis;

    public interface OnDayListener{
        void onDay(long millis);
    }

    public CalendarDialog(Context mc){
        builder = new AlertDialog.Builder(mc);
        builder.setCancelable(touchBack);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(touchBack) {
                    dismiss();
                }
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (pressBack && keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        root = LayoutInflater.from(mc).inflate(R.layout.cv_dialog_calendar, null);
        tv_date = (TextView)root.findViewById(R.id.cv_dialog_date);
        calendarView = (CalendarView) root.findViewById(R.id.cv_dialog_calendarView);
        ImageView iv_left = (ImageView)root.findViewById(R.id.cv_dialog_left);
        ImageView iv_right = (ImageView)root.findViewById(R.id.cv_dialog_right);
        TextView ok = (TextView)root.findViewById(R.id.cv_dialog_button_ok);
        final TextView cancel = (TextView)root.findViewById(R.id.cv_dialog_button_cancel);
        ok.setOnClickListener(listner);
        cancel.setOnClickListener(listner);
        iv_left.setOnClickListener(listner);
        iv_right.setOnClickListener(listner);
        calendarView.setOnCalendarCall(new CalendarView.OnCalendarCall() {
            @Override
            public void onDay(Calendar day) {
                final long millis = day.getTimeInMillis();
                setDate(millis);
                selectedMillis = millis;
            }

            @Override
            public void onMonth(Calendar day) {
                final long millis = day.getTimeInMillis();
                setDate(millis);
            }
        });
    }

    private View.OnClickListener listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.cv_dialog_button_ok){
                if(mListener!=null){
                    mListener.onDay(selectedMillis);//long millis
                }
                dismiss();
            }else if(id == R.id.cv_dialog_button_cancel){
                dismiss();
            }else if(id == R.id.cv_dialog_left){
                calendarView.swipByMonth(true);
            }else if(id == R.id.cv_dialog_right){
                calendarView.swipByMonth(false);
            }
        }
    };

    private void setDate(long millis){
        tv_date.setText(CalendarMonthView.getTime(DateFormat,millis));
    }

    /**
     *  set month format
     * @param format yyyy-MM
     */
    public void setDateFormat(String format){
        DateFormat = format;
    }

    public void selectDay(long millis){
        calendarView.selectByDay(millis);
    }

    public void show(OnDayListener call){
        if(dialog==null) {
            dialog = builder.setView(root).create();
        }
        mListener = call;
        try {
            if(!dialog.isShowing()) {
                dialog.show();
            }
        }catch (Exception ex){
        }
    }

    public void dismiss(){
        if(isShowing()){
            dialog.dismiss();
        }
    }

    private boolean isShowing(){
        return dialog!=null && dialog.isShowing();
    }
}
