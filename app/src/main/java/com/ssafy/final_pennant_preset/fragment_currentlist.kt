package com.ssafy.final_pennant_preset

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentCurrentlistBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO

private const val TAG = "fragment_currentlist_싸피"

class fragment_currentlist : Fragment() {
    private var _binding: FragmentCurrentlistBinding? = null
    private val binding: FragmentCurrentlistBinding
        get() = _binding!!
    private lateinit var callback: OnBackPressedCallback
    private lateinit var list : MutableList<MusicDTO>

    val musicviewmodel : MusicFileViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnCurrentList).isChecked=true
        musicviewmodel.selectedPlaylistName = ApplicationClass.sSharedPreferences.getCurSongList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCurrentlistBinding.inflate(inflater,container,false)
        var currentPlayListAdapter : CurrentPlayListAdapter
        list = ApplicationClass.sSharedPreferences.getSongList(musicviewmodel.selectedPlaylistName)
        musicviewmodel.selectedPlayList = PlayListDTO(musicviewmodel.selectedPlaylistName,list)
        
        if(!musicviewmodel.selectedPlaylistName.equals("")){
            //선택된 재생목록이 있는 경우
            binding.tvCurrentList.text = musicviewmodel.selectedPlaylistName
            for(i in 0..musicviewmodel.playList.size-1){
                if(musicviewmodel.playList.get(i).playlistname.equals(musicviewmodel.selectedPlaylistName)){
                    list = musicviewmodel.playList.get(i).songlist
                    break
                }
            }

            currentPlayListAdapter= CurrentPlayListAdapter(musicviewmodel.selectedPlayList.songlist)
        }
        else{
            //선택된 재생목록이 없는 경우 => 빈 리스트 전달
            binding.tvCurrentList.text="선택된 재생목록 없음"
            currentPlayListAdapter= CurrentPlayListAdapter(mutableListOf<MusicDTO>())
        }
        
        binding.rvCurrentPlayList.apply {
            adapter = currentPlayListAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration())
        }

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
    }

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
                Log.d(TAG, "bind: ${list.id} / ${list.albumId} / ${list.title} / ${list.artist} / ${list.genre}")
                title.text = list.title
                artist.text=list.artist
                genre.text=list.genre
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
                            builder.setCancelable(true)
                            builder.setPositiveButton("삭제") { _, _ ->
                                ApplicationClass.sSharedPreferences.deleteSongFromList(musicviewmodel.selectedPlaylistName,songlist[layoutPosition])
                                Log.d(TAG, "onCreateContextMenu: ${musicviewmodel.selectedPlaylistName} // ${songlist[layoutPosition].title}")
                                Log.d(TAG, "onCreateContextMenu: ${layoutPosition}")
                                binding.rvCurrentPlayList.adapter!!.notifyItemRemoved(layoutPosition)
                                ApplicationClass.sSharedPreferences.putSelectedSongPosition(layoutPosition)
                                list = ApplicationClass.sSharedPreferences.getSongList(musicviewmodel.selectedPlaylistName)
                                musicviewmodel.selectedPlayList.songlist.removeAt(layoutPosition)
                                Log.d(TAG, "onCreateContextMenu: list size : ${list.size}")
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentPlayListViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.playlist_songs_item, parent, false)
            return CurrentPlayListViewHolder(view).apply {
                //클릭 시 해당 곡 재생 기능 추가
                itemView.setOnClickListener {
                    musicviewmodel.selectedMusic = songlist[layoutPosition]
                    musicviewmodel.selectedMusicPosition = layoutPosition
                    ApplicationClass.sSharedPreferences.putSelectedSongPosition(layoutPosition)
                    requireActivity().apply {
                        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_song()).commit()
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: CurrentPlayListAdapter.CurrentPlayListViewHolder, position: Int) {
            holder.bind(songlist.get(position))
            Log.d(TAG, "onBindViewHolder: ${songlist} / ${songlist.get(position)} / ${songlist.size}")
            
        }

        override fun getItemCount(): Int {
            return songlist.size
        }
    }
}
