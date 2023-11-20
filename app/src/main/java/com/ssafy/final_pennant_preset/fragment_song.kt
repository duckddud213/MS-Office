package com.ssafy.final_pennant_preset

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentSongBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.util.concurrent.TimeUnit


private const val TAG = "fragment_song_싸피"

class fragment_song : Fragment() {

    private var _binding: FragmentSongBinding? = null
    private val binding: FragmentSongBinding
        get() = _binding!!

    private lateinit var player: ExoPlayer
    private lateinit var audioView: PlayerControlView
    private lateinit var list: MutableList<MusicDTO>

    val musicviewmodel: MusicFileViewModel by activityViewModels()

    private val updateSeekRunnable = Runnable {
        updateSeek()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).apply {
            menu.findItem(R.id.btnTotalFile).isCheckable = false
            menu.findItem(R.id.btnPlayList).isCheckable = false
            menu.findItem(R.id.btnCurrentList).isCheckable = false
            menu.findItem(R.id.btnConnectServer).isCheckable = false
            labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED
        }

        musicviewmodel.selectedPlaylistName = ApplicationClass.sSharedPreferences.getCurSongList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSongBinding.inflate(inflater, container, false)

        list = ApplicationClass.sSharedPreferences.getSongList(musicviewmodel.selectedPlaylistName)
        musicviewmodel.selectedPlayList = PlayListDTO(musicviewmodel.selectedPlaylistName, list)

        var songListAdapter = SongListAdapter(musicviewmodel.selectedPlayList.songlist)


        var playingSong = musicviewmodel.selectedMusic
        var mediaItem =
            MediaItem.fromUri("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${playingSong.id}")
        player = ExoPlayer.Builder(requireContext()).build()
        player.setMediaItem(mediaItem)

        audioView = binding.playerView
        audioView.player = player

        binding.apply {
            playListGroup.isVisible = false
            playerViewGroup.isVisible = true
        }

        binding.playListRecyclerView.apply {
            adapter = songListAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayerSetting()
        initPlayListButton()
        initPlayControlButtons()
//        initSeekBar()
//        initRecyclerView()

    }

    override fun onDetach() {
        super.onDetach()
    }

    inner class SongListAdapter(val songList: MutableList<MusicDTO>) :
        RecyclerView.Adapter<SongListAdapter.SongListViewHolder>() {

        var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        inner class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var title = itemView.findViewById<TextView>(R.id.tvNowPlayingSongTitle)
            var artist = itemView.findViewById<TextView>(R.id.tvNowPlayingSongArtist)
            var genre = itemView.findViewById<TextView>(R.id.tvNowPlayingSongGenre)
            var img = itemView.findViewById<ImageView>(R.id.tvNowPlayingSongAlbumImage)

            fun bind(music: MusicDTO) {
                title.text = music.title
                artist.text = music.artist
                genre.text = music.genre

                var musicImg = MediaMetadataRetriever()
                musicImg.setDataSource(requireContext(), ContentUris.withAppendedId(uri, music.id))
                var insertImg = musicImg.embeddedPicture

                if (insertImg != null) {
                    var bitmap = BitmapFactory.decodeByteArray(insertImg, 0, insertImg.size)
                    img.setImageBitmap(bitmap)
                }

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.now_playing_songs_item, parent, false)
            return SongListViewHolder(view).apply {

                //곡 클릭시 해당 곡으로 새로 실행

//                itemView.setOnClickListener {
//                    Toast.makeText(
//                        parent.context,
//                        "musicList.id:${layoutPosition}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    val u: Uri = ContentUris.withAppendedId(uri, musicList[layoutPosition].id)
//                    val intent = Intent(Intent.ACTION_VIEW, u)
//                }

            }
        }

        override fun onBindViewHolder(holder: SongListAdapter.SongListViewHolder, position: Int) {
            holder.bind(songList.get(position))
        }

