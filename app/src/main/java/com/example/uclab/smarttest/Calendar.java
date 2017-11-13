package com.example.uclab.smarttest;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by UCLAB on 2016-10-22.
 */
public class Calendar extends Dialog implements View.OnClickListener {
    private TextView tvDate;  //연, 월 텍스트 뷰
    private TextView curt;
    private GridAdapter gridAdapter; // 그리드뷰 어댑터
    private ArrayList<String> dayList; // 일 저장 할 리스트
    private GridView gridView; // 그리드뷰
    private java.util.Calendar mCal; // 캘린더 변수
    private Button bforw, bback, bok;
    private View.OnClickListener cli;

    long now;
    private Date date;
    private SimpleDateFormat curYearFormat;
    private SimpleDateFormat curMonthFormat;
    private SimpleDateFormat curDayFormat;
    int dayNum;

    private int count;
    private String day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.calendar);

        count = 0;

        curt = (TextView) findViewById(R.id.curText);

        tvDate = (TextView) findViewById(R.id.tv_date);

        gridView = (GridView) findViewById(R.id.gridview);

        bforw = (Button) findViewById(R.id.forw);

        bback = (Button) findViewById(R.id.back);

        bok = (Button) findViewById(R.id.ok);

        bok.setOnClickListener(cli);
        bforw.setOnClickListener(this);
        bback.setOnClickListener(this);

        // 오늘에 날짜를 세팅 해준다.
        now = System.currentTimeMillis();

        date = new Date(now);

        //연,월,일을 따로 저장
        curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
        curDayFormat = new SimpleDateFormat("dd", Locale.KOREA);

        //현재 날짜 텍스트뷰에 뿌려줌

        tvDate.setText(curYearFormat.format(date) + "/" + curMonthFormat.format(date));

        curt.setText(curYearFormat.format(date) + "-" + curMonthFormat.format(date) + "-" + curDayFormat.format(date));

        //gridview 요일 표시
        dayList = new ArrayList<String>();

        dayList.add("일");

        dayList.add("월");

        dayList.add("화");

        dayList.add("수");

        dayList.add("목");

        dayList.add("금");

        dayList.add("토");

        mCal = java.util.Calendar.getInstance();

        //이번달 1일 무슨요일인지 판단 mCal.set(Year,Month,Day)

        mCal.set(Integer.parseInt(curYearFormat.format(date)), Integer.parseInt(curMonthFormat.format(date)) - 1, 1);

        int dayNum = mCal.get(java.util.Calendar.DAY_OF_WEEK);

        //1일 - 요일 매칭 시키기 위해 공백 add

        for (int i = 1; i < dayNum; i++) {
            dayList.add("");
        }

        setCalendarDate(mCal.get(java.util.Calendar.MONTH) + 1);

        gridAdapter = new GridAdapter(this.getContext(), dayList);

        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(gridviewOnItemClickListener);
    }

    public Calendar(Context context, View.OnClickListener single) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.cli = single;
    }

    private GridView.OnItemClickListener gridviewOnItemClickListener = new GridView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg, View v, int position, long id) {
            day = gridAdapter.getItem(position);

            curt.setText(mCal.get(java.util.Calendar.YEAR) + "-" + (mCal.get(java.util.Calendar.MONTH) + 1 + "-" + day));
        }
    };

    // 해당 월에 표시할 일 수 구함
    private void setCalendarDate(int month) {
        mCal.set(java.util.Calendar.MONTH, month - 1);

        for (int i = 0; i < mCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH); i++) {
            dayList.add("" + (i + 1));
        }
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.forw:
                count++;

                dayList = new ArrayList();

                dayList.add("일");

                dayList.add("월");

                dayList.add("화");

                dayList.add("수");

                dayList.add("목");

                dayList.add("금");

                dayList.add("토");

                now = System.currentTimeMillis();

                date = new Date(now);

                curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
                curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
                curDayFormat = new SimpleDateFormat("dd", Locale.KOREA);

                mCal = java.util.Calendar.getInstance();
                mCal.set(Integer.parseInt(curYearFormat.format(date)), Integer.parseInt(curMonthFormat.format(date)) -1 + count, 1);
                dayNum = mCal.get(java.util.Calendar.DAY_OF_WEEK);

                //1일 - 요일 매칭 시키기 위해 공백 add
                for (int i = 1; i < dayNum; i++) {
                    dayList.add("");
                }

                setCalendarDate(mCal.get(java.util.Calendar.MONTH) + 1);

                gridAdapter = new GridAdapter(this.getContext(), dayList);

                gridView.setAdapter(gridAdapter);

                tvDate.setText(mCal.get(java.util.Calendar.YEAR) + "/" + (mCal.get(java.util.Calendar.MONTH) + 1));

                break;

            case R.id.back:
                count--;

                dayList = new ArrayList();

                dayList.add("일");

                dayList.add("월");

                dayList.add("화");

                dayList.add("수");

                dayList.add("목");

                dayList.add("금");

                dayList.add("토");

                now = System.currentTimeMillis();

                date = new Date(now);

                curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
                curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
                curDayFormat = new SimpleDateFormat("dd", Locale.KOREA);

                mCal = java.util.Calendar.getInstance();
                mCal.set(Integer.parseInt(curYearFormat.format(date)), Integer.parseInt(curMonthFormat.format(date)) -1 + count, 1);
                dayNum = mCal.get(java.util.Calendar.DAY_OF_WEEK);

                //1일 - 요일 매칭 시키기 위해 공백 add
                for (int i = 1; i < dayNum; i++) {
                    dayList.add("");
                }

                setCalendarDate(mCal.get(java.util.Calendar.MONTH) + 1);

                gridAdapter = new GridAdapter(this.getContext(), dayList);

                gridView.setAdapter(gridAdapter);

                tvDate.setText(mCal.get(java.util.Calendar.YEAR) + "/" + (mCal.get(java.util.Calendar.MONTH) + 1));

                break;
        }
    }

    private class GridAdapter extends BaseAdapter {
        private final List<String> list;
        private final LayoutInflater inflater;

        /**

         * 생성자

         *

         * @param context

         * @param list

         */

        public GridAdapter(Context context, List<String> list) {
            this.list = list;
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_calendar_gridview, parent, false);

                holder = new ViewHolder();

                holder.tvItemGridView = (TextView)convertView.findViewById(R.id.tv_item_gridview);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.tvItemGridView.setText("" + getItem(position));

            //해당 날짜 텍스트 컬러,배경 변경

            //mCal = java.util.Calendar.getInstance();

            //오늘 day 가져옴

            Integer today = mCal.get(java.util.Calendar.DAY_OF_MONTH);

            String sToday = String.valueOf(today);

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvItemGridView;
    }
}
