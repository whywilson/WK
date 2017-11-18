package cc.yuyeye.wk.Activity;

import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import cc.yuyeye.wk.R;

public class CanteenIntroActivity extends IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullscreen(false);
        setButtonBackVisible(false);
        setButtonNextVisible(false);
        setButtonCtaVisible(false);
        TypefaceSpan labelSpan = new TypefaceSpan(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "sans-serif-medium" : "sans serif");
        SpannableString label = SpannableString
                .valueOf(getString(R.string.label_button_cta_canteen_intro));
        label.setSpan(labelSpan, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setButtonCtaLabel(label);

        setPageScrollDuration(1500);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPageScrollInterpolator(android.R.interpolator.fast_out_slow_in);
        }

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_canteen_intro1)
                .description(R.string.description_canteen_intro1)
                .image(R.drawable.intro_scene_01_dynamic)
                .background(R.color.colorScene01)
                .backgroundDark(R.color.colorScene01)
                .layout(R.layout.slide_canteen_1)
                .build());

        addSlide(new SimpleSlide.Builder()
                .background(R.color.colorScene02)
                .backgroundDark(R.color.colorScene02)
                .layout(R.layout.slide_canteen_2)
                .build());

        addSlide(new SimpleSlide.Builder()
                .description("Send me a fafa.")
                .image(R.drawable.intro_scene_03_dynamic)
                .background(R.color.colorScene03)
                .backgroundDark(R.color.colorScene03)
                .layout(R.layout.slide_canteen_3)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.love_fingers)
                .description(R.string.fingers_love)
                .image(R.drawable.intro_scene_04_dynamic)
                .background(R.color.colorScene03)
                .backgroundDark(R.color.colorScene03)
                .layout(R.layout.slide_canteen_relative)
                .build());

        autoplay(1500, INFINITE);
		super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
//        SharedPreferences.Editor editor = mainSharePre.edit();
//        editor.putBoolean(SettingUtil.WK_TIMELINE_KEY, false);
//        editor.apply();
        super.onDestroy();
    }
}
