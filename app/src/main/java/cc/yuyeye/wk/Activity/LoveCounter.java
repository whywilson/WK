package cc.yuyeye.wk.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import cc.yuyeye.wk.R;
import cc.yuyeye.wk.Util.ToastUtil;

import static cc.yuyeye.wk.MainActivity.isWkOrNot;

public class LoveCounter extends Activity {

    public static String beginDate = "2015-05-04";
    public static String loveDate = "2016-10-05";
    public static String comingDate = "2018-12-10";

    public static int Func;
    public static int togetherDays;
    public static int pauseDays;
    public int duration = 1;
    public long i = 0;
    public int j = 1;
    public RelativeLayout mainView;
    private TextView tv_BetogetherDays;
    private Handler handler = new Handler();
    private Runnable runnable;
    private Boolean isStop = false;

    private TextView tv_MacContent;
    private LinearLayout ll_togetherDays;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_counter);

        Bundle bundle = this.getIntent().getExtras();
        Func = bundle.getInt("Func");
        String MacContent = bundle.getString("MacContent", "");

        mainView = (RelativeLayout) findViewById(R.id.activity_love_counter);
        ll_togetherDays = (LinearLayout) findViewById(R.id.ll_togetherDays);
        tv_BetogetherDays = (TextView) findViewById(R.id.daysTextView);
        tv_MacContent = (TextView) findViewById(R.id.tv_MacContent);


        if (MacContent == "") {
            tv_MacContent.setVisibility(View.GONE);
        } else {
            tv_MacContent.setText(MacContent);
        }


        if (Func == 0) {

            if (isWkOrNot()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String nowTime = format.format(new Date());
                Date date1 = null;
                Date date2 = null;
                Date date3 = null;
                try {
                    date1 = format.parse(beginDate);
                    date2 = format.parse(loveDate);
                    date3 = format.parse(comingDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // date0 is current Date
                Date date0 = null;
                try {
                    date0 = format.parse(nowTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GregorianCalendar cal1 = new GregorianCalendar();
                GregorianCalendar cal2 = new GregorianCalendar();
                GregorianCalendar cal3 = new GregorianCalendar();
                GregorianCalendar cal0 = new GregorianCalendar();
                cal1.setTime(date1);
                cal2.setTime(date2);
                cal3.setTime(date3);
                cal0.setTime(date0);
                double daysWhenPause = (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 3600 * 24);
                double daysCount = (cal0.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 3600 * 24);
                togetherDays = (int) daysCount;
                pauseDays = (int) daysWhenPause;
            }
        } else {
            ll_togetherDays.setVisibility(View.GONE);
            if (Func == 1) {

            }
            if (Func == 2) {

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mainView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                ClipboardManager cmb = (ClipboardManager) LoveCounter.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(tv_MacContent.getText().toString().trim());
                ToastUtil.showSimpleToast(getString(R.string.copied), 100);
            }
        });


        if (Func == 0) {
            tv_BetogetherDays.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View p1) {
                    if (isWkOrNot()) {
                        isStop = !isStop;
                        handler.postDelayed(runnable, 1);
                    }
                }
            });

            runnable = new Runnable() {

                public void run() {
                    if (!isStop) {
                        this.update();
                        if (j < 500) {
                            handler.postDelayed(this, duration);//flash speed
                        }
                    }
                }

                void update() {

                    if (j < 100) {
                        duration = 1;
                        i = (long) (pauseDays / 2 * Math.sin(j * Math.PI / 100 - Math.PI / 2) + pauseDays / 2) + 1;
                    } else if (j == 100) {
                        i = pauseDays;
                        duration = 170;
                    } else if (j < 200) {
                        duration = 1;
                        i = (long) ((togetherDays - pauseDays) / 2 * Math.sin(Math.PI / (200 - 100) * (j - 100) - Math.PI / 2) + (togetherDays - pauseDays) / 2 + pauseDays + 1);
                    } else {
                        i = togetherDays;
                    }
//					if(j <200){
//						duration =1;
//						i = togetherDays / 200 * j;
//					}else{
//						i = togetherDays;
//					}
					
					
                    j++;

                    tv_BetogetherDays.setText(i + "");
                }
            };

        }

        if (isWkOrNot()) {
            handler.postDelayed(runnable, 1);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable); //停止刷新
        super.onDestroy();
    }

}
