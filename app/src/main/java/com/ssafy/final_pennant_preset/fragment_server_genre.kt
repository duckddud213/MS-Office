package com.ssafy.final_pennant_preset

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentServerBinding
import com.ssafy.final_pennant.databinding.FragmentServerGenreBinding
import com.ssafy.final_pennant_preset.dto.ServerMusicDTO
import com.ssafy.final_pennant_preset.config.ApplicationClass

private const val GENRE = ApplicationClass.CHANNEL_POP

private const val TAG = "fragment_server_genre_싸피"
class fragment_server_genre : Fragment() {

    private var genre: String? = null
    private var _binding: FragmentServerGenreBinding? = null
    private val binding: FragmentServerGenreBinding
        get() = _binding!!

    private val serverViewModel : ServerViewModel by viewModels()
    private lateinit var serverMusic : ArrayList<ServerMusicDTO>

    private lateinit var musicListAdapter : MusicListAdapter
    private val downloadFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/msOffice")

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

        musicListAdapter.myItemClickListener = object : MusicListAdapter.ItemClickListener{
            override fun onMyClick(view: View, dto: ServerMusicDTO) {

            }
        }

        binding.rvServerGenre.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = musicListAdapter
        }
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


    class MusicListAdapter(var genreList: MutableList<ServerMusicDTO>) :

        RecyclerView.Adapter<MusicListAdapter.CustomViewHolder>() {
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnCreateContextMenuListener {

            var tv_genre = itemView.findViewById<TextView>(R.id.tvPlayListTitle)

            fun bind(music: ServerMusicDTO) {
                tv_genre.text = music.toString()
                tv_genre.setOnClickListener {
                    myItemClickListener.onMyClick(it, music)
                }
            }

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                v: View?,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
            }
        }

        interface ItemClickListener{
            fun onMyClick(view: View, dto: ServerMusicDTO)
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