package aeropresscipe.divinelink.aeropress.savedrecipes;

import android.os.Bundle;


import aeropresscipe.divinelink.aeropress.base.HomeView;
import aeropresscipe.divinelink.aeropress.generaterecipe.DiceUI;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import java.util.List;

import aeropresscipe.divinelink.aeropress.R;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class SavedRecipesFragment extends Fragment implements SavedRecipesView {


    private SavedRecipesPresenter presenter;
    private HomeView homeView;

    private RecyclerView savedRecipesRV;
    private Toolbar mToolBar;
    private LinearLayout mEmptyListLL;

    private Animation mFadeAnimation;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_saved_recipes, container, false);

        homeView = (HomeView) getArguments().getSerializable("home_view");

        savedRecipesRV = (RecyclerView) v.findViewById(R.id.savedRecipesRV);
        mToolBar = (Toolbar) v.findViewById(R.id.toolbar);
        mEmptyListLL = (LinearLayout) v.findViewById(R.id.emptyListLayout);

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        savedRecipesRV.setLayoutManager(layoutManager);

        presenter = new SavedRecipesPresenterImpl(this);

        presenter.getSavedRecipes(getContext());
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static SavedRecipesFragment newInstance(HomeView homeView) {

        SavedRecipesFragment fragment = new SavedRecipesFragment();
        Bundle args = new Bundle();
        args.putSerializable("home_view", homeView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void showSavedRecipes(final List<SavedRecipeDomain> savedRecipes) {

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                SavedRecipesRvAdapter savedRecipesRvAdapter = new SavedRecipesRvAdapter(savedRecipes, getActivity(), savedRecipesRV);

                @Override
                public void run() {
                    mFadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_favourites);
                    savedRecipesRV.setAnimation(mFadeAnimation);
                    savedRecipesRV.setAdapter(savedRecipesRvAdapter);
                    savedRecipesRvAdapter.createSwipeHelper();
                    savedRecipesRvAdapter.setPresenter(presenter);
                }
            });
        }
    }

    @Override
    public void showSavedRecipesAfterDeletion(final List<SavedRecipeDomain> savedRecipes, final int position) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });

        }
    }

    @Override
    public void passData(final int bloomTime, final int brewTime, final int bloomWater, final int brewWater) {

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DiceUI diceUI = new DiceUI(bloomTime, brewTime, bloomWater, brewWater);
                    diceUI.setNewRecipe(true);
                    homeView.addTimerFragment(diceUI);
                }
            });
        }
    }

    @Override
    public void showEmptyListMessage() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mFadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_favourites);
                    savedRecipesRV.startAnimation(mFadeAnimation);
                    mFadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_favourites);
                    mEmptyListLL.setAnimation(mFadeAnimation);

                    savedRecipesRV.setVisibility(View.GONE);
                    mEmptyListLL.setVisibility(LinearLayout.VISIBLE);
                }
            });
        }
    }
}