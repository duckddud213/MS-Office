package com.ssafy.final_pennant_preset


import android.app.Dialog
import android.app.Notification
import android.app.NotificationManager
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.net.MediaType
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.DialogUploadtoserverBinding
import com.ssafy.final_pennant.databinding.FragmentTotallistBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import com.ssafy.final_pennant_preset.util.RetrofitUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

private const val TAG = "fragment_totallist_싸피"

class fragment_totallist : Fragment() {
    private var _binding: FragmentTotallistBinding? = null
    private val binding: FragmentTotallistBinding
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
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnTotalFile).isChecked =
            true
        initData()
        getPlayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTotallistBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun onDetach() {
        super.onDetach()
        //프래그먼트간 화면 이동 시 음악 재생 진행률 정보 전달
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

    private fun initView() {
        val musicAdapter = MusicAdapter(musicviewmodel.MusicList)

        musicAdapter.notifyDataSetChanged()

        binding.rvTotalSong.apply {
            adapter = musicAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration())
        }
    }

    inner class MusicAdapter(val musicList: MutableList<MusicDTO>) :
        RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

        var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            OnCreateContextMenuListener {
            var title = itemView.findViewById<TextView>(R.id.tvTitle)
            var artist = itemView.findViewById<TextView>(R.id.tvArtist)
            var genre = itemView.findViewById<TextView>(R.id.tvGenre)

            init {
                itemView.setOnCreateContextMenuListener(this)
            }

            fun bind(music: MusicDTO) {
                Log.d(TAG, "bind: ${music.id}")
                title.text = music.title
                artist.text = music.artist
                genre.text = music.genre
                Log.d(TAG, "bind: 제목은 ${music.title} / 장르는 ${music.genre}")
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                requireActivity().apply {
                    menuInflater.inflate(R.menu.contextmenu, menu)
                    menu?.findItem(R.id.context_menu_add_song_to_playlist)
                        ?.setOnMenuItemClickListener {
                            musicviewmodel.selectedMusicToBeAdded = musicList[layoutPosition]

                            player.stop()
                            player.release()

                            supportFragmentManager.beginTransaction()
                                .replace(R.id.framecontainer, fragment_addtoplaylist())
                                .addToBackStack("addSongToPlayList").commit()
                            Log.d(TAG, "onCreateContextMenu: ${musicviewmodel.selectedMusicToBeAdded}")
                            true
                        }

                    menu?.findItem(R.id.context_menu_send_song_to_server)
                        ?.setOnMenuItemClickListener {
                            musicviewmodel.selectedMusicToBeAdded = musicList[layoutPosition]

                            val dialogBinding : DialogUploadtoserverBinding = DialogUploadtoserverBinding.inflate(
                                LayoutInflater.from(requireContext()))

                            var dialog = Dialog(requireContext())
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            dialog.setContentView(dialogBinding.root)
                            dialog.show()

                            dialogBinding.standardApply.setOnClickListener{
                                val selectedRadio1 = dialogBinding.standardRadioGroup1.checkedRadioButtonId

                                val radioBtn1 = dialog.findViewById<View>(selectedRadio1) as RadioButton?

                                dialog.dismiss()
                                Log.d(TAG, "onCreateContextMenu: ${radioBtn1!!.text.toString()}")
                                uploadToServer(radioBtn1!!.text.toString())
                            }
                            dialogBinding.standardCancel.setOnClickListener {
                                dialog.dismiss()
                            }
                            
                            true
                        }
                }
            }
        }

        private fun uploadToServer(genreName : String){
            val uri = ContentUris.withAppendedId(uri, musicviewmodel.selectedMusicToBeAdded.id)
            val file = File(getFilePathUri(uri))
            val fileBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
            val mp3data = MultipartBody.Part.createFormData("file","${musicviewmodel.selectedMusicToBeAdded.title}.mp3",fileBody);

            CoroutineScope(Dispatchers.Main).launch {
                RetrofitUtil.musicService.uploadMusic(ApplicationClass.sSharedPreferences.getUID()!!,genreName.lowercase(),mp3data)
            }
        }

        private fun getFilePathUri(uri: Uri) : String{

            var columnIndex = 0
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            var cursor = requireActivity().contentResolver.query(uri, proj, null, null, null)

            if (cursor!!.moveToFirst()){
                columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            }

            return cursor.getString(columnIndex)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
            return MusicViewHolder(view).apply {

                //추후 전체 곡 목록에서 클릭 시 재생목록 상관없이 단일 음원 재생 기능 추가 예정

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

        override fun onBindViewHolder(holder: MusicAdapter.MusicViewHolder, position: Int) {
            holder.bind(musicList.get(position))
        }

        override fun getItemCount(): Int {
            return musicList.size
        }
    }

    private fun initData() {
        val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        musicviewmodel.MusicList.clear()
        val set = mutableSetOf<MusicDTO>()

        getMP3().use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                        ?: "unknown title"
                    val albumId =
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val artist =
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                            ?: "unknown artist"
                    val genre = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE))
                        ?: "genre"

                    val dto = MusicDTO(id, title, albumId, artist, genre)

                    if (title.contains("통화")) continue
                    if (title.contains("녹음")) continue
                    Log.d(TAG, "${dto.toString()}: ${set.contains(dto)}")
                    if (set.add(dto)) {
                        musicviewmodel.MusicList.add(dto)
                    }

                    //기본 설정 통화 녹음 파일들 제외
//                    if (!dto.title.contains("통화") && !dto.title.contains("녹음") && set.add(dto)
//                    ) {
//                        Log.d(TAG, "initData: ${dto.title} || ${dto.artist}")
//                        musicviewmodel.MusicList.add(dto)
//                    }
                } while (it.moveToNext())
            }
        }
    }

    private fun getMP3(): Cursor {
        val resolver = requireActivity().contentResolver
        val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val mp3File = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.GENRE,
        )

        return resolver.query(queryUri, mp3File, null, null, sortOrder)!!
    }
}
