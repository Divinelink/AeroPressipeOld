package aeropresscipe.divinelink.aeropress.timer;

import aeropresscipe.divinelink.aeropress.R;
import aeropresscipe.divinelink.aeropress.generaterecipe.DiceUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;


public class TimerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        Toolbar mToolBar = findViewById(R.id.toolbar);
        mToolBar.setNavigationOnClickListener(v1 -> onBackPressed());

        DiceUI diceUI = (DiceUI) getIntent().getSerializableExtra("timer");

        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.timerRoot, TimerFragment.newInstance(diceUI))
                .commit();
    }


}