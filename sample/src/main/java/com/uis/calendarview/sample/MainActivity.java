package com.uis.calendarview.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.uis.calendarview.CalendarDialog;
import com.uis.calendarview.CalendarMonthView;
import com.uis.calendarview.CalendarView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView)findViewById(R.id.tv);
        tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new CalendarDialog(this).show(new CalendarDialog.OnDayListener() {
            @Override
            public void onDay(long millis) {
                Log.e("log", CalendarMonthView.getTime("yyyy-MM-dd HH:mm:ss",millis));
            }
        });
    }
}
