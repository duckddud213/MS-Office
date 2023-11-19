package com.ssafy.final_pennant_preset

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentPlaylistBinding
import com.ssafy.final_pennant_preset.DTO.MusicDTO
import com.ssafy.final_pennant_preset.DTO.MusicFileViewModel
import com.ssafy.final_pennant_preset.DTO.PlayListDTO

private const val TAG = "fragment_playlist_싸피"

class fragment_playlist : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    val musicviewmodel: MusicFileViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPlayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val playListAdapter = PlayListAdapter(musicviewmodel.playList)
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        binding.rvTotalPlayList.apply {
            adapter = playListAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddNewPlayList.setOnClickListener {
            val ad = AlertDialog.Builder(requireContext())
            ad.setIcon(R.drawable.music_ssafy_office)
            ad.setTitle("생성할 재생목록 이름 입력")

            val et:EditText = EditText(requireContext())
            ad.setView(et)

            ad.setPositiveButton("확인") { dialog, _ ->
                var str:String = et.text.toString()

                if(str.equals("")){
                    Toast.makeText(requireContext(),"이름을 입력해주세요.",Toast.LENGTH_SHORT).show()
                }
                else{
                    if(isNotDup(str)){
                        Log.d(TAG, "onViewCreated: ${str}")
                        musicviewmodel.playList.add(PlayListDTO(str, mutableListOf<MusicDTO>()))
                        binding.rvTotalPlayList.adapter!!.notifyItemInserted(musicviewmodel.playList.size-1)
                    }
                    else{
                        Toast.makeText(requireContext(),"기존에 생성된 재생목록입니다.",Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            ad.setNegativeButton("취소"){ dialog,_ ->
                dialog.dismiss()
            }
            ad.show()

        }
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

    }

    inner class PlayListAdapter(val playlists: MutableList<PlayListDTO>) :
        RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder>() {
        inner class PlayListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var title = itemView.findViewById<TextView>(R.id.tvPlayListTitle)
            fun bind(list: PlayListDTO) {
                Log.d(TAG, "bind: ${list.playlistname} / ${list.songlist}")
                Log.d(TAG, "bind: ${musicviewmodel.playList.size}")
                title.text = list.playlistname
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.totalplaylist_item, parent, false)
            return PlayListViewHolder(view).apply {

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