        override fun getItemCount(): Int {
            return songList.size
        }
    }

    private fun updateSeek() {
        val player = this.player ?: return
        val duration = if (player.duration >= 0) player.duration else 0 // 전체 음악 길이
        val position = player.currentPosition

        updateSeekUi(duration, position)

        val state = player.playbackState

        view?.removeCallbacks(updateSeekRunnable)
        // 재생 중 일때 (대 중이 아니거나, 재생이 끝나지 않은 경우)
        if (state != Player.STATE_IDLE && state != Player.STATE_ENDED) {
            view?.postDelayed(updateSeekRunnable, 1000) // 1초에 한번씩 실행
        }
    }

    private fun updateSeekUi(duration: Long, position: Long) {
        binding.playListSeekBar.max = (duration / 1000).toInt() // 총 길이를 설정. 1000으로 나눠 작게
        binding.playListSeekBar.progress = (position / 1000).toInt() // 동일하게 1000으로 나눠 작게

        binding.playerSeekBar.max = (duration / 1000).toInt()
        binding.playerSeekBar.progress = (position / 1000).toInt()

        binding.playTimeTextView.text = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS), // 현재 분
            (position / 1000) % 60 // 분 단위를 제외한 현재 초
        )
        binding.totalTimeTextView.text = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS), // 전체 분
            (duration / 1000) % 60 // 분 단위를 제외한 초
        )
    }

    private fun initPlayerSetting() {
        context?.let {
            player = ExoPlayer.Builder(it).build()
        }

        binding.playerView.player = player
        var transition : Transition = Slide(Gravity.BOTTOM)
        transition.duration = 60000
        transition.addTarget(R.id.player_view)
        TransitionManager.beginDelayedTransition(R.id.player_view as ViewGroup, transition)
        if(isVisible){
            setVisible(false)
            CoroutineScope(Dispatchers.Main).cancel()
        }
        else{
            setVisible(true)
//            delayVisibleGone()
        }


        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                if (isPlaying) {
                    binding.playControlImageView.setImageResource(R.drawable.img_pause)
                } else {
                    binding.playControlImageView.setImageResource(R.drawable.img_play)
                }
            }

//            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//                super.onMediaItemTransition(mediaItem, reason)
//
//                val newIndex: String = mediaItem?.mediaId ?: return
//                model.currentPosition = newIndex.toInt()
//                adapter.submitList(model.getAdapterModels())
//
//                // 리사이클러 뷰 스크롤 이동
//                binding.playListRecyclerView.scrollToPosition(model.currentPosition)
//
//                updatePlayerView(model.currentMusicModel())
//            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                updateSeek()
            }

        }
        )
    }

    private fun initPlayListButton() {
        binding.playListImageView.setOnClickListener {
            // 강의 와는 다르게 구현
            binding.playListGroup.isVisible = !binding.playListGroup.isVisible
            binding.playerViewGroup.isVisible = !binding.playerViewGroup.isVisible
        }
    }

    private fun initPlayControlButtons() {

        // 재생 or 일시정지 버튼
        binding.playControlImageView.setOnClickListener {
            val player = player

            Log.d(TAG, "initPlayControlButtons: ${player.isPlaying}")
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }

        binding.skipNextImageView.setOnClickListener {

            musicviewmodel.selectedMusicPosition = musicviewmodel.selectedMusicPosition+1
            musicviewmodel.selectedMusicPosition =  musicviewmodel.selectedMusicPosition % musicviewmodel.selectedPlayList.songlist.size
            playMusic(musicviewmodel.selectedMusicPosition)
        }

        binding.skipPrevImageView.setOnClickListener {
            musicviewmodel.selectedMusicPosition = musicviewmodel.selectedMusicPosition-1
            if(musicviewmodel.selectedMusicPosition<0){
                musicviewmodel.selectedMusicPosition=musicviewmodel.selectedPlayList.songlist.size-1
            }
            playMusic(musicviewmodel.selectedMusicPosition)
        }
    }

    private fun playMusic(position : Int) {
        var mediaItem =
            MediaItem.fromUri("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${musicviewmodel.selectedPlayList.songlist[position].id}")

        binding.playerViewGroup.visibility=View.VISIBLE

        player?.setMediaItem(mediaItem,0) // startPositionsMs=0 초 부터 시작
        player?.play()
    }

//    private fun setControllerVisible() {
//        val transition: Transition = Slide(Gravity.BOTTOM)
//        transition.duration = 600
//        transition.addTarget(this@BaseControllerView)
//        TransitionManager.beginDelayedTransition(this@BaseControllerView as ViewGroup, transition)
//        if (isVisible) {
//            setVisible(false)
//            coroutineForCancelAndCreate.cancel()
//        }
//        else {
//            setVisible(true)
//            delayVisibleGone()
//        }
//    }
//
//    private fun setVisible(visible:Boolean){
//        isVisible = visible
//        binding.playerView.isVisible=visible
//    }
}
