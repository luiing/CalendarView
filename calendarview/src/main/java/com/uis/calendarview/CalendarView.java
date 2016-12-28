package com.uis.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Calendar;
import java.util.Map;

/**
 * @author uis
 * @version 1.0.1
 */

public class CalendarView extends FrameLayout{

    private static final int MAX = 100000;
    private OnCalendarCall mListener;
    private boolean IsDayClicked = false;
    private int DayClickedDay = -1;
    private int selectDay=-1;
    private int selectYear=-1;
    private int selectMonth=-1;
    private int selectDaysInMonth = -1;
    private int selectPreDaysInMonth = -1;
    private CalendarMonthView currentMonth;
    private ViewPager viewPager;//thie calendar height = 256dp
    private final int mYear;
    private final int mMonth;
    private final int mDay;
    private int monthViewId = 0;

    public CalendarView(Context context) {
        this(context,null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr,0);
        monthViewId = attr.getResourceId(R.styleable.CalendarView_monthViewId,R.layout.cv_view_calendar_month);
        attr.recycle();
        final Calendar calendar = Calendar.getInstance();
        mYear =calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DATE);
        init();
    }

    private void init(){
        viewPager = new ViewPager(getContext());
        addView(viewPager);
        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        final CalendarMonthView monthView = (CalendarMonthView) LayoutInflater.from(getContext()).inflate(monthViewId,null);
        params.height = monthView.getMonthHeight();
        viewPager.setLayoutParams(params);
        final CalendarAdapter mAdapter = new CalendarAdapter();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentMonth = mAdapter.getItem(position);
                if(currentMonth==null)return;
                final int curMonth = currentMonth.getMonth();
                final int curDay = currentMonth.getDay();
                final int curYear = currentMonth.getYear();
                if(IsDayClicked){
                    if(selectYear==curYear && selectMonth==curMonth){//同一月份
                        IsDayClicked = false;
                        currentMonth.selectDay(DayClickedDay);
                        DayClickedDay = -1;
                    }else{
                        IsDayClicked = false;
                        currentMonth.selectDay(DayClickedDay);
                        DayClickedDay = -1;
                    }
                }else{
                    if(selectYear==curYear && selectMonth==curMonth && selectDay>0){
                        currentMonth.markDay(selectDay);
                        if(mListener!=null){
                            mListener.onDay(currentMonth.getCurentCalendar());
                        }
                    }else{
                        currentMonth.markDay(-1);
                        if(mListener!=null){
                            mListener.onMonth(currentMonth.getCurentCalendar());
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(MAX/2);
    }

    public void setOnCalendarCall(OnCalendarCall listener){
        mListener = listener;
    }

    public void markDay(int[] send){
        markDay(null,send);
    }

    //标记
    public void markDay(int[] diff,int[] send){
        if(currentMonth!=null){
            currentMonth.markDay(diff,send);
        }
    }

    //按照天数选择
    public void selectByDay(boolean isPre){//true:pre,false:next
        if(viewPager!=null && currentMonth!=null){
            if(selectDay == -1){
                return;
            }
            final int curMonth = currentMonth.getMonth();
            final int curDay = currentMonth.getDay();
            final int curYear = currentMonth.getYear();
            final int curPosition = viewPager.getCurrentItem();
            if( curYear==selectYear && curMonth == selectMonth){
                currentMonth.selectDay(curDay + (isPre ? -1 : 1));
            }else{
                DayClickedDay = selectDay + (isPre ? -1 : 1);
                int position = MAX/2 + 12*(selectYear-mYear) + (selectMonth-mMonth);
                if(selectDay == 1 && isPre){
                    DayClickedDay = selectPreDaysInMonth;
                    position--;
                }else if(selectDay == selectDaysInMonth && !isPre){
                    DayClickedDay = 1;
                    position++;
                }
                if(curPosition == position){
                    currentMonth.selectDay(DayClickedDay);
                }else if(position>=0 && position< MAX) {
                    IsDayClicked = true;
                    viewPager.setCurrentItem(position, false);
                }
            }
        }
    }

    public void selectByDay(long millis){
        final Calendar mcalendar = Calendar.getInstance();
        mcalendar.setTimeInMillis(millis);
        selectByDay(mcalendar.get(Calendar.YEAR),mcalendar.get(Calendar.MONTH),mcalendar.get(Calendar.DATE));
    }

    public void selectByDay(int year,int month,int day){
        if(viewPager!=null && (month<12&&month>=0)){
            final int position = 12*(year-mYear) + (month - mMonth);
            final int skipPosition = MAX/2+position;
            selectYear = year;
            selectMonth = month;
            selectDay = day;
            if(skipPosition == viewPager.getCurrentItem()){
                if(currentMonth!=null) {
                    currentMonth.selectDay(day);
                }
            }else {
                viewPager.setCurrentItem(skipPosition, false);
            }
        }
    }

    public void swipByMonth(boolean isPre){
        if(null != viewPager){
            viewPager.setCurrentItem( viewPager.getCurrentItem()+(isPre?-1:1),true);
        }
    }

    public void swipByDate(int year,int month){
        if(viewPager!=null){
            final int position = 12*(year-mYear) + (month - mMonth);
            viewPager.setCurrentItem(MAX/2+position);
        }
    }

    public interface OnCalendarCall{
        void onDay(Calendar day);
        void onMonth(Calendar day);
    }

    private CalendarMonthView.OnDayClickListener Listener = new CalendarMonthView.OnDayClickListener() {
        @Override
        public void onDayClick(CalendarMonthView view, Calendar day) {
            selectDay = day.get(Calendar.DATE);
            selectMonth = day.get(Calendar.MONTH);
            selectYear = day.get(Calendar.YEAR);
            selectDaysInMonth = CalendarMonthView.getDaysInMonth(selectYear,selectMonth);
            selectPreDaysInMonth = selectMonth==Calendar.JANUARY ?31:CalendarMonthView.getDaysInMonth(selectYear,selectMonth-1);
            if(mListener!=null){
                mListener.onDay(day);
            }
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
                DayClickedDay = day.get(Calendar.DATE);
                if(position>=0 && position< MAX) {
                    IsDayClicked = true;
                    viewPager.setCurrentItem(position, true);
                }
            }
        }
    };

    private class CalendarAdapter extends PagerAdapter {
        private Map<Integer,CalendarMonthView> map = new ArrayMap<>();

        private CalendarAdapter() {
        }

        private CalendarMonthView getItem(int position){
            return map.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final CalendarMonthView monthView = (CalendarMonthView) LayoutInflater.from(getContext()).inflate(monthViewId,null);
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
            monthView.setOnDayClickListener(Listener);
            if(currentMonth==null && position == MAX/2){
                currentMonth = monthView;
                if(mListener!=null){
                    mListener.onMonth(currentMonth.getCurentCalendar());
                    currentMonth.selectDay(mDay);
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