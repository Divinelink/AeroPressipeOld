package aeropresscipe.divinelink.aeropress.timer;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import aeropresscipe.divinelink.aeropress.generaterecipe.DiceUI;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import aeropresscipe.divinelink.aeropress.R;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;


public class TimerFragment extends Fragment implements TimerView {

    private TextView timerTextView;
    private TextView notificationTextView;
    private LinearLayout mLikeRecipeLayout;
    private MaterialProgressBar progressBar;
    private ImageButton likeRecipeBtn;

    private int secondsRemaining = 0;

    private DiceUI diceUI;
    private TimerPresenter presenter;

    private final GetPhaseFactory getPhaseFactory = new GetPhaseFactory();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timer, container, false);

        if (getArguments() != null) {
            diceUI = (DiceUI) getArguments().getSerializable("timer");
        }

        presenter = new TimerPresenterImpl(this);
        initViews(v);
        initListeners();
        return v;
    }


    @Override
    public void showTimer(final int time, boolean bloomPhase) {
        secondsRemaining = time;

        if (bloomPhase) {
            timerHandler.postDelayed(bloomRunnable, 1000);
            updateCountdownUI();
            notificationTextView.setText(String.format("%s\n%s", getString(R.string.bloomPhase), getString(R.string.bloomPhaseWaterText, diceUI.getBloomWater())));
            diceUI.setRecipeHadBloom(true);
        } else {
            timerHandler.postDelayed(brewRunnable, 1000);
            // Checks if there was a bloom or not, and set corresponding text on textView.
            updateCountdownUI();
            if (getPhaseFactory.findPhase(diceUI.getBloomTime(), diceUI.getBrewTime()).getPhase() || diceUI.recipeHadBloom()) {
                notificationTextView.setText(String.format("%s\n%s", getString(R.string.brewPhase), getString(R.string.brewPhaseWithBloom, diceUI.getRemainingBrewWater())));
            } else
                notificationTextView.setText(String.format("%s\n%s", getString(R.string.brewPhase), getString(R.string.brewPhaseNoBloom, diceUI.getRemainingBrewWater())));
            diceUI.setBloomTime(0);
        }
        // Set max progress bar to be either the max BloomTime or BrewTime.
        // Avoid if statements by using Factory
        progressBar.setMax(getPhaseFactory.getMaxTime(diceUI.getBloomTime(), diceUI.getBrewTime()));

        ObjectAnimator.ofInt(progressBar, "progress", time)
                .setDuration(300)
                .start();
    }

    Handler timerHandler = new Handler(Looper.getMainLooper());
    Runnable bloomRunnable = new Runnable() {
        @Override
        public void run() {
            if (secondsRemaining == 1 || secondsRemaining == 0) {
                presenter.startBrewing(
                        getPhaseFactory.findPhase(0, diceUI.getBrewTime()).getTime(),
                        false,
                        getContext());
                timerHandler.removeCallbacks(bloomRunnable);
            } else {
                timerHandler.postDelayed(this, 1000);
                secondsRemaining -= 1;
                updateCountdownUI();
            }
        }
    };

    Runnable brewRunnable = new Runnable() {
        @Override
        public void run() {
            if (secondsRemaining == 1 || secondsRemaining == 0) {
                timerHandler.removeCallbacks(brewRunnable);
                //TODO ADD ANIMATION
                presenter.showMessage();

            } else {
                timerHandler.postDelayed(this, 1000);
                secondsRemaining -= 1;
                updateCountdownUI();
            }
        }
    };

    private void updateCountdownUI() {
        int minutesUntilFinished = secondsRemaining / 60;
        int secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60;
        timerTextView.setText(String.format("%d:%02d", minutesUntilFinished, secondsInMinuteUntilFinished));
        progressBar.setProgress(secondsInMinuteUntilFinished + minutesUntilFinished * 60);
    }

    @Override
    public void onPause() {
        // Use OnPause instead of OnStop, because onStop is called after we go back to HomeActivity,
        // and in this case we don't get the isBrewing boolean in time
        super.onPause();

        diceUI.setNewRecipe(false);
        if (getPhaseFactory.findPhase(diceUI.getBloomTime(), diceUI.getBrewTime()).getTime() != 0) {
            final boolean isBloomPhase = getPhaseFactory.findPhase(diceUI.getBloomTime(), diceUI.getBrewTime()).getPhase();

            timerHandler.removeCallbacks(bloomRunnable);
            timerHandler.removeCallbacks(brewRunnable);


            presenter.saveValuesOnPause(getContext(), secondsRemaining, diceUI.getBrewTime(), isBloomPhase);
        } else {
            // When leaving Timer and it is over, set isBrewing boolean to false, meaning that brewing process is over
            // which removes the resume button on Generate Recipe Fragment.
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isBrewing", false);
            editor.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // if resuming from recipe without bloom isNewRecipe == false
        if (diceUI.isNewRecipe()) // if it's a new recipe, dont call returnValuesOnResume
            presenter.startBrewing(
                    getPhaseFactory.findPhase(diceUI.getBloomTime(), diceUI.getBrewTime()).getTime(),
                    getPhaseFactory.findPhase(diceUI.getBloomTime(), diceUI.getBrewTime()).getPhase(),
                    getContext());
        else
            //When resuming, we need to pass the old recipe, not the new one.
            presenter.returnValuesOnResume(getContext());

    }

    @Override
    public void showMessage() {
        //TODO make it show a button that returns to starting screen
        timerTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);


        Animation mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_timer);
        notificationTextView.startAnimation(mFadeInAnimation);
        notificationTextView.setText(R.string.enjoyCoffee);


        startMoveAnimation(notificationTextView, 300f);
        startMoveAnimation(mLikeRecipeLayout, -300f);


        // Temporary Fix?
        diceUI.setBloomTime(0);
        diceUI.setBrewTime(0);

    }

    @Override
    public void addToLiked(final boolean isLiked) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isLiked)
                    likeRecipeBtn.setImageResource(R.drawable.ic_heart_on);
                else
                    likeRecipeBtn.setImageResource(R.drawable.ic_heart_off);
            });
        }
    }

    private void startMoveAnimation(View view, float value) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(view, "translationY", value);
        animation.setDuration(1000);
        animation.start();
    }

    private void initViews(View v) {
        timerTextView = (TextView) v.findViewById(R.id.savedTimeTV);
        progressBar = (MaterialProgressBar) v.findViewById(R.id.progressBar);
        notificationTextView = (TextView) v.findViewById(R.id.notificationTV);
        likeRecipeBtn = (ImageButton) v.findViewById(R.id.likeRecipeButton);
        mLikeRecipeLayout = (LinearLayout) v.findViewById(R.id.likeRecipeLayout);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        likeRecipeBtn.setOnClickListener(view -> presenter.saveLikedRecipeOnDB(getContext()));

        likeRecipeBtn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                AnimatorSet reducer = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.reduce_size);
                reducer.setTarget(view);
                reducer.start();

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                AnimatorSet regainer = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.regain_size);
                regainer.setTarget(view);
                regainer.start();
            }
            return false;
        });
    }

    public static TimerFragment newInstance(DiceUI diceUI) {

        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putSerializable("timer", diceUI);
        fragment.setArguments(args);
        return fragment;
    }
}
