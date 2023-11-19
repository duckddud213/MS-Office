package com.ssafy.final_pennant_preset

import android.content.Context
import android.os.Bundle
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentPlaylistBinding
import com.ssafy.final_pennant_preset.dto.MusicDTO
import com.ssafy.final_pennant_preset.dto.MusicFileViewModel
import com.ssafy.final_pennant_preset.dto.PlayListDTO
import com.ssafy.final_pennant_preset.config.ApplicationClass

private const val TAG = "fragment_playlist_싸피"

class fragment_playlist : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding
        get() = _binding!!

    val musicviewmodel: MusicFileViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPlayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().apply {
            //currentPlayList로 이동 후 onBackPressed 상황시 bottomNavItem focus 전환 위해 적용
            //isChecked는 하나의 항목에만 적용되므로 true로 변환될 아이템에 설정하면 나머지는 자동 false처리(따로 처리X)
            findViewById<BottomNavigationView>(R.id.bottomNavView).labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_AUTO
            findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnPlayList).isChecked=true
        }


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
                        addPlayList(str)
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

    override fun onDetach() {
        super.onDetach()
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

        for(i in 0..namelistarray.size-2){
            Log.d(TAG, "getPlayList: ${namelistarray.get(i)}")
            musicviewmodel.playList.add(PlayListDTO(namelistarray.get(i), ApplicationClass.sSharedPreferences.getSongList(namelistarray.get(i))))
        }
    }

    private fun addPlayList(name:String){
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

                            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                            builder.setTitle("재생목록 삭제")
                            builder.setMessage(
                                "재생 목록 [${playlists[layoutPosition].playlistname}]을 삭제하시겠습니까?"
                            )
                            builder.setCancelable(true)
                            builder.setPositiveButton("삭제") { _, _ ->
                                for(i in 0..musicviewmodel.playList.size-1){
                                    if(musicviewmodel.playList.get(i).playlistname.equals(playlists[layoutPosition].playlistname)){
                                        ApplicationClass.sSharedPreferences.deleteSongListName(playlists[layoutPosition].playlistname)
                                        musicviewmodel.playList.removeAt(i)
                                        binding.rvTotalPlayList.adapter!!.notifyItemRemoved(i)
                                        break
                                    }
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.totalplaylist_item, parent, false)
            return PlayListViewHolder(view).apply {
//                클릭 시 현재 재생 목록에 해당 재생 목록 전달 기능 추가
                itemView.setOnClickListener {
                    for(i in 0..musicviewmodel.playList.size-1){
                        if(musicviewmodel.playList.get(i).playlistname==playlists[layoutPosition].playlistname){
                            musicviewmodel.selectedPlaylistName = musicviewmodel.playList.get(i).playlistname
                            musicviewmodel.playList.get(i).songlist=ApplicationClass.sSharedPreferences.getSongList(musicviewmodel.selectedPlaylistName)

                            Log.d(TAG, "onCreateViewHolder: ${musicviewmodel.selectedPlaylistName}")
                            ApplicationClass.sSharedPreferences.putCurSongList(musicviewmodel.selectedPlaylistName)
                            Log.d(TAG, "onCreateViewHolder: getCur : ${ApplicationClass.sSharedPreferences.getCurSongList()}")

                            requireActivity().apply {
                                findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnCurrentList).isChecked=true
                                supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_currentlist()).addToBackStack("moveToCurrentPlayList").commit()
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
