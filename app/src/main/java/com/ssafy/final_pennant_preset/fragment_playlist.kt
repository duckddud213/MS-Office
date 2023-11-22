package com.ssafy.final_pennant_preset

import android.app.Dialog
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.DialogAddplaylistBinding
import com.ssafy.final_pennant.databinding.DialogDeleteplaylistBinding
import com.ssafy.final_pennant.databinding.DialogUploadtoserverBinding
import com.ssafy.final_pennant.databinding.FragmentPlaylistBinding
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import com.ssafy.final_pennant_preset.config.ApplicationClass
import java.util.concurrent.TimeUnit

private const val TAG = "fragment_playlist_싸피"

class fragment_playlist : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    val musicviewmodel: MusicFileViewModel by activityViewModels()


    //=======================================
    private lateinit var player: ExoPlayer
    var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val updateSeekRunnable = Runnable {
        savePlayingState()
    }
    //=======================================

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnPlayList).isChecked =
            true
        getPlayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().apply {
            //currentPlayList로 이동 후 onBackPressed 상황시 bottomNavItem focus 전환 위해 적용
            //isChecked는 하나의 항목에만 적용되므로 true로 변환될 아이템에 설정하면 나머지는 자동 false처리(따로 처리X)
            findViewById<BottomNavigationView>(R.id.bottomNavView).labelVisibilityMode =
                NavigationBarView.LABEL_VISIBILITY_AUTO
            findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnPlayList).isChecked =
                true
        }


        val playListAdapter = PlayListAdapter(musicviewmodel.playList)
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        binding.rvTotalPlayList.apply {
            adapter = playListAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration(requireContext()))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddNewPlayList.setOnClickListener {
            val dialogBinding: DialogAddplaylistBinding = DialogAddplaylistBinding.inflate(
                LayoutInflater.from(requireContext())
            )

            var dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(dialogBinding.root)
            dialog.show()

            dialogBinding.btnCreatePlayListYes.setOnClickListener {
                var str: String = dialogBinding.etNewPlayListName.text.toString()
                if (str.equals("")) {
                    Toast.makeText(requireContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    if (isNotDup(str)) {
                        Log.d(TAG, "onViewCreated: ${str}")
                        addPlayList(str)
                        musicviewmodel.playList.add(PlayListDTO(str, mutableListOf<MusicDTO>()))
                        binding.rvTotalPlayList.adapter!!.notifyItemInserted(musicviewmodel.playList.size - 1)
                        ApplicationClass.sSharedPreferences.putSongList(str,mutableListOf<MusicDTO>())
                    }
                    else {
                        Toast.makeText(requireContext(), "기존에 생성된 재생목록입니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                dialog.dismiss()
            }
            dialogBinding.btnCreatePlayListNo.setOnClickListener {
                dialog.dismiss()
            }
        }

        player = ExoPlayer.Builder(requireContext()).build()

        //======================================
        if (musicviewmodel.isPlaying) {
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

            var mediaItem = MediaItem.fromUri("${uri}/${musicviewmodel.selectedMusic.id}")
            player.setMediaItem(mediaItem, musicviewmodel.isPlayingOn)
            player.prepare()
            player.play()

            savePlayingState()


        }

        //=======================================
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

    override fun onDetach() {
        super.onDetach()
        //프래그먼트간 화면 이동 시 음악 재생 진행률 정보 전달
        player.stop()
        player.release()
    }

    private fun isNotDup(name: String): Boolean {
        for (i in 0..musicviewmodel.playList.size - 1) {
            if (musicviewmodel.playList.get(i).playlistname.equals(name)) {
                return false
            }
        }
        return true
    }

    private fun getPlayList() {
        musicviewmodel.playList.clear()
        var namelistarray = ApplicationClass.sSharedPreferences.getSongListName()

        for (i in 0..namelistarray.size - 2) {
            Log.d(TAG, "getPlayList: ${namelistarray.get(i)}")
            musicviewmodel.playList.add(
                PlayListDTO(
                    namelistarray.get(i),
                    ApplicationClass.sSharedPreferences.getSongList(namelistarray.get(i))
                )
            )
        }
    }

    private fun addPlayList(name: String) {
        ApplicationClass.sSharedPreferences.addSongListName(name)
    }

    inner class PlayListAdapter(val playlists: MutableList<PlayListDTO>) :
        RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder>() {
        inner class PlayListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnCreateContextMenuListener {
            var title = itemView.findViewById<TextView>(R.id.tvPlayListTitle)

            init {
                itemView.setOnCreateContextMenuListener(this)
            }

            fun bind(list: PlayListDTO) {
                Log.d(TAG, "bind: ${list.playlistname} / ${list.songlist}")
                Log.d(TAG, "bind: ${musicviewmodel.playList.size}")
                title.text = list.playlistname
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                requireActivity().apply {
                    menuInflater.inflate(R.menu.playlistcontextmenu, menu)
                    menu?.findItem(R.id.context_menu_delete_playlist)
                        ?.setOnMenuItemClickListener {
                            val dialogBinding: DialogDeleteplaylistBinding =
                                DialogDeleteplaylistBinding.inflate(
                                    LayoutInflater.from(requireContext())
                                )

                            var dialog = Dialog(requireContext())
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            dialog.setContentView(dialogBinding.root)
                            dialog.show()

                            dialogBinding.tvDeletePlayListMsg.text =
                                "재생 목록 [${playlists[layoutPosition].playlistname}]을 삭제하시겠습니까?"

                            dialogBinding.btnDeletePlayListYes.setOnClickListener {
                                for (i in 0..musicviewmodel.playList.size - 1) {
                                    if (musicviewmodel.playList.get(i).playlistname.equals(playlists[layoutPosition].playlistname)) {
                                        ApplicationClass.sSharedPreferences.deleteSongListName(
                                            playlists[layoutPosition].playlistname
                                        )
                                        if (musicviewmodel.selectedPlaylistName.equals(
                                                musicviewmodel.playList.get(i).playlistname
                                            )
                                        ) {
                                            ApplicationClass.sSharedPreferences.putCurSongList("")
                                            ApplicationClass.sSharedPreferences.putSelectedSongPosition(
                                                -1
                                            )
                                        }
                                        musicviewmodel.playList.removeAt(i)
                                        binding.rvTotalPlayList.adapter!!.notifyItemRemoved(i)
                                        break
                                    }
                                }
                                dialog.dismiss()
                            }
                            dialogBinding.btnDeletePlayListNo.setOnClickListener {
                                dialog.dismiss()
                            }

                            true
                        }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.totalplaylist_item, parent, false)
            return PlayListViewHolder(view).apply {
//                클릭 시 현재 재생 목록에 해당 재생 목록 전달 기능 추가
                itemView.setOnClickListener {
                    for (i in 0..musicviewmodel.playList.size - 1) {
                        if (musicviewmodel.playList.get(i).playlistname == playlists[layoutPosition].playlistname) {
                            musicviewmodel.selectedPlaylistName =
                                musicviewmodel.playList.get(i).playlistname
                            musicviewmodel.playList.get(i).songlist =
                                ApplicationClass.sSharedPreferences.getSongList(musicviewmodel.selectedPlaylistName)

                            Log.d(TAG, "onCreateViewHolder: ${musicviewmodel.selectedPlaylistName}")
                            ApplicationClass.sSharedPreferences.putCurSongList(musicviewmodel.selectedPlaylistName)
                            Log.d(
                                TAG,
                                "onCreateViewHolder: getCur : ${ApplicationClass.sSharedPreferences.getCurSongList()}"
                            )
                            
                            player.stop()
                            player.release()

                            requireActivity().apply {
                                findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(
                                    R.id.btnCurrentList
                                ).isChecked = true
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.framecontainer, fragment_currentlist())
                                    .addToBackStack("moveToCurrentPlayList").commit()
                            }
                            break
                        }
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: PlayListAdapter.PlayListViewHolder, position: Int) {
            holder.bind(playlists.get(position))
        }

        override fun getItemCount(): Int {
            return playlists.size
        }
    }
}
