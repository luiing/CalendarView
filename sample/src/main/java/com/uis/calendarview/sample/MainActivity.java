package com.uis.calendarview.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.uis.calendarview.CalendarDialog;
import com.uis.calendarview.CalendarMonthView;
import com.uis.calendarview.CalendarView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv;
    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.tv);
        calendarView = (CalendarView)findViewById(R.id.calendarView);

        tv.setOnClickListener(this);
        calendarView.setOnCalendarCall(new CalendarView.OnCalendarCall() {
            @Override
            public void onDay(Calendar day) {
                tv.setText(CalendarMonthView.getTime("yyyy-MM-dd",day.getTimeInMillis()));
            }

            @Override
            public void onMonth(Calendar day) {
                calendarView.markDay(new int[]{15,16},new int[]{20,21,22});
                tv.setText(CalendarMonthView.getTime("yyyy-MM",day.getTimeInMillis()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        new CalendarDialog(this).show(new CalendarDialog.OnDayListener() {
            @Override
            public void onDay(long millis) {
                tv.setText( CalendarMonthView.getTime("yyyy-MM-dd HH:mm:ss",millis) );
            }
        });
    }
}
