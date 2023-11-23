package com.ssafy.final_pennant_preset

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentServerGenreBinding
import com.ssafy.final_pennant_preset.dto.ServerMusicDTO
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.service.MusicDownloader
import com.ssafy.final_pennant_preset.util.RetrofitUtil
import java.util.concurrent.TimeUnit

private const val GENRE = ApplicationClass.CHANNEL_POP

private const val TAG = "fragment_server_genre_싸피"
class fragment_server_genre : Fragment() {

    private var genre: String? = null
    private var _binding: FragmentServerGenreBinding? = null
    private val binding: FragmentServerGenreBinding
        get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private val serverViewModel : ServerViewModel by viewModels()
    private lateinit var serverMusic : ArrayList<ServerMusicDTO>

    private lateinit var musicListAdapter : MusicListAdapter
    private lateinit var downLoader : MusicDownloader

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
        arguments?.let {
            genre = it.getString(GENRE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServerGenreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerObserver()
        serverViewModel.getListWithGenre(genre!!)
        musicListAdapter = MusicListAdapter(arrayListOf())

        downLoader = MusicDownloader(mainActivity)

        musicListAdapter.myItemClickListener = object : ItemClickListener{
            override fun onMyClick(view: View, dto: ServerMusicDTO) {
                downLoader.downloadFile(dto.musicUrl, dto.musicName)
            }
        }

        binding.rvServerGenre.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = musicListAdapter
        }

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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        //프래그먼트간 화면 이동 시 음악 재생 진행률 정보 전달
        player.stop()
        player.release()
    }

    fun registerObserver() {
        serverViewModel.musicWithGenre.observe(viewLifecycleOwner) {
            serverMusic = it as ArrayList<ServerMusicDTO>
            Log.d(TAG, "registerObserver: $serverMusic")
            binding.rvServerGenre.apply {
                musicListAdapter.genreList = it
                adapter = musicListAdapter
                musicListAdapter.notifyDataSetChanged()
            }
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

        musicviewmodel.isPlayingOn = player.currentPosition
        Log.d(TAG, "savePlayingState: 진짜 바뀌나? : ${musicviewmodel.isPlayingOn}")

        if (player.playbackState != Player.STATE_IDLE && player.playbackState != Player.STATE_ENDED) {
            view?.postDelayed(updateSeekRunnable, 1000) // 1초에 한번씩 실행
        }
    }
    //=======================================

    inner class MusicListAdapter(var genreList: MutableList<ServerMusicDTO>) :

        RecyclerView.Adapter<MusicListAdapter.CustomViewHolder>() {
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnCreateContextMenuListener {

            var tv_genre = itemView.findViewById<TextView>(R.id.tvPlayListTitle)
            lateinit var serverMusic: ServerMusicDTO
            val uid = ApplicationClass.sSharedPreferences.getUID()!!

            fun bind(music: ServerMusicDTO) {
                serverMusic = music
                tv_genre.text = music.toString()
                tv_genre.setOnClickListener {
                    myItemClickListener.onMyClick(it, music)
                }
                tv_genre.setOnCreateContextMenuListener(this)
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                requireActivity().apply {
                    if (uid == serverMusic.uploadUser) {
                        menuInflater.inflate(R.menu.server_item_remove, menu)
                        menu?.findItem(R.id.server_rmItem)
                            ?.setOnMenuItemClickListener {
                            serverViewModel.deleteMusic(genre!!, serverMusic.musicId)
                            musicListAdapter.genreList.remove(serverMusic)
                            true
                        }
                    }
                }
            }
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


    interface ItemClickListener{
        fun onMyClick(view: View, dto: ServerMusicDTO)
    }

    companion object {
        @JvmStatic
        fun newInstance(genre: String) =
            fragment_server_genre().apply {
                arguments = Bundle().apply {
                    putString(GENRE, genre)
                }
            }
    }
}