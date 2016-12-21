package com.uis.calendarview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author uis
 * @version 1.0.0
 */
public class CalendarMonthView extends View {

    private static final int DAYS_IN_WEEK = 7;
    private static final int MAX_WEEKS_IN_MONTH = 6;
    private static final String MONTH_YEAR_FORMAT = "yyyy-MM";

    private static final int DEFAULT_SELECTED_DAY = -1;
    private static final int DEFAULT_WEEK_START = Calendar.SUNDAY;

    private int weeksNum = MAX_WEEKS_IN_MONTH;
    private int mDesiredMonthHeight = 32;//16

    private int mDesiredWeekHeight = 30;//10
    private int mDesiredDayHeight = 36;//16
    private int mDesiredCellWidth = 36;
    private int mDesiredDaySelectorRadius = 13;
    private int bottomHeight = 10;

    private boolean isEnableMonth = false;
    private int monthTextColor = Color.parseColor("#FFB3B3B3");
    private int monthTextSize = 16;//sp
    private int weekTextColor = Color.parseColor("#FFB3B3B3");
    private int weekTextSize = 10;//sp
    private int dayTextColor = Color.parseColor("#FFFFFFFF");
    private int dayTextSelectedColor = Color.parseColor("#FFFFFFFF");
    private int dayTextDisableColor = Color.parseColor("#80FFFFFF");
    private int dayTextSize = 16;//sp
    private int daySelectedColor = Color.parseColor("#FF83E7FF");//优先级最大
    private int dayMarkSendedColor = Color.parseColor("#FF0091CD");
    private int dayMarkDiffColor = Color.parseColor("#FFF08A8C");
    private float daySelectedStroke = 1.5f;//dp

    private int mToday = DEFAULT_SELECTED_DAY;
    private int mWeekStart = DEFAULT_WEEK_START;
    /**
     * The number of days (ex. 28) in the current month.
     */
    private int mDaysInMonth = 30;//月份天数
    private int mDayOfWeekStart = 1;
    private int mEnabledDayStart = 1;
    private int mEnabledDayEnd = 31;

    private OnDayClickListener mOnDayClickListener;

    private int mHighlightedDay = -1;
    private int mPreviouslyHighlightedDay = -1;

    private final TextPaint mMonthPaint = new TextPaint();
    private final TextPaint mDayOfWeekPaint = new TextPaint();
    private final TextPaint mDayPaint = new TextPaint();

    private final Paint mDaySelectorPaint = new Paint();
    private final Paint mDayMarkPaint = new Paint();

    private String[] mDayOfWeekLabels;
    private final Calendar mCalendar;
    private final Locale mLocale;
    private final NumberFormat mDayFormatter;

    private String mMonthYearLabel;

    private int mMonth = 0;
    private int mYear = 0;

    private int mMonthHeight;
    private int mDayOfWeekHeight;
    private int mDayHeight;
    private int mCellWidth;
    private int mDaySelectorRadius;

    private int mPaddedWidth;
    private int mPaddedHeight;

    private int[] diffArray = {};
    private int[] sendArray = {};

    public CalendarMonthView(Context context) {
        this(context, null);
    }

