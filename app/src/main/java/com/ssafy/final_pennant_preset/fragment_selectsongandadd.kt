package com.ssafy.final_pennant_preset

import android.app.Notification
import android.app.NotificationManager
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentSelectsongandaddBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import com.ssafy.final_pennant_preset.dto.checkboxSongData
import java.util.concurrent.TimeUnit

private const val TAG = "fragment_selectsonganda_싸피"

class fragment_selectsongandadd : Fragment() {
    private var _binding: FragmentSelectsongandaddBinding? = null
    private val binding: FragmentSelectsongandaddBinding
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

    fun checkSameSongData(Name1: String, Name2: String, Artist1: String, Artist2: String): Boolean {
        if (Name1 == Name2 && Artist1 == Artist2) {
            return true
        }

        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musicfileviewmodel.checkedSongList.clear()

        for (i in 0..musicfileviewmodel.MusicList.size - 1) {
            musicfileviewmodel.checkedSongList.add(
                checkboxSongData(
                    musicfileviewmodel.MusicList[i].title.toString(),
                    musicfileviewmodel.MusicList[i].artist.toString(),
                    false
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectsongandaddBinding.inflate(inflater, container, false)
        val allsonglistadapter = AllSongListAdapter(musicfileviewmodel.MusicList)
        binding.lvAddPlaylist2.apply {
            adapter = allsonglistadapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration(requireContext()))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //재생목록이 없는 상태면 Toast메시지나 Dialog로 안내하고 강제로 페이지 전환 추가

        binding.btnAddSongToSelectedPlaylist2.setOnClickListener {
            for (i in 0..musicfileviewmodel.checkedSongList.size - 1) {
                for (j in 0..musicfileviewmodel.MusicList.size - 1) {
                    //cId, cName : 체크한 항목 || pId, pName : 전체 곡 목록에 있는 항목
                    var cName = musicfileviewmodel.checkedSongList[i].songtitle
                    var cArtist = musicfileviewmodel.checkedSongList[i].songartist
                    var pName = musicfileviewmodel.MusicList[j].title
                    var pArtist = musicfileviewmodel.MusicList[j].artist
                    var isDup = false

                    if (checkSameSongData(
                            cName,
                            pName,
                            cArtist,
                            pArtist
                        ) && musicfileviewmodel.checkedSongList[i].checked
                    ) {
                        for (k in 0..musicfileviewmodel.selectedPlayList.songlist.size - 1) {
                            //이미 현재 재생목록에 있는 곡일 경우 추가하지 않음
                            if (checkSameSongData(
                                    musicfileviewmodel.selectedPlayList.songlist[k].title,
                                    cName,
                                    musicfileviewmodel.selectedPlayList.songlist[k].artist,
                                    cArtist
                                )
                            ) {
                                isDup = true
                            }
                        }
                        if (!isDup) {
                            musicfileviewmodel.selectedPlayList.songlist.add(musicfileviewmodel.MusicList[j])
                        }
                    }
                    ApplicationClass.sSharedPreferences.putSongList(
                        musicfileviewmodel.selectedPlaylistName,
                        musicfileviewmodel.selectedPlayList.songlist
                    )
                }
            }

            player.stop()
            player.release()

            requireActivity().apply {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.beginTransaction().replace(R.id.framecontainer, fragment_currentlist()).commit()
            }



            if (musicfileviewmodel.checkedSongList.size == 0) {
                Toast.makeText(requireContext(), "재생 목록에 추가된 곡이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "재생 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show()
            }

            musicfileviewmodel.checkedSongList.clear()
        }

        binding.btnGoBacks2.setOnClickListener {
            player.stop()
            player.release()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.framecontainer, fragment_currentlist()).commit()
        }

        //======================================
        player = ExoPlayer.Builder(requireContext()).build()
        if (musicfileviewmodel.isPlaying) {
            //음악 재생 중에 넘어온 경우

            musicfileviewmodel.playerNotificationManager =
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

            musicfileviewmodel.playerNotificationManager.setPlayer(player)

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
            view?.postDelayed(updateSeekRunnable, 100) // 1초에 한번씩 실행
        }
    }
    //=======================================

    inner class AllSongListAdapter(val songlist: MutableList<MusicDTO>) :
        RecyclerView.Adapter<AllSongListAdapter.AllSongListViewHolder>() {

        inner class AllSongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var title = itemView.findViewById<TextView>(R.id.tvTitle2)
            var artist = itemView.findViewById<TextView>(R.id.tvArtist2)
            var genre = itemView.findViewById<TextView>(R.id.tvGenre2)
            var checkBox = itemView.findViewById<CheckBox>(R.id.checkBox2)
            var img = itemView.findViewById<ImageView>(R.id.ivSongImg2)

            fun bind(songlist: MusicDTO, songChecked: checkboxSongData) {
                title.text = songlist.title
                artist.text = songlist.artist
                genre.text = songlist.genre
                checkBox.isChecked = songChecked.checked

                Log.d(
                    TAG,
                    "bind: 현재 bind된 데이터 => ${title.text} / ${artist.text} / ${genre.text} / ${checkBox.isChecked} / "
                )

                for (i in 0..musicfileviewmodel.checkedSongList.size - 1) {
                    Log.d(TAG, "bind: 현재 checkedList는 : ${musicfileviewmodel.checkedSongList[i]}")
                }

                var musicImg = MediaMetadataRetriever()
                musicImg.setDataSource(
                    requireContext(),
                    ContentUris.withAppendedId(uri, songlist.id)
                )
                var insertImg = musicImg.embeddedPicture

                if (insertImg != null) {
                    var bitmap = BitmapFactory.decodeByteArray(insertImg, 0, insertImg.size)
                    img.setImageBitmap(bitmap)
                } else {
                    img.setImageResource(R.drawable.music_ssafy_office)
                }

                checkBox.setOnClickListener {
//                    var checked =
//                        checkboxSongData(songlist.title, songlist.artist, checkBox.isChecked)
//
//                    if (!checkBox.isChecked) {
//                        for (i in 0..musicfileviewmodel.checkedSongList.size - 1) {
//                            Log.d(TAG, "bind: 값 확인 ${musicfileviewmodel.checkedSongList[i].songtitle} / ${checked.songtitle} / ${musicfileviewmodel.checkedSongList[i].songartist} / ${checked.songartist}")
//                            if (checkSameSongData(musicfileviewmodel.checkedSongList[i].songtitle,checked.songtitle,musicfileviewmodel.checkedSongList[i].songartist,checked.songartist)) {
//                                musicfileviewmodel.checkedSongList.removeAt(i)
//                                Log.d(TAG, "bind: 삭제 위치 : ${i} / ${adapterPosition}")
//                                Log.d(TAG,"bind: 현재 크기는 ${musicfileviewmodel.checkedSongList.size}")
//                                break
//                            }
//                        }
//                    } else {
//                        musicfileviewmodel.checkedSongList.add(checked)
//                    }
//                    for (i in 0..musicfileviewmodel.checkedSongList.size - 1) {
//                        Log.d(TAG, "bind: ${i}번 데이터 => ${musicfileviewmodel.checkedSongList[i]}")
//                    }
                    songChecked.checked = checkBox.isChecked
                    if (songChecked.checked) {
                        //체크된 경우 리스트에 저장
                        songChecked.songtitle = title.text.toString()
                        songChecked.songartist = artist.text.toString()
                    } else {

                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSongListViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.songlist_tobeadded_toplaylist, parent, false)
            return AllSongListViewHolder(view).apply {
            }
        }

        override fun onBindViewHolder(
            holder: AllSongListAdapter.AllSongListViewHolder,
            position: Int
        ) {
            holder.bind(songlist.get(position), musicfileviewmodel.checkedSongList[position])
        }

        override fun getItemCount(): Int {
            return songlist.size
        }
    }
}
