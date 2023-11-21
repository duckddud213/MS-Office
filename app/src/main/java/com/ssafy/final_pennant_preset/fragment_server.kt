package com.ssafy.final_pennant_preset

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentPlaylistBinding
import com.ssafy.final_pennant.databinding.FragmentServerBinding
import com.ssafy.final_pennant.databinding.FragmentSongBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass

class fragment_server : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val binding: FragmentServerBinding
        get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnConnectServer).isChecked = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyOptionsMenu() {
        _binding = null
        super.onDestroyOptionsMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabPushManage.setOnClickListener {
            showDialogPush()
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showDialogPush() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater.inflate(R.layout.dialog_pushmanage, null)

        val switch_dance = inflater.findViewById<Switch>(R.id.switch_dance)
        val switch_ballad = inflater.findViewById<Switch>(R.id.switch_ballad)
        val switch_pop = inflater.findViewById<Switch>(R.id.switch_pop)
        val switch_idol = inflater.findViewById<Switch>(R.id.switch_idol)
        val switch_rock = inflater.findViewById<Switch>(R.id.switch_rock)

        switch_dance.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_DANCE)
        switch_ballad.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_BALLAD)
        switch_pop.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_POP)
        switch_idol.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_IDOL)
        switch_rock.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_ROCK)

        switch_dance.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_DANCE, isChecked)
            ApplicationClass.receive_notification_dance = isChecked
        }

        switch_ballad.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_BALLAD, isChecked)
            ApplicationClass.receive_notification_ballad = isChecked
        }

        switch_pop.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_POP, isChecked)
            ApplicationClass.receive_notification_pop = isChecked
        }

        switch_idol.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_IDOL, isChecked)
            ApplicationClass.receive_notification_idol = isChecked
        }

        switch_rock.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_ROCK, isChecked)
            ApplicationClass.receive_notification_rock = isChecked
        }

        builder.apply {
            setView(inflater)
            setPositiveButton("확인") { dialog, _ ->
            }
        }

        builder.create().show()
    }
}