package com.ssafy.final_pennant_preset


import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentTotallistBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO

private const val TAG = "fragment_totallist_싸피"

class fragment_totallist : Fragment() {
    private var _binding: FragmentTotallistBinding? = null
    private val binding: FragmentTotallistBinding
        get() = _binding!!

    val musicviewmodel: MusicFileViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnTotalFile).isChecked=true
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
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun getPlayList() {
        musicviewmodel.playList.clear()
        var namelistarray = ApplicationClass.sSharedPreferences.getSongListName()

        for(i in 0..namelistarray.size-2){
            Log.d(TAG, "getPlayList: ${namelistarray.get(i)}")
            musicviewmodel.playList.add(PlayListDTO(namelistarray.get(i), ApplicationClass.sSharedPreferences.getSongList(namelistarray.get(i))))
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
                            musicviewmodel.selectedMusic = musicList[layoutPosition]
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.framecontainer, fragment_addtoplaylist())
                                .addToBackStack("addSongToPlayList").commit()
                            Log.d(TAG, "onCreateContextMenu: ${musicviewmodel.selectedMusic}")
                            true
                        }
                }
            }

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
        getMP3().use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val artist =
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val genre = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE))

                    val dto = MusicDTO(id, title, albumId, artist, genre)

                    //기본 설정 통화 녹음 파일들 제외
                    if (!dto.title.contains("통화") && !dto.title.contains("녹음") && !musicviewmodel.MusicList.contains(dto)) {
                        musicviewmodel.MusicList.add(dto)
                    }
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