    public CalendarMonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources res = context.getResources();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarMonthView, defStyleAttr,0);

        isEnableMonth = a.getBoolean(R.styleable.CalendarMonthView_isEnableMonth,false);
        mDesiredMonthHeight = isEnableMonth ? dp2px(res,mDesiredMonthHeight) : 0;
        final String weekNameArray = a.getString(R.styleable.CalendarMonthView_weekNameArray);
        if(!TextUtils.isEmpty(weekNameArray)){
            mDayOfWeekLabels = split(weekNameArray,",");
        }
        monthTextSize = dp2px(res,16);
        weekTextSize = a.getDimensionPixelSize(R.styleable.CalendarMonthView_weekTextSize, dp2px(res,10) );
        dayTextSize = a.getDimensionPixelSize(R.styleable.CalendarMonthView_dayTextSize, dp2px(res,16) );
        int weekHeight = a.getDimensionPixelSize(R.styleable.CalendarMonthView_weekHeight, dp2px(res,30) );
        int dayHeight = a.getDimensionPixelSize(R.styleable.CalendarMonthView_dayHeight, dp2px(res,36) );
        int dayWidth = a.getDimensionPixelSize(R.styleable.CalendarMonthView_dayWidth, dp2px(res,36) );
        int daySelectorRadius = a.getDimensionPixelSize(R.styleable.CalendarMonthView_daySelectorRadius, dp2px(res,13) );
        int daySelectorStroke = a.getDimensionPixelSize(R.styleable.CalendarMonthView_daySelectorStroke, dp2px(res,1.5f));
        int tailHeight = a.getDimensionPixelSize(R.styleable.CalendarMonthView_tailHeight, dp2px(res,10));;

        weekTextColor = a.getColor(R.styleable.CalendarMonthView_weekTextColor, this.weekTextColor);
        dayTextColor  = a.getColor(R.styleable.CalendarMonthView_dayTextColor, this.dayTextColor);
        dayTextSelectedColor  = a.getColor(R.styleable.CalendarMonthView_dayTextSelectorColor,this.dayTextSelectedColor);
        dayTextDisableColor  = a.getColor(R.styleable.CalendarMonthView_dayTextDisableColor,this.dayTextDisableColor);
        daySelectedColor =  a.getColor(R.styleable.CalendarMonthView_daySelectorColor,this.daySelectedColor);
        a.recycle();
        mDesiredWeekHeight = weekHeight;
        mDesiredDayHeight = dayHeight;
        mDesiredCellWidth = dayWidth;
        mDesiredDaySelectorRadius = daySelectorRadius;
        daySelectedStroke = daySelectorStroke;

        bottomHeight = tailHeight;
        mLocale = getLocale(res);
        mDayFormatter = NumberFormat.getIntegerInstance(mLocale);
        mCalendar = Calendar.getInstance(mLocale);
        initLable();
        initPaints();
        //setTechMothParams(mCalendar.get(Calendar.YEAR),mCalendar.get(Calendar.MONTH));
    }

    private Calendar getCalendar(){
        return Calendar.getInstance(mLocale);
    }

    private String[] split(String str, String separator){
        String[] res ={};
        if(!TextUtils.isEmpty(str)){
            res = str.split(separator);
        }
        return res;
    }

    private void initLable() {
        mMonthYearLabel = getTime(MONTH_YEAR_FORMAT,mCalendar.getTimeInMillis());
        if(mDayOfWeekLabels==null) {
            mDayOfWeekLabels = split("日,一,二,三,四,五,六",",");
        }
    }

    private void initPaints() {
        final String monthTypeface = "sans-serif-medium";
        final String dayOfWeekTypeface = "sans-serif-medium";
        final String dayTypeface = "sans-serif-medium";
        mMonthPaint.setAntiAlias(true);
        mMonthPaint.setTextSize(monthTextSize);
        mMonthPaint.setTypeface(Typeface.create(monthTypeface, 0));
        mMonthPaint.setTextAlign(Paint.Align.CENTER);
        mMonthPaint.setStyle(Paint.Style.FILL);
        mMonthPaint.setColor(monthTextColor);

        mDayOfWeekPaint.setAntiAlias(true);
        mDayOfWeekPaint.setTextSize(weekTextSize);
        mDayOfWeekPaint.setTypeface(Typeface.create(dayOfWeekTypeface, 0));
        mDayOfWeekPaint.setTextAlign(Paint.Align.CENTER);
        mDayOfWeekPaint.setStyle(Paint.Style.FILL);
        mDayOfWeekPaint.setColor(weekTextColor);

        mDaySelectorPaint.setAntiAlias(true);
        mDaySelectorPaint.setStyle(Paint.Style.STROKE);
        mDaySelectorPaint.setStrokeWidth(daySelectedStroke);
        mDaySelectorPaint.setColor(daySelectedColor);

        mDayMarkPaint.setAntiAlias(true);
        mDayMarkPaint.setStyle(Paint.Style.FILL);

        mDayPaint.setAntiAlias(true);
        mDayPaint.setTextSize(dayTextSize);
        mDayPaint.setTypeface(Typeface.create(dayTypeface, 0));
        mDayPaint.setTextAlign(Paint.Align.CENTER);
        mDayPaint.setStyle(Paint.Style.FILL);
        mDayPaint.setColor(dayTextColor);
    }

    private int dp2px(Resources res, float dp) {
        return (int) (0.5f + res.getDisplayMetrics().density * dp);
    }

    private Locale getLocale(Resources res) {
        final Locale mLocale;
        if (Build.VERSION.SDK_INT >= 24) {
            mLocale = res.getConfiguration().getLocales().get(0);
        } else {
            mLocale = res.getConfiguration().locale;
        }
        return mLocale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int preferredHeight = mDesiredDayHeight * weeksNum
                + mDesiredWeekHeight + mDesiredMonthHeight
                + getPaddingTop() + getPaddingBottom() + bottomHeight;
        final int preferredWidth = mDesiredCellWidth * DAYS_IN_WEEK
                + getPaddingLeft() + getPaddingRight();
//        LogUtil.e("xx","hMeasureSpec="+heightMeasureSpec+",height="+preferredHeight+",dayH="+mDesiredDayHeight+",weekH="+mDesiredWeekHeight+
//            ",monthH="+mDesiredMonthHeight+",pTop="+getPaddingTop()+",pBottom="+getPaddingBottom()+",bootom="+bottomHeight);
        final int resolvedWidth = resolveSize(preferredWidth, widthMeasureSpec);
        final int resolvedHeight = resolveSize(preferredHeight, heightMeasureSpec);
        setMeasuredDimension(resolvedWidth, resolvedHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }
        // Let's initialize a completely reasonable number of variables.
        final int w = right - left;
        final int h = bottom - top;
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int paddedRight = w - paddingRight;
        final int paddedBottom = h - paddingBottom;
        final int paddedWidth = paddedRight - paddingLeft;
        final int paddedHeight = paddedBottom - paddingTop;
        if (paddedWidth == mPaddedWidth || paddedHeight == mPaddedHeight) {
            return;
        }
        mPaddedWidth = paddedWidth;
        mPaddedHeight = paddedHeight;
        // We may have been laid out smaller than our preferred size. If so,
        // scale all dimensions to fit.
        final int measuredPaddedHeight = getMeasuredHeight() - paddingTop - paddingBottom;
        final float scaleH = paddedHeight / (float) measuredPaddedHeight;
        final int monthHeight = (int) (mDesiredMonthHeight * scaleH);
        final int cellWidth = mPaddedWidth / DAYS_IN_WEEK;
        mMonthHeight = monthHeight;
        mDayOfWeekHeight = (int) (mDesiredWeekHeight * scaleH);
        mDayHeight = (int) (mDesiredDayHeight * scaleH);
        mCellWidth = cellWidth;

        // Compute the largest day selector radius that's still within the clip
        // bounds and desired selector radius.
        final int maxSelectorWidth = cellWidth / 2 + Math.min(paddingLeft, paddingRight);
        final int maxSelectorHeight = mDayHeight / 2 + paddingBottom;
        mDaySelectorRadius = Math.min(mDesiredDaySelectorRadius, Math.min(maxSelectorWidth, maxSelectorHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        canvas.translate(paddingLeft, paddingTop);
        if(isEnableMonth) {
            drawMonth(canvas);
        }
        drawDaysOfWeek(canvas);
        drawDays(canvas);

        canvas.translate(-paddingLeft, -paddingTop);
    }

    void drawMonth(Canvas canvas) {
        final float x = mPaddedWidth / 2f;
        // Vertically centered within the month header height.
        final float lineHeight = mMonthPaint.ascent() + mMonthPaint.descent();
        final float y = (mMonthHeight - lineHeight) / 2f;

        canvas.drawText(mMonthYearLabel, x, y, mMonthPaint);
    }


    void drawDaysOfWeek(Canvas canvas) {
        final TextPaint p = mDayOfWeekPaint;
        final int headerHeight = mMonthHeight;
        final int rowHeight = mDayOfWeekHeight;
        final int colWidth = mCellWidth;

        // Text is vertically centered within the day of week height.
        final float halfLineHeight = (p.ascent() + p.descent()) / 2f;
        final int rowCenter = headerHeight + rowHeight / 2;

        for (int col = 0; col < DAYS_IN_WEEK; col++) {
            final int colCenter = colWidth * col + colWidth / 2;
            final int colCenterRtl;
            colCenterRtl = colCenter;
            final String label = mDayOfWeekLabels[col];
            canvas.drawText(label, colCenterRtl, rowCenter - halfLineHeight, p);
        }
    }

    void drawDays(Canvas canvas) {
        final TextPaint paint = mDayPaint;

        final int headerHeight = mMonthHeight + mDayOfWeekHeight;
        final int rowHeight = mDayHeight;
        final int colWidth = mCellWidth;

        // Text is vertically centered within the row height.
        final float halfLineHeight = (paint.ascent() + paint.descent()) / 2f;
        int rowCenter = headerHeight + rowHeight / 2;
        int preMonthDayNum = (Calendar.JANUARY == mMonth) ? 31 : getDaysInMonth(mYear,mMonth - 1);
        if(isInEditMode()){
            mHighlightedDay = 15;
            diffArray = new int[]{20};
            sendArray = new int[]{25};
        }
        for (int i = 1, size = weeksNum * DAYS_IN_WEEK, col = 0; i <= size; i++) {//mDayOfWeekStart start form 1
            final int colCenter = colWidth * (col % DAYS_IN_WEEK) + colWidth / 2;
            final int colCenterRtl;
            colCenterRtl = colCenter;
            int day;
            int paintType = -1;//0 this month,-1 pre & next month,1 selected,2 mark send ,3 mark diff

            if (i < mDayOfWeekStart) {//pre month
                day = preMonthDayNum + i + 1 - mDayOfWeekStart;
            } else if (i > mDaysInMonth + mDayOfWeekStart - 1) {//next month
                day = i - (mDaysInMonth + mDayOfWeekStart - 1);
            } else {//this month
                paintType = 0;
                day = i + 1 - mDayOfWeekStart;
                boolean isDiff = false;
                boolean isSend = false;
                for(int d:diffArray){
                    if(day==d){
                        isDiff = true;
                        break;
                    }
                }
                if(!isDiff){
                    for(int d:sendArray){
                        if(day==d){
                            isSend = true;
                            break;
                        }
                    }
                }
                if (mHighlightedDay == day) {
                    final Paint selectorPaint = mDaySelectorPaint;
                    canvas.drawCircle(colCenterRtl, rowCenter, mDaySelectorRadius, selectorPaint);
                    paintType = 1;
                }else if (isDiff) {//diff
                    paintType = 1;
                    final Paint diffPaint = mDayMarkPaint;
                    diffPaint.setColor(dayMarkDiffColor);
                    canvas.drawCircle(colCenterRtl, rowCenter, mDaySelectorRadius, diffPaint);
                }else if (isSend) {//send
                    paintType = 1;
                    final Paint sendPaint = mDayMarkPaint;
                    sendPaint.setColor(dayMarkSendedColor);
                    canvas.drawCircle(colCenterRtl, rowCenter, mDaySelectorRadius, sendPaint);
                }
            }
            switch (paintType) {
                case -1:
                    paint.setColor(dayTextDisableColor);
                    break;
                case 1:
                    paint.setColor(dayTextSelectedColor);
                    break;
                default:
                    paint.setColor(dayTextColor);
                    break;
            }
            canvas.drawText(mDayFormatter.format(day), colCenterRtl, rowCenter - halfLineHeight, paint);
            col++;
            if (col / DAYS_IN_WEEK > 0 && col % DAYS_IN_WEEK == 0) {
                rowCenter += rowHeight;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) (event.getX() + 0.5f);
        final int y = (int) (event.getY() + 0.5f);
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                final int touchedItem = getDayAtLocation(x, y);
                mPreviouslyHighlightedDay = touchedItem;
                break;
            case MotionEvent.ACTION_MOVE://当touchedtem不在月份内，不更新UI
                if (mPreviouslyHighlightedDay < 0) {
                    // Touch something that's not an item, reject event.
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                final int clickedItem = getDayAtLocation(x, y);
                if (clickedItem == mPreviouslyHighlightedDay) {
                    onIndexClicked(clickedItem);
                    invalidate();
                }else{
                    return false;
                }
                break;
            // Fall through.
            case MotionEvent.ACTION_CANCEL:
                //invalidate();
                break;
        }
        return true;
    }

    private int getDayAtLocation(int x, int y) {
        final int paddedX = x - getPaddingLeft();
        if (paddedX < 0 || paddedX >= mPaddedWidth) {
            return -1;
        }
        final int headerHeight = mMonthHeight + mDayOfWeekHeight;
        final int paddedY = y - getPaddingTop();
        if (paddedY < headerHeight || paddedY >= mPaddedHeight) {
            return -1;
        }
        // Adjust for RTL after applying padding.
        final int paddedXRtl;

        paddedXRtl = paddedX;

        final int row = (paddedY - headerHeight) / mDayHeight;
        final int col = (paddedXRtl * DAYS_IN_WEEK) / mPaddedWidth;
        final int index = col + row * DAYS_IN_WEEK;//start from 0
        return index;//day;
    }

    private int findDayOffset() {
        final int offset = mDayOfWeekStart - mWeekStart;
        if (mDayOfWeekStart < mWeekStart) {
            return offset + DAYS_IN_WEEK;
        }
        return offset;
    }

    private boolean onDayClicked(int day){
        if (!isValidDayOfMonth(day) || !isDayEnabled(day)) {
            return false;
        }
        if(mHighlightedDay == day){
            return false;
        }
        mHighlightedDay = day;
        if (mOnDayClickListener != null) {
            final Calendar date = getCalendar();
            date.set(Calendar.YEAR,mYear);
            date.set(Calendar.MONTH,mMonth);
            date.set(Calendar.DATE,day);
            mOnDayClickListener.onDayClick(this, date);
        }
        return true;
    }

    //pre:daysInMonth-(offset-1)+index
    //now:index-(offset-1)   index[offset,daysInMonth+(offset-1)]
    //next:index-(offset-1)-daysInMonth
    private boolean onIndexClicked(int index) {
        final int offset = findDayOffset();
        final int nowDays = mDaysInMonth;
        int startIndex = findDayOffset();
        int endIndex = nowDays + (offset-1);
        int day;
        if(index<0 || index >= DAYS_IN_WEEK*MAX_WEEKS_IN_MONTH ){
            return false;
        }if(index < startIndex){//pre month
            final int preDays;//上一个月天数
            final int preMonth;
            final int preYear;
            if(Calendar.JANUARY ==mMonth ){
                preDays = 31;
                preYear = mYear -1;
                preMonth = Calendar.DECEMBER;
            }else{
                preMonth = mMonth-1;
                preYear = mYear;
                preDays = getDaysInMonth(mYear,preMonth);
            }
            day = preDays + index - (offset-1);
            mHighlightedDay = -1;
            if (mOnDayClickListener != null){
                final Calendar date = getCalendar();
                date.set(Calendar.YEAR,preYear);
                date.set(Calendar.MONTH,preMonth);
                date.set(Calendar.DATE,day);
                mOnDayClickListener.onDayClick(true,date);
            }
        }else if(index > endIndex){//next month
            final int nextMonth;
            final int nextYear;
            if(Calendar.DECEMBER ==mMonth ){
                nextMonth = Calendar.JANUARY;
                nextYear = mYear+1;
            }else{
                nextYear = mYear;
                nextMonth = mMonth+1;
            }
            day = index - nowDays - (offset-1);
            mHighlightedDay = -1;
            if (mOnDayClickListener != null){
                final Calendar date = getCalendar();
                date.set(Calendar.YEAR,nextYear);
                date.set(Calendar.MONTH,nextMonth);
                date.set(Calendar.DATE,day);
                mOnDayClickListener.onDayClick(false,date);
            }
        }else{//current month
            day = index - (offset-1);
            return onDayClicked(day);
        }
        return true;
    }

    private boolean isDayEnabled(int day) {
        return day >= mEnabledDayStart && day <= mEnabledDayEnd;
    }

    private boolean isValidDayOfMonth(int day) {
        return day >= 1 && day <= mDaysInMonth;
    }

    public Calendar getCurentCalendar(){
        final Calendar date = getCalendar();
        date.set(Calendar.YEAR,mYear);
        date.set(Calendar.MONTH,mMonth);
        date.set(Calendar.DATE,mHighlightedDay>0?mHighlightedDay:1);
        return date;
    }

    public int getMonth(){
        return mMonth;
    }

    public int getYear(){
        return mYear;
    }

    public int getDay(){
        return mHighlightedDay;
    }

    public void markDay(int day){
        if(isValidDayOfMonth(day) && isDayEnabled(day)){
            mHighlightedDay = day;
        }else{
            mHighlightedDay = -1;
        }
        invalidate();
    }

    public void markDay(int[] diffArray,int[] sendArray){//设置不同和发送日期
        if(diffArray!=null) {
            this.diffArray = diffArray;
        }
        if(sendArray!=null) {
            this.sendArray = sendArray;
        }
        invalidate();
    }

    public void selectDay(int selectedDay){
        if(0 == selectedDay || selectedDay == (mDaysInMonth+1) ){
            if(0 == selectedDay){
                //selectedDay = findDayOffset() -1;
                final int preDays;//上一个月天数
                final int preMonth;
                final int preYear;
                if(Calendar.JANUARY ==mMonth ){
                    preDays = 31;
                    preYear = mYear -1;
                    preMonth = Calendar.DECEMBER;
                }else{
                    preMonth = mMonth-1;
                    preYear = mYear;
                    preDays = getDaysInMonth(mYear,preMonth);
                }
                int day = preDays;
                mHighlightedDay = -1;
                if (mOnDayClickListener != null){
                    final Calendar date = getCalendar();
                    date.set(Calendar.YEAR,preYear);
                    date.set(Calendar.MONTH,preMonth);
                    date.set(Calendar.DATE,day);
                    mOnDayClickListener.onDayClick(true,date);
                }
                invalidate();
                return;
            }else{
                selectedDay = mDaysInMonth + findDayOffset();
            }
            onIndexClicked(selectedDay);
        }if(selectedDay>0 && selectedDay<=mDaysInMonth){
            onDayClicked(selectedDay);
        }else{
            mHighlightedDay = -1;
        }
        invalidate();
    }

    public void setTechMothParams(int year,int month){//设置时间
        if(year>0) {
            mCalendar.set(Calendar.YEAR, year);
            mYear = year;
        }
        if(month<=11 && month >= 0) {
            mCalendar.set(Calendar.MONTH, month);
            mMonth = month;
        }
        mCalendar.set(Calendar.DATE,1);

        mDaysInMonth = getDaysInMonth(mYear, mMonth );
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);
        invalidate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean focusChanged = false;
        final int _keyCode = event.getKeyCode();
        switch (_keyCode){
            case KeyEvent.KEYCODE_DPAD_LEFT:
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                break;
            case KeyEvent.KEYCODE_TAB:
                break;
        }
        if(focusChanged){
            invalidate();
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    public interface OnDayClickListener {
        void onDayClick(CalendarMonthView view, Calendar day);
        void onDayClick(boolean isPre, Calendar day);
    }

    public static int getDaysInMonth(Calendar calendar){
        return getDaysInMonth(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH));
    }

    public static int getDaysInMonth(int year,int month){
        int dayInMonth = 30;
        switch (month){
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                dayInMonth = 31;
                break;
            case Calendar.FEBRUARY:
                dayInMonth = (year%4 == 0) ? 29 : 28;
                break;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                dayInMonth = 30;
                break;
        }
        return dayInMonth;
    }

    public static  String getTime(String format,long timestamp){
        SimpleDateFormat formatter   =   new   SimpleDateFormat   (format);
        Date curDate   =   new   Date(timestamp);//获取当前时间
        String   str   =   formatter.format(curDate);
        return str;
    }
}