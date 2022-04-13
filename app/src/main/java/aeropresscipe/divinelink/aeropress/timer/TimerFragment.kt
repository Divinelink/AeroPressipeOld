package aeropresscipe.divinelink.aeropress.timer

import aeropresscipe.divinelink.aeropress.generaterecipe.DiceUI
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import aeropresscipe.divinelink.aeropress.R
import aeropresscipe.divinelink.aeropress.databinding.FragmentTimerBinding
import android.view.MotionEvent
import android.animation.AnimatorSet
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.preference.PreferenceManager
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment

class TimerFragment : Fragment(), TimerView {
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding

    private var presenter: TimerPresenter? = null
    private var diceUI: DiceUI? = null

    private var secondsRemaining = 0

    private val getPhaseFactory = GetPhaseFactory()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        diceUI = arguments?.getParcelable("timer")

        initListeners()

        presenter = TimerPresenterImpl(this)
        return binding?.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {

        binding?.likeRecipeButton?.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val reducer = AnimatorInflater.loadAnimator(context, R.animator.reduce_size) as AnimatorSet
                reducer.setTarget(view)
                reducer.start()
            } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                val regainer = AnimatorInflater.loadAnimator(context, R.animator.regain_size) as AnimatorSet
                regainer.setTarget(view)
                regainer.start()
            }
            false
        }

        binding?.likeRecipeButton?.setOnClickListener {
            presenter?.saveLikedRecipeOnDB(context)
        }
    }

    override fun showTimer(time: Int, bloomPhase: Boolean) {
        secondsRemaining = time
        if (bloomPhase) {
            timerHandler.postDelayed(bloomRunnable, 1000)
            updateCountdownUI()
            binding?.notificationTextView?.text = String.format("%s\n%s", getString(R.string.bloomPhase), getString(R.string.bloomPhaseWaterText, diceUI?.bloomWater))
            diceUI?.setRecipeHadBloom(true)
        } else {
            timerHandler.postDelayed(brewRunnable, 1000)
            // Checks if there was a bloom or not, and set corresponding text on textView.
            updateCountdownUI()
            if (getPhaseFactory.findPhase(diceUI?.bloomTime, diceUI?.brewTime)?.phase == true || diceUI?.recipeHadBloom() == true
            ) {
                binding?.notificationTextView?.text = String.format("%s\n%s", getString(R.string.brewPhase), getString(R.string.brewPhaseWithBloom, diceUI?.remainingBrewWater))
            } else {
                binding?.notificationTextView?.text = String.format("%s\n%s", getString(R.string.brewPhase), getString(R.string.brewPhaseNoBloom, diceUI?.remainingBrewWater))
            }
            diceUI?.bloomTime = 0
        }
        // Set max progress bar to be either the max BloomTime or BrewTime.
        // Avoid if statements by using Factory
        binding?.progressBar?.max = getPhaseFactory.getMaxTime(diceUI?.bloomTime, diceUI?.brewTime)
        ObjectAnimator.ofInt(binding?.progressBar, "progress", time)
            .setDuration(300)
            .start()
    }

    var timerHandler = Handler()
    private var bloomRunnable: Runnable = object : Runnable {
        override fun run() {
            if (secondsRemaining == 1 || secondsRemaining == 0) {
                presenter?.startBrewing(getPhaseFactory.findPhase(0, diceUI?.brewTime)?.time ?: 0, false, context)
                timerHandler.removeCallbacks(this)
            } else {
                timerHandler.postDelayed(this, 1000)
                secondsRemaining -= 1
                updateCountdownUI()
            }
        }
    }
    var brewRunnable: Runnable = object : Runnable {
        override fun run() {
            if (secondsRemaining == 1 || secondsRemaining == 0) {
                timerHandler.removeCallbacks(this)
                //TODO ADD ANIMATION
                presenter?.showMessage()
            } else {
                timerHandler.postDelayed(this, 1000)
                secondsRemaining -= 1
                updateCountdownUI()
            }
        }
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        binding?.timerTextView?.text = String.format("%d:%02d", minutesUntilFinished, secondsInMinuteUntilFinished)
        binding?.progressBar?.progress = secondsInMinuteUntilFinished + minutesUntilFinished * 60
    }

    override fun onPause() {
        // Use OnPause instead of OnStop, because onStop is called after we go back to HomeActivity,
        // and in this case we don't get the isBrewing boolean in time
        super.onPause()
        diceUI?.isNewRecipe = false
        if (getPhaseFactory.findPhase(diceUI?.bloomTime, diceUI?.brewTime)?.time != 0) {
            val isBloomPhase = getPhaseFactory.findPhase(diceUI?.bloomTime, diceUI?.brewTime)?.phase
            timerHandler.removeCallbacks(bloomRunnable)
            timerHandler.removeCallbacks(brewRunnable)

            if (isBloomPhase != null) {
                presenter?.saveValuesOnPause(context, secondsRemaining, diceUI?.brewTime ?: 0, isBloomPhase)
            }
        } else {
            // When leaving Timer and it is over, set isBrewing boolean to false, meaning that brewing process is over
            // which removes the resume button on Generate Recipe Fragment.
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.putBoolean("isBrewing", false)
            editor.apply()
        }
    }

    override fun onResume() {
        super.onResume()
        // if resuming from recipe without bloom isNewRecipe == false
        if (diceUI?.isNewRecipe == true) {// if it's a new recipe, don't call returnValuesOnResume
            presenter?.startBrewing(
                getPhaseFactory.findPhase(diceUI?.bloomTime, diceUI?.brewTime)?.time ?: 0,
                getPhaseFactory.findPhase(diceUI?.bloomTime, diceUI?.brewTime)?.phase ?: false,
                context)
        } else {  //When resuming, we need to pass the old recipe, not the new one.
            presenter?.returnValuesOnResume(context)
        }
    }

    override fun showMessage() {
        //TODO make it show a button that returns to starting screen
        binding?.timerTextView?.visibility = View.INVISIBLE
        binding?.progressBar?.visibility = View.INVISIBLE
        val mFadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in_timer)
        binding?.notificationTextView?.startAnimation(mFadeInAnimation)
        binding?.notificationTextView?.setText(R.string.enjoyCoffee)
        startMoveAnimation(binding?.notificationTextView, 300f)
        startMoveAnimation(binding?.likeRecipeLayout, -300f)


        // Temporary Fix?
        diceUI?.bloomTime = 0
        diceUI?.brewTime = 0
    }

    override fun addToLiked(isLiked: Boolean) {
            activity?.runOnUiThread {
                if (isLiked) {
                    binding?.likeRecipeButton?.setImageResource(R.drawable.ic_heart_on)
                } else {
                    binding?.likeRecipeButton?.setImageResource(R.drawable.ic_heart_off)
                }
            }
        }
    }

    private fun startMoveAnimation(view: View?, value: Float) {
        val animation = ObjectAnimator.ofFloat(view, "translationY", value)
        animation.duration = 1000
        animation.start()
    }


//companion object {
//    @JvmStatic
//    fun newInstance() =
//        TimerFragment().apply {
//            arguments = Bundle().apply {
//                // no arguments yet
//            }
//        }
companion object {
    @JvmStatic
    fun newInstance(diceUI: DiceUI?): TimerFragment {
        val fragment = TimerFragment()
        val args = Bundle()
        args.putParcelable("timer", diceUI)
        fragment.arguments = args
        return fragment
    }
}
}
