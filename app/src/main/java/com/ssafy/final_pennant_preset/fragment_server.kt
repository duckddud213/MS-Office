package com.ssafy.final_pennant_preset

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentPlaylistBinding
import com.ssafy.final_pennant.databinding.FragmentServerBinding
import com.ssafy.final_pennant.databinding.FragmentSongBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import java.util.concurrent.TimeUnit


private const val TAG = "fragment_server_싸피"
class fragment_server : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val binding: FragmentServerBinding
        get() = _binding!!

    val musicviewmodel: MusicFileViewModel by activityViewModels()

    //=======================================
    private lateinit var player: ExoPlayer
    var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val updateSeekRunnable = Runnable {
        savePlayingState()
    }
    //=======================================

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

    override fun onDetach() {
        super.onDetach()
        //프래그먼트간 화면 이동 시 음악 재생 진행률 정보 전달
        player.stop()
        player.release()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player = ExoPlayer.Builder(requireContext()).build()
        //======================================
        if (musicviewmodel.isPlaying) {
            //음악 재생 중에 넘어온 경우

            musicviewmodel.playerNotificationManager =
                PlayerNotificationManager.Builder(requireActivity(), 5, "MS Office")
                    .setNotificationListener(object :
                        PlayerNotificationManager.NotificationListener {
                        override fun onNotificationPosted(
                            notificationId: Int,
                            notification: Notification,
                            ongoing: Boolean
                        ) {
                            super.onNotificationPosted(notificationId, notification, ongoing)
                            if (ongoing) {
                                Log.d(TAG, "onNotificationPosted: 재생 중이다")
                                Log.d(TAG, "onNotificationPosted: ${notification.actions}")
                            } else {
                                Log.d(TAG, "onNotificationPosted: 멈췄다")
                            }
                        }
                    })
                    .setChannelImportance(NotificationManager.IMPORTANCE_HIGH)
                    .setSmallIconResourceId(R.drawable.music_ssafy_office)
                    .setChannelDescriptionResourceId(R.string.app_name)
                    .setPreviousActionIconResourceId(R.drawable.img_skipprevious)
                    .setPauseActionIconResourceId(R.drawable.img_pause)
                    .setPlayActionIconResourceId(R.drawable.img_play)
                    .setNextActionIconResourceId(R.drawable.img_skipnext)
                    .setChannelNameResourceId(R.string.app_name)
                    .build()

            musicviewmodel.playerNotificationManager.setPlayer(player)

            var mediaItem = MediaItem.fromUri("${uri}/${musicviewmodel.selectedMusic.id}")
            player.setMediaItem(mediaItem, musicviewmodel.isPlayingOn)
            player.prepare()
            player.play()

            savePlayingState()
        }

        //=======================================

        binding.fabPushManage.setOnClickListener {
            showDialogPush()
        }

        initGenreList()
    }

    private fun initGenreList() {
        val genreList = mutableListOf(
            ApplicationClass.CHANNEL_DANCE
            , ApplicationClass.CHANNEL_BALLAD
            , ApplicationClass.CHANNEL_ROCK
            , ApplicationClass.CHANNEL_POP
            , ApplicationClass.CHANNEL_IDOL
        )

        val genreListAdapter = GenreListAdapter(genreList)
        genreListAdapter.myItemClickListener = object : GenreListAdapter.ItemClickListener{
            override fun onMyClick(view: View, data: String, position: Int) {
                val genreFrag = fragment_server_genre
                parentFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.framecontainer, genreFrag.newInstance(data))
                    .commit()
            }
        }

        binding.rvServerGenre.apply {
            adapter = genreListAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration(requireContext()))
        }
    }

    //=======================================
    fun savePlayingState() {
        var duration = player.duration
        var position = player.currentPosition

        var posStr = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS), // 현재 분
            (position / 1000) % 60 // 분 단위를 제외한 현재 초
        )
        var durStr = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS), // 전체 분
            (duration / 1000) % 60 // 분 단위를 제외한 초
        )

        if (posStr.equals(durStr) && !posStr.equals("00:00")) {
            musicviewmodel.selectedMusicPosition = musicviewmodel.selectedMusicPosition + 1
            musicviewmodel.selectedMusicPosition =
                musicviewmodel.selectedMusicPosition % musicviewmodel.selectedPlayList.songlist.size

            musicviewmodel.selectedMusic =
                musicviewmodel.selectedPlayList.songlist[musicviewmodel.selectedMusicPosition]

            ApplicationClass.sSharedPreferences.putSelectedSongPosition(musicviewmodel.selectedMusicPosition)

            var mediaItem = MediaItem.fromUri("${uri}/${musicviewmodel.selectedMusic.id}")
            musicviewmodel.isPlayingOn = 0
            player.setMediaItem(mediaItem, musicviewmodel.isPlayingOn)
            player.prepare()
            player.play()

        }

        musicviewmodel.isPlayingOn = player.currentPosition + 1000
        Log.d(TAG, "savePlayingState: 진짜 바뀌나? : ${musicviewmodel.isPlayingOn}")

        if (player.playbackState != Player.STATE_IDLE && player.playbackState != Player.STATE_ENDED) {
            view?.postDelayed(updateSeekRunnable, 1000) // 1초에 한번씩 실행
        }
    }
    //=======================================

    class GenreListAdapter(val genreList: MutableList<String>) :

        RecyclerView.Adapter<GenreListAdapter.CustomViewHolder>() {
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnCreateContextMenuListener {

            var tv_genre = itemView.findViewById<TextView>(R.id.tvPlayListTitle)

            fun bind(genre: String) {
                tv_genre.text = genre
                tv_genre.setOnClickListener {
                    myItemClickListener.onMyClick(it, genre, layoutPosition)
                }
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
            }
        }

        interface ItemClickListener{
            fun onMyClick(view: View, data: String, position: Int)
        }

        lateinit var myItemClickListener: ItemClickListener
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.totalplaylist_item, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.bind(genreList.get(position))
        }

        override fun getItemCount(): Int {
            return genreList.size
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
            setPositiveButton("확인") { _, _ ->
            }
        }

        builder.create().show()
    }
}
