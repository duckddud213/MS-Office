package com.ssafy.final_pennant_preset

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentAddtoplaylistBinding
import com.ssafy.final_pennant_preset.DTO.MusicFileViewModel
import com.ssafy.final_pennant_preset.DTO.PlayListDTO
import com.ssafy.final_pennant_preset.DTO.checkboxData

private const val TAG = "fragment_totallist_싸피"

class fragment_addtoplaylist : Fragment() {
    private var _binding: FragmentAddtoplaylistBinding? = null
    private val binding: FragmentAddtoplaylistBinding
        get() = _binding!!

    val musicfileviewmodel: MusicFileViewModel by activityViewModels()
    private val playList = mutableListOf<PlayListDTO>()

    fun setTestData() {
        //        테스트용 데이터
        var dto = PlayListDTO(1, "test", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto)
        var dto2 = PlayListDTO(2, "test2", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto2)
        var dto3 = PlayListDTO(3, "test3", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto3)
        var dto4 = PlayListDTO(4, "test4", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto4)
        var dto5 = PlayListDTO(5, "test5", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto5)
        var dto6 = PlayListDTO(6, "test6", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto6)
        var dto7 = PlayListDTO(7, "tes7", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto7)
        var dto8 = PlayListDTO(8, "test8", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto8)
        var dto9 = PlayListDTO(9, "test9", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto9)
        var dto10 = PlayListDTO(10, "test10", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto10)
        var dto11 = PlayListDTO(11, "test11", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto11)
        var dto12 = PlayListDTO(12, "test12", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto12)
        var dto13 = PlayListDTO(13, "test13", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto13)
        var dto14 = PlayListDTO(14, "test14", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto14)
        var dto15 = PlayListDTO(15, "test15", musicfileviewmodel.MusicList)
        musicfileviewmodel.playList.add(dto15)
    }

    fun checkSameData(Id1: Long, Name1: String, Id2: Long, Name2: String): Boolean {
        if (Id1.equals(Id2) && Name1.equals(Name2)) {
            return true
        }

        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        musicfileviewmodel.playList.clear()
        setTestData()
        _binding = FragmentAddtoplaylistBinding.inflate(inflater, container, false)
        val allplaylistadapter = AllPlayListAdapter(musicfileviewmodel.playList)
        binding.lvAddPlaylist.apply {
            adapter = allplaylistadapter
            this.layoutManager = LinearLayoutManager(requireActivity())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var songInfo = musicfileviewmodel.selectedMusic

        binding.btnAddSongToSelectedPlaylist.setOnClickListener {
            Log.d(TAG, "onViewCreated: ${musicfileviewmodel.checkedPlayList.size}")

            for (i in 0..musicfileviewmodel.checkedPlayList.size - 1) {
                Log.d(TAG, "onViewCreated: ${i}번 데이터 => ${musicfileviewmodel.checkedPlayList[i]}")
            }

            for (i in 0..musicfileviewmodel.checkedPlayList.size - 1) {
                for (j in 0..musicfileviewmodel.playList.size - 1) {
                    //cId, cName : 체크한 항목 || pId, pName : 전체 재생 목록에 있는 항목
                    var cId = musicfileviewmodel.checkedPlayList[i].playlistId
                    var cName = musicfileviewmodel.checkedPlayList[i].playlistname
                    var pId = musicfileviewmodel.playList[j].playlistId
                    var pName = musicfileviewmodel.playList[j].playlistname

                    if(checkSameData(cId,cName,pId,pName)){
                        musicfileviewmodel.playList[j].songlist.add(songInfo)
                    }
                }
            }

            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_totallist()).commit()

            Toast.makeText(requireContext(),"재생 목록에 추가되었습니다.",Toast.LENGTH_SHORT).show()

        }

        binding.btnGoBacks.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.framecontainer,fragment_totallist()).commit()
        }
    }

    inner class AllPlayListAdapter(val playlists: MutableList<PlayListDTO>) :
        RecyclerView.Adapter<AllPlayListAdapter.AllPlayListViewHolder>() {

        inner class AllPlayListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var title = itemView.findViewById<TextView>(R.id.tvPlayLists)
            var checkBox = itemView.findViewById<CheckBox>(R.id.checkBox1)

            fun bind(playlist: PlayListDTO) {
                title.text = playlist.playlistname
                checkBox.setOnClickListener {
                    var checked =
                        checkboxData(playlist.playlistId, playlist.playlistname, checkBox.isChecked)
                    if (!checkBox.isChecked) {
                        for (i in 0..musicfileviewmodel.checkedPlayList.size - 1) {
                            if (checkSameData(
                                    musicfileviewmodel.checkedPlayList[i].playlistId,
                                    musicfileviewmodel.checkedPlayList[i].playlistname,
                                    checked.playlistId,
                                    checked.playlistname
                                )
                            ) {
                                musicfileviewmodel.checkedPlayList.removeAt(i)
                                Log.d(
                                    TAG,
                                    "bind: 현재 크기는 ${musicfileviewmodel.checkedPlayList.size}"
                                )
                                break
                            }
                        }
                    } else {
                        musicfileviewmodel.checkedPlayList.add(checked)
                    }
                    Log.d(TAG, "bind: check box 클릭됨 =>${adapterPosition} /  ${checkBox.isChecked}")
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlayListViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.playlist_listview_item, parent, false)
            return AllPlayListViewHolder(view).apply {
            }
        }

        override fun onBindViewHolder(
            holder: AllPlayListAdapter.AllPlayListViewHolder,
            position: Int
        ) {
            holder.bind(playlists.get(position))
        }

        override fun getItemCount(): Int {
            return playlists.size
        }
    }
}
