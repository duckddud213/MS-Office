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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentCurrentlistBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import java.util.concurrent.TimeUnit

private const val TAG = "fragment_currentlist_싸피"

class fragment_currentlist : Fragment() {
    private var _binding: FragmentCurrentlistBinding? = null
    private val binding: FragmentCurrentlistBinding
        get() = _binding!!
    private lateinit var callback: OnBackPressedCallback
    private lateinit var list: MutableList<MusicDTO>

    val musicviewmodel: MusicFileViewModel by activityViewModels()

    //=======================================
    private lateinit var player: ExoPlayer
    var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val updateSeekRunnable = Runnable {
        savePlayingState()
    }
    //=======================================

    override fun onAttach(context: Context) {
        player = MainActivity.getPlayer(context)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnCurrentList).isChecked =
            true
        musicviewmodel.selectedPlaylistName = ApplicationClass.sSharedPreferences.getCurSongList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCurrentlistBinding.inflate(inflater, container, false)
        var currentPlayListAdapter: CurrentPlayListAdapter
        list = ApplicationClass.sSharedPreferences.getSongList(musicviewmodel.selectedPlaylistName)
        musicviewmodel.selectedPlayList = PlayListDTO(musicviewmodel.selectedPlaylistName, list)

        if (musicviewmodel.selectedPlaylistName.isNotEmpty()) {
            //선택된 재생목록이 있는 경우
            binding.tvCurrentList.text = musicviewmodel.selectedPlaylistName
            for (i in 0..musicviewmodel.playList.size - 1) {
                if (musicviewmodel.playList.get(i).playlistname.equals(musicviewmodel.selectedPlaylistName)) {
                    list = musicviewmodel.playList.get(i).songlist
                    break
                }
            }
            musicviewmodel.selectedPlayList.songlist=list
//            list = musicviewmodel.selectedPlayList.songlist
//            currentPlayListAdapter =
//                CurrentPlayListAdapter(musicviewmodel.selectedPlayList.songlist)
        } else {
            //선택된 재생목록이 없는 경우 => 빈 리스트 전달
            binding.tvCurrentList.text = "선택된 재생목록 없음"
            list = mutableListOf<MusicDTO>()
        }

        currentPlayListAdapter = CurrentPlayListAdapter(list)

        binding.rvCurrentPlayList.apply {
            adapter = currentPlayListAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration(requireContext()))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddNewPlayList.setOnClickListener {
            if (musicviewmodel.selectedPlaylistName.equals("")) {
                Toast.makeText(requireContext(), "선택된 재생목록이 없습니다!", Toast.LENGTH_SHORT).show()
            } else {
                player.stop()
                player.release()

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.framecontainer, fragment_selectsongandadd())
                    .addToBackStack("addSongToPlayList").commit()
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
            view?.postDelayed(updateSeekRunnable, 100) // 1초에 한번씩 실행
        }
    }
    //=======================================

    inner class CurrentPlayListAdapter(val songlist: MutableList<MusicDTO>) :
        RecyclerView.Adapter<CurrentPlayListAdapter.CurrentPlayListViewHolder>() {
        inner class CurrentPlayListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnCreateContextMenuListener {
            var title = itemView.findViewById<TextView>(R.id.tvSongTitle)
            var artist = itemView.findViewById<TextView>(R.id.tvSongArtist)
            var genre = itemView.findViewById<TextView>(R.id.tvSongGenre)

            init {
                itemView.setOnCreateContextMenuListener(this)
            }

            fun bind(list: MusicDTO) {
                Log.d(
                    TAG,
                    "bind: ${list.id} / ${list.albumId} / ${list.title} / ${list.artist} / ${list.genre}"
                )
                title.text = list.title
                artist.text = list.artist
                genre.text = list.genre
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

                            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                            builder.setTitle("재생목록에서 삭제")
                            builder.setMessage(
                                "현재 재생 목록에서 [${songlist[layoutPosition].title}]을 삭제하시겠습니까?"
                            )
                            builder.setIcon(R.drawable.music_ssafy_office)
                            builder.setCancelable(true)
                            builder.setPositiveButton("삭제") { _, _ ->
                                ApplicationClass.sSharedPreferences.deleteSongFromList(
                                    musicviewmodel.selectedPlaylistName,
                                    songlist[layoutPosition]
                                )

                                musicviewmodel.selectedPlayList.songlist.removeAt(layoutPosition)
                                list = musicviewmodel.selectedPlayList.songlist
                                ApplicationClass.sSharedPreferences.putSongList(musicviewmodel.selectedPlaylistName,list)
                                Log.d(TAG, "onCreateContextMenu: ${layoutPosition}")
                                binding.rvCurrentPlayList.adapter!!.notifyItemRemoved(layoutPosition)

                                if (musicviewmodel.selectedMusicPosition == layoutPosition) {
                                    //재생 중이던 곡이 삭제된 경우 => 음악 정지
                                    player.stop()
                                    player.release()
                                    musicviewmodel.selectedMusicPosition %= musicviewmodel.selectedPlayList.songlist.size
                                    ApplicationClass.sSharedPreferences.putSelectedSongPosition(-1)
                                }
                            }
                            builder.setNegativeButton(
                                "취소"
                            ) { dialog, _ -> dialog.cancel() }
                            builder.create().show()
                            true
                        }
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CurrentPlayListViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.playlist_songs_item, parent, false)
            return CurrentPlayListViewHolder(view).apply {
                //클릭 시 해당 곡 재생 기능 추가
                itemView.setOnClickListener {
                    Log.d(TAG, "onCreateViewHolder: position : ${layoutPosition}")
                    if (musicviewmodel.selectedMusic != songlist[layoutPosition]) {
                        musicviewmodel.checkSameSong = false
                    }
                    musicviewmodel.selectedMusic = songlist[layoutPosition]
                    musicviewmodel.selectedMusicPosition = layoutPosition
                    ApplicationClass.sSharedPreferences.putSelectedSongPosition(layoutPosition)
                    requireActivity().apply {
                        supportFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.framecontainer, fragment_song()).commit()
                    }
                }
            }
        }

        override fun onBindViewHolder(
            holder: CurrentPlayListAdapter.CurrentPlayListViewHolder,
            position: Int
        ) {
            holder.bind(songlist.get(position))
            Log.d(
                TAG,
                "onBindViewHolder: ${songlist} / ${songlist.get(position)} / ${songlist.size}"
            )

        }

        override fun getItemCount(): Int {
            return songlist.size
        }
    }
}
