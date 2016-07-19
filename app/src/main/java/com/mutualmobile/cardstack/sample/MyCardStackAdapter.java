package com.mutualmobile.cardstack.sample;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.mutualmobile.cardstack.CardStackAdapter;
import com.mutualmobile.cardstack.sample.interfaces.OnRestartRequest;
import com.mutualmobile.cardstack.sample.utils.Logger;
import com.tramsun.libs.prefcompat.Pref;

public class MyCardStackAdapter extends CardStackAdapter implements CompoundButton.OnCheckedChangeListener {
    //private static int[] bgColorIds;

    private static ModelA[] collect;

    private static final float SMALL_GAP = 40;
    private static final float LARGE_GAP = 100;

    private final LayoutInflater mInflater;
    private final Context mContext;
    private Logger log = new Logger(MyCardStackAdapter.class.getSimpleName());
    private OnRestartRequest mCallback;
    private Runnable updateSettingsView;

    public MyCardStackAdapter(MainActivity activity) {
        super(activity);
        mContext = activity;
        mInflater = LayoutInflater.from(activity);
        mCallback = activity;
        collect = new ModelA[]{
                new ModelA(R.color.card1_bg, SMALL_GAP),
                new ModelA(R.color.orange, LARGE_GAP),
                new ModelA(R.color.card2_bg, SMALL_GAP),
                new ModelA(R.color.dark_green, SMALL_GAP),
                new ModelA(R.color.dirty_green, SMALL_GAP),
                new ModelA(R.color.teal, LARGE_GAP),
                new ModelA(R.color.dark_blue, SMALL_GAP),
                new ModelA(R.color.indigo, SMALL_GAP),
                new ModelA(R.color.violet, SMALL_GAP),
                new ModelA(R.color.card6_bg, LARGE_GAP),
                new ModelA(R.color.card5_bg, SMALL_GAP),
                new ModelA(R.color.red, SMALL_GAP),
                new ModelA(R.color.rose, SMALL_GAP),
                new ModelA(R.color.pastel_magenta, SMALL_GAP)
        };
    }

    @Override
    public int getCount() {
        return collect.length;
    }

    public ModelA getItem(int position){
        return collect[position];
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        log.d("onCheckedChanged() called with: " + "buttonView = [" + buttonView + "], isChecked = [" + isChecked + "]");
        switch (buttonView.getId()) {
            case R.id.parallax_enabled:
                Pref.putBoolean(Prefs.PARALLAX_ENABLED, isChecked);
                break;
            case R.id.reverse_click_animation:
                Prefs.setReverseClickAnimationEnabled(isChecked);
                break;
            case R.id.show_init_animation:
                Pref.putBoolean(Prefs.SHOW_INIT_ANIMATION, isChecked);
                break;
        }
        updateSettingsView.run();
    }

    @Override
    public View createView(int position, ViewGroup container) {
        //if (position == 0) return getSettingsView(container);

        CardView root = (CardView) mInflater.inflate(R.layout.card, container, false);
        root.setCardBackgroundColor(ContextCompat.getColor(mContext, collect[position].bgColor));
        TextView cardTitle = (TextView) root.findViewById(R.id.card_title);
        cardTitle.setText(mContext.getResources().getString(R.string.card_title, position));
        return root;
    }

    @Override
    protected Animator getAnimatorForView(View view, int currentCardPosition, int selectedCardPosition) {
        if (Prefs.isReverseClickAnimationEnabled()) {

            int offsetTop = getScrollOffset();

            if (currentCardPosition > selectedCardPosition) {
                return ObjectAnimator.ofFloat(view, View.Y, view.getY(), offsetTop + getCardFinalY(currentCardPosition));
            } else {
                return ObjectAnimator.ofFloat(view, View.Y, view.getY(), offsetTop + getCardOriginalY(0) + (currentCardPosition * getCardGapBottom()));
            }
        } else {
            return super.getAnimatorForView(view, currentCardPosition, selectedCardPosition);
        }
    }

    private View getSettingsView(ViewGroup container) {
        CardView root = (CardView) mInflater.inflate(R.layout.settings_card, container, false);
        root.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.card1_bg));

        final Switch showInitAnimation = (Switch) root.findViewById(R.id.show_init_animation);
        final Switch parallaxEnabled = (Switch) root.findViewById(R.id.parallax_enabled);
        final Switch reverseClickAnimation = (Switch) root.findViewById(R.id.reverse_click_animation);
        final EditText parallaxScale = (EditText) root.findViewById(R.id.parallax_scale);
        final EditText cardGap = (EditText) root.findViewById(R.id.card_gap);
        final EditText cardGapBottom = (EditText) root.findViewById(R.id.card_gap_bottom);

        updateSettingsView = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                showInitAnimation.setChecked(Prefs.isShowInitAnimationEnabled());
                showInitAnimation.setOnCheckedChangeListener(MyCardStackAdapter.this);

                reverseClickAnimation.setChecked(Prefs.isReverseClickAnimationEnabled());
                reverseClickAnimation.setOnCheckedChangeListener(MyCardStackAdapter.this);

                boolean isParallaxEnabled = Prefs.isParallaxEnabled();
                parallaxEnabled.setChecked(isParallaxEnabled);
                parallaxEnabled.setOnCheckedChangeListener(MyCardStackAdapter.this);

                parallaxScale.setText("" + Prefs.getParallaxScale(mContext));
                parallaxScale.setEnabled(isParallaxEnabled);

                cardGap.setText("" + Prefs.getCardGap(mContext));

                cardGapBottom.setText("" + Prefs.getCardGapBottom(mContext));
            }
        };

        updateSettingsView.run();

        Button restartActivityButton = (Button) root.findViewById(R.id.restart_activity);
        restartActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePrefsIfRequired(parallaxScale);
                updatePrefsIfRequired(cardGap);
                updatePrefsIfRequired(cardGapBottom);
                mCallback.requestRestart();
            }
        });

        Button resetDefaultsButton = (Button) root.findViewById(R.id.reset_defaults);
        resetDefaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.resetDefaults(mContext);
                updateSettingsView.run();
            }
        });

        return root;
    }

    private void updatePrefsIfRequired(EditText view) {
        String text = view.getText().toString();
        int value;

        try {
            value = Integer.parseInt(text);
        } catch (Exception e) {
            value = Integer.MIN_VALUE;
        }
        if (value == Integer.MIN_VALUE) {
            log.e("Invalid value for " + view.getResources().getResourceName(view.getId()));
            return;
        }

        switch (view.getId()) {
            case R.id.parallax_scale:
                log.d("parallax_scale now is " + Integer.parseInt(text));
                Pref.putInt(Prefs.PARALLAX_SCALE, Integer.parseInt(text));
                break;
            case R.id.card_gap:
                log.d("card_gap now is " + Integer.parseInt(text));
                Pref.putInt(Prefs.CARD_GAP, Integer.parseInt(text));
                break;
            case R.id.card_gap_bottom:
                log.d("card_gap_bottom now is " + Integer.parseInt(text));
                Pref.putInt(Prefs.CARD_GAP_BOTTOM, Integer.parseInt(text));
                break;
        }
    }

    @Override
    public float getTotalHeight(){
        float height = 0;
        for(int i=0; i <collect.length; i++){
            height += collect[i].spacing;
        }
        return height;
    }

    private float getHeightUntil(int position){
        float height = 0;
        for(int i=0; i<position; i++){
            height += collect[i].spacing;
        }
        return height;
    }

    @Override
    protected float getCardOriginalY(int position) {
        return mParentPaddingTop + getHeightUntil(position);
    }
}
