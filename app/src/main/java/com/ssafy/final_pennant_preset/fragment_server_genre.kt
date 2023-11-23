package com.ssafy.final_pennant_preset

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentServerGenreBinding
import com.ssafy.final_pennant_preset.dto.ServerMusicDTO
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.service.MusicDownloader
import com.ssafy.final_pennant_preset.util.RetrofitUtil

private const val GENRE = ApplicationClass.CHANNEL_IDOL

private const val TAG = "fragment_server_genre_싸피"
class fragment_server_genre : Fragment() {

    private var genre: String? = null
    private var _binding: FragmentServerGenreBinding? = null
    private val binding: FragmentServerGenreBinding
        get() = _binding!!

    private lateinit var mainActivity: MainActivity

    private lateinit var musicListAdapter : MusicListAdapter
    private lateinit var downLoader : MusicDownloader

    private val mainActivityViewModel:MainActivityViewModel by activityViewModels()
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
        mainActivityViewModel.setListWithGenre(genre!!)

        musicListAdapter = MusicListAdapter()

        downLoader = MusicDownloader(mainActivity)

        musicListAdapter.myItemClickListener = object : ItemClickListener{
            override fun onMyClick(view: View, dto: ServerMusicDTO) {
                Toast.makeText(mainActivity, "${dto}를 다운로드합니다", Toast.LENGTH_SHORT).show()
                downLoader.downloadFile(dto.musicUrl, dto.musicName)
                mainActivityViewModel.downloadFile = "/msOffice/${dto.musicName}"
            }
        }

        binding.rvServerGenre.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = musicListAdapter
            addItemDecoration(CustomItemDecoration(requireContext()))
        }

        binding.tvServerDownloadMusic.text = "Download - $genre"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }



    fun registerObserver() {
        mainActivityViewModel.musicWithGenre.observe(viewLifecycleOwner) {
            Log.d(TAG, "registerObserver: $it")
            musicListAdapter.submitList(it.toMutableList())
        }
    }

    inner class MusicListAdapter :
        ListAdapter<ServerMusicDTO, fragment_server_genre.MusicListAdapter.CustomViewHolder> (ServerMusicComparator) {
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnCreateContextMenuListener {

            var tv_genre = itemView.findViewById<TextView>(R.id.tvPlayListTitle)
            var tv_mine = itemView.findViewById<TextView>(R.id.tvPlayIsMine)
            var layout_serverItem = itemView.findViewById<ConstraintLayout>(R.id.layoutServerItem)
            lateinit var serverMusic: ServerMusicDTO
            val uid = ApplicationClass.sSharedPreferences.getUID()!!

            fun bind(music: ServerMusicDTO) {
                serverMusic = music
                tv_genre.text = music.toString()
                tv_genre.setOnClickListener {
                    myItemClickListener.onMyClick(it, music)
                }
                tv_genre.setOnCreateContextMenuListener(this)

                if (uid == music.uploadUser) {
                    layout_serverItem.setBackgroundResource(R.drawable.gradient)
                    // background color
                    layout_serverItem.background.alpha = 100
                    // 투명도
                    tv_mine.text = "my muisc"
                }
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                requireActivity().apply {
                    if (uid == serverMusic.uploadUser) {
                        menuInflater.inflate(R.menu.server_item_remove, menu)
                        menu?.findItem(R.id.server_updateItem)
                            ?.setOnMenuItemClickListener {
                                showDialogUpdate(serverMusic)
                                true
                        }

                        menu?.findItem(R.id.server_removeItem)
                            ?.setOnMenuItemClickListener {
                                mainActivityViewModel.deleteMusic(genre!!, serverMusic.musicId)
                                Toast.makeText(mainActivity, "${serverMusic.toString()}을 삭제합니다", Toast.LENGTH_SHORT).show()
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
            holder.bind(getItem(position))
        }
    }

    interface ItemClickListener{
        fun onMyClick(view: View, dto: ServerMusicDTO)
    }
    @SuppressLint("MissingInflatedId")
    private fun showDialogUpdate(dto: ServerMusicDTO) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater.inflate(R.layout.dialog_updatemusic, null)
        val et_update = inflater.findViewById<EditText>(R.id.et_update)
        et_update.setTextColor(Color.BLACK)

        et_update.setText(dto.toString())

        builder.apply {
            setView(inflater)
            setPositiveButton("수정") { dialogInterface, i -> // 안드로이드 버전에 따라 다를 수 있음.
                if (et_update.text.toString().isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "이름을 확인해 주세요", Toast.LENGTH_SHORT).show()
                } else {
                    var updateDto = ServerMusicDTO(
                        dto.musicId
                        , dto.uploadUser
                        , et_update.text.toString() + ".mp3" // name
                        , dto.musicS3Key
                        , dto.musicGenre // genre
                        , dto.musicUrl )
                    mainActivityViewModel.updateMusic(updateDto)
                    Toast.makeText(requireContext(), "수정되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("취소") { dialogInterface, _ -> dialogInterface.cancel() }
        }
        builder.create().show()
    }

    companion object {

        var curGenre: String = ApplicationClass.CHANNEL_ROCK
        @JvmStatic
        fun newInstance(genre: String) =
            fragment_server_genre().apply {
                arguments = Bundle().apply {
                    putString(GENRE, genre)
                }
                curGenre = genre
            }
        object ServerMusicComparator: DiffUtil.ItemCallback<ServerMusicDTO>() {
            override fun areItemsTheSame(oldItem: ServerMusicDTO, newItem: ServerMusicDTO): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
            override fun areContentsTheSame(oldItem: ServerMusicDTO, newItem: ServerMusicDTO): Boolean {
                return oldItem.musicId == newItem.musicId
            }

        }
    }
}