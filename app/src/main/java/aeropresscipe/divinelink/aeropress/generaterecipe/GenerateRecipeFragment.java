package aeropresscipe.divinelink.aeropress.generaterecipe;

import android.os.Bundle;

import aeropresscipe.divinelink.aeropress.base.HomeView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import aeropresscipe.divinelink.aeropress.R;

public class GenerateRecipeFragment extends Fragment implements GenerateRecipeView {

    RecyclerView recipeRv;
    LinearLayout generateRecipeButton, timerButton;
    Button resumeBrewBtn;

    private Animation mFadeInAnimation, mAdapterAnimation;
    private GenerateRecipePresenter presenter;
    HomeView homeView;
    DiceUI diceUI;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_generate_recipe, container, false);

        homeView = (HomeView) getArguments().getSerializable("home_view");
        recipeRv = v.findViewById(R.id.recipe_rv);
        generateRecipeButton = v.findViewById(R.id.generateRecipeButton);
        timerButton = v.findViewById(R.id.startTimerButton);
        resumeBrewBtn = v.findViewById(R.id.resumeBrewButton);


        generateRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.getNewRecipe(getContext(), false);
            }
        });

        generateRecipeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                presenter.getNewRecipe(getContext(), true);
                return true;
            }
        });

        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diceUI.setNewRecipe(true);
                homeView.startTimerActivity(diceUI);
            }
        });

        resumeBrewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diceUI.setNewRecipe(false);
                homeView.startTimerActivity(diceUI);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recipeRv.setLayoutManager(layoutManager);

        presenter = new GenerateRecipePresenterImpl(this);
        presenter.getRecipe(getContext());
        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.initiliaze_animation);


        return v;
    }

    public static GenerateRecipeFragment newInstance(HomeView homeView) {

        Bundle args = new Bundle();
        GenerateRecipeFragment fragment = new GenerateRecipeFragment();
        args.putSerializable("home_view", homeView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void showRecipe(final DiceDomain randomRecipe) {

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                final GenerateRecipeRvAdapter recipeRvAdapter = new GenerateRecipeRvAdapter(randomRecipe, getActivity());

                @Override
                public void run() {
                    recipeRv.setAdapter(recipeRvAdapter);
                    if (!mFadeInAnimation.hasEnded()) {
                        try { //FIXME Maybe this isn't the best solution. Basically, there's a problem where if you're using Fragment Transition animation, and you also want to load the following animation on the button, app crashes.
                            //So I used this try catch method and app waits until UI loads.
                            //This happens because, getContext() is null since there are parts of the lifecycle where this will return null.
                            mFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_out);
                        } catch (Exception ignore) {
                        }
                        resumeBrewBtn.startAnimation(mFadeInAnimation);
                    }
                }
            });
        }
    }

    @Override
    public void passData(int bloomTime, int brewTime, int bloomWater, int remainingBrewWater) {
        // Set bloom time and brewtime. Needed for Timer
        diceUI = new DiceUI(bloomTime, brewTime, bloomWater, remainingBrewWater);
    }

    @Override
    public void showIsAlreadyBrewingDialog() {
        Toast.makeText(getActivity(), R.string.alreadyBrewingDialog, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRecipeRemoveResume(final DiceDomain randomRecipe) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                final GenerateRecipeRvAdapter recipeRvAdapter = new GenerateRecipeRvAdapter(randomRecipe, getActivity());

                @Override
                public void run() {

                    mAdapterAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.adapter_anim);
                    recipeRv.startAnimation(mAdapterAnimation);
                    // We need this so the adapter changes during the animation phase, and not before it.
                    Handler adapterHandler = new Handler(Looper.getMainLooper());
                    Runnable adapterRunnable = new Runnable() {
                        @Override
                        public void run() {
                            recipeRv.setAdapter(recipeRvAdapter);
                        }
                    };

                    adapterHandler.postDelayed(adapterRunnable, mAdapterAnimation.getDuration());

                    if (!mFadeInAnimation.hasEnded()) {
                        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                        resumeBrewBtn.startAnimation(mFadeInAnimation);
                    }

                }
            });
        }
    }

    @Override
    public void showRecipeAppStarts(final DiceDomain randomRecipe) {

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                final GenerateRecipeRvAdapter recipeRvAdapter = new GenerateRecipeRvAdapter(randomRecipe, getActivity());

                @Override
                public void run() {
                    recipeRv.setAdapter(recipeRvAdapter);
                    if (mFadeInAnimation == null)
                        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.initiliaze_animation);
                    resumeBrewBtn.startAnimation(mFadeInAnimation);
                }
            });
        }
    }
}