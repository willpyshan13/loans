
package com.will.loans.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.will.loans.R;
import com.will.loans.beans.bean.BannerItem;
import com.will.loans.constant.ServerInfo;
import com.will.loans.utils.ScreenProperties;

import java.util.LinkedList;
import java.util.List;

public class Home extends BaseFragment implements OnClickListener {
    private ViewPager viewPager;

    private RadioGroup groupPoint;

    private List<BannerItem> wheel;

    private int currentPage;

    private AQuery aq;

    private final int MESSAGE_WHAT = 0;

    private final long FLING_PAGE_INTERVAL = 3000;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (currentPage == wheel.size() + 2) {
                currentPage = 2;
            }
            viewPager.setCurrentItem(currentPage);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return inflater.inflate(R.layout.fragment_home, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        aq = new AQuery(getActivity(), view);
        setTitleText(view, R.string.tab_mine, R.string.refresh, R.string.tab_home);
        setTitleVisible(view, View.VISIBLE, View.VISIBLE, View.VISIBLE);
        ((Button) view.findViewById(R.id.title_btn_right)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.title_btn_left)).setOnClickListener(this);

        groupPoint = (RadioGroup) view.findViewById(R.id.rg_points);

        viewPager = (ViewPager) view.findViewById(R.id.vp_ads);

        initViewPager();
    }

    private void initViewPager() {
        viewPager = (ViewPager) getView().findViewById(R.id.vp_ads);

        initViewPagerBound();

        if (wheel == null) {
            return;
        }

        LinkedList<View> pageViews = new LinkedList<View>();
        addPageViews(pageViews);
        addPointView(pageViews.size() - 2);

        viewPager.setAdapter(new WheelPagerAdapter(getActivity(), pageViews, wheel));
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                //                renderBrief(arg0);
                adjustWheel(arg0);
                adjustPoint(arg0);
                sendMessageForNextPage(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        viewPager.setCurrentItem(1);

        currentPage = 2;
        handler.sendEmptyMessageDelayed(MESSAGE_WHAT, FLING_PAGE_INTERVAL);

    }

    private void initViewPagerBound() {
        // 先算出图片长在屏幕中占多少英寸
        float xInch = (ScreenProperties.getScreenWidth() / ScreenProperties.getXdpi());
        //      Log.e("xInch", xInch + "Inch");
        // 根据图片宽长比例算出高应该占多少英寸
        double yInch = xInch * (15 / 32.0);
        //      Log.e("yInch", yInch + "Inch");
        // 再根据Y轴方向上每英寸多少像素算出图片高应该有多少像素
        int viewPagerHeight = (int) (ScreenProperties.getXdpi() * yInch);
        //      Log.e("viewPagerWidth", ScreenProperties.getScreenWidth() + "");
        //      Log.e("viewPagerHeight", viewPagerHeight + "");
        RelativeLayout viewPagerBound = (RelativeLayout) getView()
                .findViewById(R.id.rlyt_viewpager);
        viewPagerBound.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                viewPagerHeight));
    }

    private void addPageViews(LinkedList<View> pageViews) {
        for (BannerItem videoItem : wheel) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ScaleType.CENTER_CROP);
            aq.id(imageView).image(ServerInfo.IMAGE_PREFIX + videoItem.url, true, true);
            pageViews.add(imageView);
        }

        for (int i = 0; i < wheel.size(); i++) {
            if (i == 0) {
                ImageView imageView = extraImageView(i);
                pageViews.addLast(imageView);
            }

            if (i == wheel.size() - 1) {
                ImageView imageView = extraImageView(i);
                pageViews.addFirst(imageView);
            }
        }
    }

    private ImageView extraImageView(int i) {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ScaleType.CENTER_CROP);
        aq.id(imageView).image(ServerInfo.IMAGE_PREFIX + wheel.get(i).url, true, true);

        return imageView;

    }

    private void addPointView(int count) {
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                RadioButton radBtn = new RadioButton(getActivity());
                radBtn.setClickable(false);
                int width = (int) ScreenProperties.getScreenDensity() * 25;
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(width,
                        LayoutParams.WRAP_CONTENT);
                radBtn.setButtonDrawable(R.drawable.page_control_sel);
                groupPoint.addView(radBtn, params);
            }
        }

        View v = groupPoint.getChildAt(0);
        RadioButton radioBtn = (RadioButton) v;
        radioBtn.setChecked(true);
    }

    private void adjustWheel(int currentPage) {
        if (currentPage == wheel.size() + 1) {
            viewPager.setCurrentItem(1, false);
        }

        if (currentPage == 0) {
            viewPager.setCurrentItem(wheel.size(), false);
        }
    }

    private void adjustPoint(int pageIndex) {
        if (pageIndex == 0 || pageIndex == wheel.size() + 1) {
            return;
        }

        View child = groupPoint.getChildAt(pageIndex - 1);
        if (child instanceof RadioButton) {
            RadioButton radBtn = (RadioButton) child;
            radBtn.setChecked(true);
        }
    }

    private void sendMessageForNextPage(int nowPage) {
        if (handler.hasMessages(MESSAGE_WHAT)) {
            handler.removeMessages(MESSAGE_WHAT);
        }

        currentPage = nowPage + 1;
        handler.sendEmptyMessageDelayed(MESSAGE_WHAT, FLING_PAGE_INTERVAL);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }
}
