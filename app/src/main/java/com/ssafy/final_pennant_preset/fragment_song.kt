package com.ssafy.final_pennant_preset

import android.annotation.SuppressLint
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
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentSongBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import java.util.concurrent.TimeUnit

private const val TAG = "fragment_song_싸피"
class fragment_song : Fragment() {

    private var _binding:FragmentSongBinding ?= null
    private val binding : FragmentSongBinding
        get() = _binding!!
    private lateinit var songlistadapter:SongListAdapter

    val musicviewmodel : MusicFileViewModel by activityViewModels()
    var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private lateinit var player: ExoPlayer
    private lateinit var list: MutableList<MusicDTO>
    lateinit var mediaItem : MediaItem

    private val updateSeekRunnable = Runnable {
        updateSeek()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).apply {
            menu.findItem(R.id.btnTotalFile).isCheckable = false
            menu.findItem(R.id.btnPlayList).isCheckable = false
            menu.findItem(R.id.btnCurrentList).isCheckable = false
            menu.findItem(R.id.btnConnectServer).isCheckable = false
            labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED
        }

        musicviewmodel.selectedPlaylistName = ApplicationClass.sSharedPreferences.getCurSongList()
        list = ApplicationClass.sSharedPreferences.getSongList(musicviewmodel.selectedPlaylistName)
        musicviewmodel.selectedMusicPosition = ApplicationClass.sSharedPreferences.getSelectedSongPosition()
        musicviewmodel.selectedMusic = list.get(musicviewmodel.selectedMusicPosition)
        Log.d(TAG, "onCreate: 선택한 음악 : ${musicviewmodel.selectedMusic}")
        musicviewmodel.selectedPlaylistName = ApplicationClass.sSharedPreferences.getCurSongList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSongBinding.inflate(inflater, container, false)

        musicviewmodel.selectedPlayList = PlayListDTO(musicviewmodel.selectedPlaylistName, list)

        songlistadapter = SongListAdapter(musicviewmodel.selectedPlayList.songlist)

        binding.trackTextView.text = musicviewmodel.selectedMusic.title
        binding.artistTextView.text=musicviewmodel.selectedMusic.artist


        var musicImg = MediaMetadataRetriever()
        Log.d(TAG, "onCreateView: ${musicviewmodel.selectedMusic.id}")
        musicImg.setDataSource(requireContext(),ContentUris.withAppendedId(uri,musicviewmodel.selectedMusic.id))
        var insertImg = musicImg.embeddedPicture
        if(insertImg!=null){
            var bitmap = BitmapFactory.decodeByteArray(insertImg,0,insertImg.size)
            binding.coverImageView.setImageBitmap(bitmap)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.showTimeoutMs=0
        binding.playerView.player = player

        Log.d(TAG, "onViewCreated: selectedMusicPosition : ${musicviewmodel.selectedMusicPosition}")
        Log.d(TAG, "onViewCreated: ${musicviewmodel.selectedPlayList.songlist.size}")
        mediaItem = MediaItem.fromUri("${uri}/${musicviewmodel.selectedPlayList.songlist[musicviewmodel.selectedMusicPosition].id}")
        player.setMediaItem(mediaItem,0)
        binding.playControlImageView.setImageResource(R.drawable.img_pause)
        player.prepare()
        player.play()

        binding.playListRecyclerView.apply {
            adapter = adapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration())
        }

        binding.playControlImageView.setOnClickListener {
            if(player.isPlaying){
                player.pause()
                Log.d(TAG, "onViewCreated: here?")
                binding.playControlImageView.setImageResource(R.drawable.img_play)
            }
            else{
                player.setMediaItem(mediaItem,0)
                player.play()
                binding.playControlImageView.setImageResource(R.drawable.img_pause)
            }
        }

        initPlayView()
        initPlayListButton()
        initPlayControlButtons()
        initSeekBar()
        initRecyclerView()
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

