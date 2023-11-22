package com.ssafy.final_pennant_preset

import android.app.Notification
import android.app.NotificationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentAddtoplaylistBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import com.ssafy.final_pennant_preset.dto.checkboxData
import java.util.concurrent.TimeUnit

private const val TAG = "fragment_addtoplaylist_싸피"

class fragment_addtoplaylist : Fragment() {
    private var _binding: FragmentAddtoplaylistBinding? = null
    private val binding: FragmentAddtoplaylistBinding
        get() = _binding!!

    val musicfileviewmodel: MusicFileViewModel by activityViewModels()
    private val playList = mutableListOf<PlayListDTO>()

    //=======================================
    private lateinit var player: ExoPlayer
    var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val updateSeekRunnable = Runnable {
        savePlayingState()
    }
    //=======================================

    fun checkSameData(Name1: String, Name2: String): Boolean {
        if (Name1.equals(Name2)) {
            return true
        }

        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musicfileviewmodel.checkedPlayList.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddtoplaylistBinding.inflate(inflater, container, false)
        val allplaylistadapter = AllPlayListAdapter(musicfileviewmodel.playList)
        binding.lvAddPlaylist.apply {
            adapter = allplaylistadapter
            this.layoutManager = LinearLayoutManager(requireActivity())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //재생목록이 없는 상태면 Toast메시지나 Dialog로 안내하고 강제로 페이지 전환 추가

        var songInfo = musicfileviewmodel.selectedMusicToBeAdded

        binding.btnAddSongToSelectedPlaylist.setOnClickListener {
            Log.d(TAG, "onViewCreated: ${musicfileviewmodel.checkedPlayList.size}")

            for (i in 0..musicfileviewmodel.checkedPlayList.size - 1) {
                Log.d(TAG, "onViewCreated: ${i}번 데이터 => ${musicfileviewmodel.checkedPlayList[i]}")
            }

            for (i in 0..musicfileviewmodel.checkedPlayList.size - 1) {
                for (j in 0..musicfileviewmodel.playList.size - 1) {
                    //cId, cName : 체크한 항목 || pId, pName : 전체 재생 목록에 있는 항목
                    var cName = musicfileviewmodel.checkedPlayList[i].playlistname
                    var pName = musicfileviewmodel.playList[j].playlistname
                    var isDup = false

                    if (checkSameData(cName, pName)) {
                        for (k in 0..musicfileviewmodel.playList[j].songlist.size - 1) {
                            //이미 재생목록에 있는 곡일 경우 추가하지 않음
                            if (musicfileviewmodel.playList[j].songlist.get(k).equals(songInfo)) {
                                isDup = true
                            }
                        }
                        if (!isDup) {
                            musicfileviewmodel.playList[j].songlist.add(songInfo)
                        }
                        Log.d(
                            TAG,
                            "onViewCreated: ${musicfileviewmodel.playList[j].playlistname} / ${musicfileviewmodel.playList[j].songlist.size}"
                        )
                    }
                    ApplicationClass.sSharedPreferences.putSongList(
                        musicfileviewmodel.playList[j].playlistname,
                        musicfileviewmodel.playList[j].songlist
                    )
                }
            }

            player.stop()
            player.release()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.framecontainer, fragment_totallist()).commit()

            Toast.makeText(requireContext(), "재생 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show()

        }

        binding.btnGoBacks.setOnClickListener {
            player.stop()
            player.release()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.framecontainer, fragment_totallist()).commit()
        }

        //======================================
        player = ExoPlayer.Builder(requireContext()).build()
        if (musicfileviewmodel.isPlaying) {
            //음악 재생 중에 넘어온 경우

            var playerNotificationManager =
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

            playerNotificationManager.setPlayer(player)

            var mediaItem = MediaItem.fromUri("${uri}/${musicfileviewmodel.selectedMusic.id}")
            player.setMediaItem(mediaItem, musicfileviewmodel.isPlayingOn)
            player.prepare()
            player.play()

            savePlayingState()
        }

        //=======================================
    }

    override fun onDetach() {
        super.onDetach()
        //프래그먼트간 화면 이동 시 음악 재생 진행률 정보 전달
        player.stop()
        player.release()
    }

    override fun onPause() {
        super.onPause()
        player.stop()
        player.release()
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
            musicfileviewmodel.selectedMusicPosition = musicfileviewmodel.selectedMusicPosition + 1
            musicfileviewmodel.selectedMusicPosition =
                musicfileviewmodel.selectedMusicPosition % musicfileviewmodel.selectedPlayList.songlist.size

            musicfileviewmodel.selectedMusic =
                musicfileviewmodel.selectedPlayList.songlist[musicfileviewmodel.selectedMusicPosition]

            ApplicationClass.sSharedPreferences.putSelectedSongPosition(musicfileviewmodel.selectedMusicPosition)

            var mediaItem = MediaItem.fromUri("${uri}/${musicfileviewmodel.selectedMusic.id}")
            musicfileviewmodel.isPlayingOn = 0
            player.setMediaItem(mediaItem, musicfileviewmodel.isPlayingOn)
            player.prepare()
            player.play()

        }

        musicfileviewmodel.isPlayingOn = player.currentPosition
        Log.d(TAG, "savePlayingState: 진짜 바뀌나? : ${musicfileviewmodel.isPlayingOn}")

        if (player.playbackState != Player.STATE_IDLE && player.playbackState != Player.STATE_ENDED) {
            view?.postDelayed(updateSeekRunnable, 1000) // 1초에 한번씩 실행
        }
    }
    //=======================================

    inner class AllPlayListAdapter(val playlists: MutableList<PlayListDTO>) :
        RecyclerView.Adapter<AllPlayListAdapter.AllPlayListViewHolder>() {

        inner class AllPlayListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var title = itemView.findViewById<TextView>(R.id.tvPlayLists)
            var checkBox = itemView.findViewById<CheckBox>(R.id.checkBox1)

            fun bind(playlist: PlayListDTO) {
                title.text = playlist.playlistname
                checkBox.setOnClickListener {
                    var checked =
                        checkboxData(playlist.playlistname, checkBox.isChecked)
                    if (!checkBox.isChecked) {
                        for (i in 0..musicfileviewmodel.checkedPlayList.size - 1) {
                            if (checkSameData(
                                    musicfileviewmodel.checkedPlayList[i].playlistname,
                                    checked.playlistname
                                )
                            ) {
                                musicfileviewmodel.checkedPlayList.removeAt(i)
                                Log.d(
                                    TAG,
                                    "bind: 현재 크기는 ${musicfileviewmodel.checkedPlayList.size}"
                                )
                                break
                            }
                        }
                    } else {
                        musicfileviewmodel.checkedPlayList.add(checked)
                    }
                    Log.d(TAG, "bind: check box 클릭됨 =>${adapterPosition} /  ${checkBox.isChecked}")
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlayListViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.playlist_listview_item, parent, false)
            return AllPlayListViewHolder(view).apply {
            }
        }

        override fun onBindViewHolder(
            holder: AllPlayListAdapter.AllPlayListViewHolder,
            position: Int
        ) {
            holder.bind(playlists.get(position))
        }

        override fun getItemCount(): Int {
            return playlists.size
        }
    }
}
