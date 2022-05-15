package aeropresscipe.divinelink.aeropress.base;

import aeropresscipe.divinelink.aeropress.generaterecipe.DiceUI;
import aeropresscipe.divinelink.aeropress.history.HistoryFragment;
import aeropresscipe.divinelink.aeropress.savedrecipes.SavedRecipesFragment;
import aeropresscipe.divinelink.aeropress.timer.TimerActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


import aeropresscipe.divinelink.aeropress.R;
import aeropresscipe.divinelink.aeropress.generaterecipe.GenerateRecipeFragment;
import androidx.fragment.app.FragmentTransaction;



public class HomeActivity extends AppCompatActivity implements HomeView, Parcelable {
    //FIXME make sure Parcelable is absolutely necessary, otherwise remove it and try to fix the bug it creates where app crashes when closing the app.
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnItemSelectedListener(onItemSelectedListener);
        mBottomNavigationView.setOnItemReselectedListener(onItemReselectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.homeRoot, GenerateRecipeFragment.newInstance(this))
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // When leaving from Timer Activity, set bottomNavigation to be the recipe button
        // and restart the fragment, so we can see the resume button flashing.
        mBottomNavigationView.setSelectedItemId(R.id.recipe);
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .replace(R.id.homeRoot, GenerateRecipeFragment.newInstance(this))
                .commit();
    }

    @Override
    public void startTimerActivity(DiceUI diceUI) {
        Intent timerIntent = new Intent(this, TimerActivity.class);
        timerIntent.putExtra("timer", diceUI);
        startActivity(timerIntent);
    }

    @Override
    public void addGenerateRecipeFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.homeRoot, GenerateRecipeFragment.newInstance(this))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void addSavedRecipesFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.homeRoot, SavedRecipesFragment.newInstance(this))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void addHistoryFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.homeRoot, HistoryFragment.newInstance(this))
                .addToBackStack(null)
                .commit();
    }

    NavigationBarView.OnItemSelectedListener onItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.recipe:
                        addGenerateRecipeFragment();
                        return true;
                    case R.id.favourites:
                        addSavedRecipesFragment();
                        return true;
                    case R.id.history:
                        addHistoryFragment();
                        return true;
                }
                return false;
            };

    NavigationBarView.OnItemReselectedListener onItemReselectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.recipe:
                // do nothing
            case R.id.favourites:
                // do nothing
            case R.id.history:
                // do nothing
        }
    };


    @Override
    public void onBackPressed() {
        if (mBottomNavigationView.getSelectedItemId() == R.id.recipe) {
            super.onBackPressed();
            finish();
        } else {
            mBottomNavigationView.setSelectedItemId(R.id.recipe);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        // do nothing
    }
}
