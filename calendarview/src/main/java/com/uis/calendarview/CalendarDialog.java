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
 * @version 1.0.0
 */

public class CalendarDialog {
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private View root;
    private TextView tv_date;
    private ViewPager viewPager;
    private CalendarAdapter mAdapter;

    private boolean pressBack = true;
    private boolean touchBack = true;
    private OnDayListener mListener;
    private Calendar currentCalendar;
    private CalendarMonthView currentView;
    private final int mYear;
    private final int mMonth;
    private final int mDay;
    private final String DateFormat = "yyyy-MM";

    private int selectedYear = 0;
    private int selectedMonth = 0;
    private int selectedDay = -1;

    public interface OnDayListener{
        void onDay(long millis);
    }

    public CalendarDialog(Context mc){
        final Calendar mcalendar = Calendar.getInstance();
        mYear = mcalendar.get(Calendar.YEAR);
        mMonth = mcalendar.get(Calendar.MONTH);
        mDay = mcalendar.get(Calendar.DATE);
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
        tv_date = (TextView)root.findViewById(R.id.dialog_date);
        viewPager = (ViewPager)root.findViewById(R.id.dialog_calendar_viewpager);

        ImageView iv_left = (ImageView)root.findViewById(R.id.dialog_left);
        ImageView iv_right = (ImageView)root.findViewById(R.id.dialog_right);
        TextView ok = (TextView)root.findViewById(R.id.button_ok);
        final TextView cancel = (TextView)root.findViewById(R.id.button_cancel);
        ok.setOnClickListener(listner);
        cancel.setOnClickListener(listner);
        iv_left.setOnClickListener(listner);
        iv_right.setOnClickListener(listner);
        mAdapter = new CalendarAdapter(mc);
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(MAX/2);
    }

    private View.OnClickListener listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.button_ok){
                if(mListener!=null){
                    mListener.onDay(currentCalendar.getTimeInMillis());//long millis
                }
                dismiss();
            }else if(id == R.id.button_cancel){
                dismiss();
            }else if(id == R.id.dialog_left){
                viewPager.setCurrentItem(viewPager.getCurrentItem()-1,true);
            }else if(id == R.id.dialog_right){
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1,true);
            }
        }
    };

    public void selectDay(long millis){
        final Calendar mcalendar = Calendar.getInstance();
        mcalendar.setTimeInMillis(millis);
        selectDay(mcalendar.get(Calendar.YEAR),mcalendar.get(Calendar.MONTH),mcalendar.get(Calendar.DATE));
    }
    /**
     *
     * @param year
     * @param month start 0-11
     * @param day start 1
     */
    public void selectDay(int year,int month,int day){
        if(viewPager!=null && (month<12&&month>=0)){
            final int position = 12*(year-mYear) + (month - mMonth);
            final int skipPosition = MAX/2+position;
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;
            if(skipPosition == viewPager.getCurrentItem()){
                if(currentView!=null) {
                    currentView.selectDay(day);
                }
            }else {
                viewPager.setCurrentItem(skipPosition, false);
            }
        }
    }

    private void setDate(long millis){
        tv_date.setText(CalendarMonthView.getTime(DateFormat,millis));
    }

    public void show(){
        show(null);
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

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
        @Override
        public void onPageSelected(int position) {
            CalendarMonthView monthView = mAdapter.getItem(position);
            if(monthView!=null){
                setDate(monthView.getCurentCalendar().getTimeInMillis());
                final int year = monthView.getYear();
                final int month = monthView.getMonth();
                //final int day = monthView.getDay();
                final int curYear = currentCalendar.get(Calendar.YEAR);
                final int curMonth = currentCalendar.get(Calendar.MONTH);
                final int curDay = currentCalendar.get(Calendar.DATE);

                if(selectedDay>0 && (year==selectedYear && month==selectedMonth && selectedDay>0)){//viewpage刷新需要重新标记选定日期
                    monthView.selectDay(selectedDay);
                    selectedDay = -1;
                }else if(curYear == year && curMonth == month){
                    monthView.selectDay(curDay);
                }else{
                    monthView.selectDay(-1);
                }
            }
        }
    };

    private CalendarMonthView.OnDayClickListener dayListener = new CalendarMonthView.OnDayClickListener() {
        @Override
        public void onDayClick(CalendarMonthView view, Calendar day) {
            currentCalendar = day;
            currentView = view;
        }

        @Override
        public void onDayClick(boolean isPre, Calendar day) {
            if(viewPager!=null) {
                int position = viewPager.getCurrentItem();
                if (isPre) {
                    position--;
                } else {
                    position++;
                }
                selectedYear = day.get(Calendar.YEAR);
                selectedMonth = day.get(Calendar.MONTH);
                selectedDay = day.get(Calendar.DATE);
                if(position>=0 && position< MAX) {
                    viewPager.setCurrentItem(position, true);
                }
            }
        }
    };

    private static final int MAX = 100000;

    private class CalendarAdapter extends PagerAdapter {
        private Context mc;
        private Map<Integer,CalendarMonthView> map = new ArrayMap<>();

        public CalendarAdapter(Context mc) {
            this.mc = mc;
        }

        public CalendarMonthView getItem(int position){
            return map.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final CalendarMonthView monthView = (CalendarMonthView)LayoutInflater.from(mc).inflate(R.layout.cv_dialog_calendar_month,null);
            int result = mMonth + position - MAX/2;
            int y;
            int m;
            if(result < 0){
                y = ( (result+1)/12) -1;
                m = result%12;
                if(m<0){
                    m = 12 + m;
                }
            }else{
                y = result/12;
                m = result%12;
            }
            monthView.setTechMothParams( mYear+y, m);
            monthView.setOnDayClickListener(dayListener);
            if(monthView!=null ){
                final int year = monthView.getYear();
                final int month = monthView.getMonth();
                if(currentCalendar==null && position == MAX/2) {//选择今天
                    if(selectedDay>0){
                        monthView.selectDay(selectedDay);
                        selectedDay = -1;
                    }else {
                        monthView.selectDay(mDay);
                    }
                    setDate(monthView.getCurentCalendar().getTimeInMillis());
                }else if(selectedDay>0 && (year==selectedYear && month==selectedMonth)){//跳转日期
                    monthView.selectDay(selectedDay);
                    setDate(monthView.getCurentCalendar().getTimeInMillis());
                    selectedDay = -1;
                }
            }
            map.put(position,monthView);
            container.addView(monthView);
            return monthView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            map.remove(position);
            container.removeView((View)object);
        }

        @Override
        public int getCount() {
            return MAX;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