                itemView.setOnClickListener {
                    Toast.makeText(requireContext(),"${songList[layoutPosition].title}을/를 재생합니다.",Toast.LENGTH_SHORT).show()
                    ApplicationClass.sSharedPreferences.putSelectedSongPosition(layoutPosition)
                    musicviewmodel.selectedMusicPosition=layoutPosition
                    musicviewmodel.selectedMusic=musicviewmodel.selectedPlayList.songlist.get(layoutPosition)

                    //player view 전환
                    binding.trackTextView.text = musicviewmodel.selectedMusic.title
                    binding.artistTextView.text=musicviewmodel.selectedMusic.artist
                    var musicImg = MediaMetadataRetriever()
                    Log.d(TAG, "onCreateView: ${musicviewmodel.selectedMusic.id}")
                    musicImg.setDataSource(requireContext(),ContentUris.withAppendedId(uri,musicviewmodel.selectedMusic.id))
                    var insertImg = musicImg.embeddedPicture
                    if(insertImg!=null){
                        var bitmap = BitmapFactory.decodeByteArray(insertImg,0,insertImg.size)
                        binding.coverImageView.setImageBitmap(bitmap)
                    }

                    binding.playListGroup.isVisible = binding.playerViewGroup.isVisible.also {
                        binding.playerViewGroup.isVisible = binding.playListGroup.isVisible
                    }

                    mediaItem = MediaItem.fromUri("${uri}/${musicviewmodel.selectedPlayList.songlist[musicviewmodel.selectedMusicPosition].id}")
                    player.setMediaItem(mediaItem,0)
                    player.prepare()
                    player.play()
                }

            }
        }

        override fun onBindViewHolder(holder: SongListAdapter.SongListViewHolder, position: Int) {
            holder.bind(songList.get(position))
        }

        override fun getItemCount(): Int {
            return songList.size
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSeekBar() {
        binding.playerSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player.seekTo(seekBar.progress * 1000L)
            }

        })
    }

    private fun initPlayControlButtons() {

        // 재생 or 일시정지 버튼
        binding.playControlImageView.setOnClickListener {
            if (player.isPlaying) {
                Log.d(TAG, "initPlayControlButtons: player.isPlaying")
                player.pause()
            } else {
                Log.d(TAG, "initPlayControlButtons: player.isNotPlaying")
                player.play()
            }
        }

        binding.skipNextImageView.setOnClickListener {
            musicviewmodel.selectedMusicPosition = musicviewmodel.selectedMusicPosition+1
            musicviewmodel.selectedMusicPosition =  musicviewmodel.selectedMusicPosition % musicviewmodel.selectedPlayList.songlist.size

            musicviewmodel.selectedMusic = musicviewmodel.selectedPlayList.songlist[musicviewmodel.selectedMusicPosition]
            binding.trackTextView.text=musicviewmodel.selectedMusic.title
            binding.artistTextView.text=musicviewmodel.selectedMusic.artist

            var musicImg = MediaMetadataRetriever()
            Log.d(TAG, "onCreateView: ${musicviewmodel.selectedMusic.id}")
            musicImg.setDataSource(requireContext(),ContentUris.withAppendedId(uri,musicviewmodel.selectedMusic.id))
            var insertImg = musicImg.embeddedPicture
            if(insertImg!=null){
                var bitmap = BitmapFactory.decodeByteArray(insertImg,0,insertImg.size)
                binding.coverImageView.setImageBitmap(bitmap)
            }

            if(binding.playListGroup.isVisible) {
                binding.playListGroup.isVisible=!binding.playListGroup.isVisible
            }

            playMusic(musicviewmodel.selectedMusicPosition)

        }

        binding.skipPrevImageView.setOnClickListener {
            musicviewmodel.selectedMusicPosition = musicviewmodel.selectedMusicPosition-1
            if(musicviewmodel.selectedMusicPosition<0){
                musicviewmodel.selectedMusicPosition=musicviewmodel.selectedPlayList.songlist.size-1
            }

            musicviewmodel.selectedMusic = musicviewmodel.selectedPlayList.songlist[musicviewmodel.selectedMusicPosition]
            binding.trackTextView.text=musicviewmodel.selectedMusic.title
            binding.artistTextView.text=musicviewmodel.selectedMusic.artist

            var musicImg = MediaMetadataRetriever()
            Log.d(TAG, "onCreateView: ${musicviewmodel.selectedMusic.id}")
            musicImg.setDataSource(requireContext(),ContentUris.withAppendedId(uri,musicviewmodel.selectedMusic.id))
            var insertImg = musicImg.embeddedPicture
            if(insertImg!=null){
                var bitmap = BitmapFactory.decodeByteArray(insertImg,0,insertImg.size)
                binding.coverImageView.setImageBitmap(bitmap)
            }

            playMusic(musicviewmodel.selectedMusicPosition)
        }
    }

    private fun initPlayView() {

        player.addListener(object : Player.Listener {
            // 미디어 아이템이 바뀔 때
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                Log.d(TAG, "onMediaItemTransition: ${mediaItem?.mediaId}")
                val newIndex: String = mediaItem?.mediaId ?: return
//                model.currentPosition = newIndex.toInt()
//                adapter.submitList(model.getAdapterModels())

                // 리사이클러 뷰 스크롤 이동
//                binding.playListRecyclerView.scrollToPosition(model.currentPosition)
//
//                updatePlayerView(model.currentMusicModel())
            }

            // 재생, 재생완료, 버퍼링 상태 ...
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)

                updateSeek()
            }
        })

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
        binding.totalTimeTextView.text= String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS), // 전체 분
            (duration / 1000) % 60 // 분 단위를 제외한 초
        )
    }

    private fun updatePlayerView(music: MusicDTO) {
        binding.trackTextView.text = music.title
        binding.artistTextView.text = music.artist

//        Glide.with(binding.coverImageView.context)
//            .load(music.id)
//            .into(binding.coverImageView)
        var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var musicImg = MediaMetadataRetriever()
        musicImg.setDataSource(requireContext(),ContentUris.withAppendedId(uri,music.id))
        var insertImg = musicImg.embeddedPicture
        if(insertImg!=null){
            var bitmap = BitmapFactory.decodeByteArray(insertImg,0,insertImg.size)
            binding.coverImageView.setImageBitmap(bitmap)
        }

    }

    private fun initRecyclerView() {
        songlistadapter =  SongListAdapter(musicviewmodel.selectedPlayList.songlist)

        binding.playListRecyclerView.adapter = songlistadapter
        binding.playListRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun initPlayListButton() {
        binding.playListImageView.setOnClickListener {
            // 강의 와는 다르게 구현
            binding.playListGroup.isVisible = binding.playerViewGroup.isVisible.also {
                binding.playerViewGroup.isVisible = binding.playListGroup.isVisible
            }
        }
    }

//    private fun setMusicList(modelList: List<MusicModel>) {
//        player ?: return
//        context?.let {
//            player?.addMediaItems(modelList.map { musicModel ->
//                MediaItem.Builder()
//                    .setMediaId(musicModel.id.toString()) // 미디어 아이디를 musicModel id로
//                    .setUri(musicModel.streamUrl)
//                    .build()
//                /*
//                미디어 아이템에 2가지 태그 지정 가능
//                미디어 id, 뷰에 태그 지정했듯 미디어 아이템에 태그 지정 가능
//                 */
//            })
//
//            player?.prepare()
//        }
//    }

    private fun playMusic(position: Int) {
//        model.updateCurrentPosition(musicModel)
//        player?.seekTo(model.currentPosition, 0) // positionsMs=0 초 부터 시작
//        player?.play()

        var mediaItem =
            MediaItem.fromUri("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${musicviewmodel.selectedPlayList.songlist[position].id}")

        binding.playerViewGroup.visibility=View.VISIBLE

        player?.setMediaItem(mediaItem,0) // startPositionsMs=0 초 부터 시작
        player?.play()
    }

    override fun onStop() {
        super.onStop()

        player?.pause()
        view?.removeCallbacks(updateSeekRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        player?.release()
        view?.removeCallbacks(updateSeekRunnable)
    }
}
