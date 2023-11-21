package com.ssafy.final_pennant_preset

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ssafy.final_pennant.R
import com.ssafy.final_pennant.databinding.FragmentPlaylistBinding
import com.ssafy.final_pennant.databinding.FragmentServerBinding
import com.ssafy.final_pennant.databinding.FragmentSongBinding
import com.ssafy.final_pennant_preset.config.ApplicationClass
import com.ssafy.final_pennant_preset.dto.PlayListDTO


private const val TAG = "fragment_server_싸피"
class fragment_server : Fragment() {

    private var _binding: FragmentServerBinding? = null
    private val binding: FragmentServerBinding
        get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView).menu.findItem(R.id.btnConnectServer).isChecked = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyOptionsMenu() {
        _binding = null
        super.onDestroyOptionsMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabPushManage.setOnClickListener {
            showDialogPush()
        }

        initGenreList()
    }

    private fun initGenreList() {
        val genreList = mutableListOf(
            ApplicationClass.CHANNEL_DANCE
            , ApplicationClass.CHANNEL_BALLAD
            , ApplicationClass.CHANNEL_ROCK
            , ApplicationClass.CHANNEL_POP
            , ApplicationClass.CHANNEL_IDOL
        )

        val genreListAdapter = GenreListAdapter(genreList)
        genreListAdapter.myItemClickListener = object : GenreListAdapter.ItemClickListener{
            override fun onMyClick(view: View, data: String, position: Int) {
                Toast.makeText(requireContext(), data, Toast.LENGTH_SHORT).show()
                val genreFrag = fragment_server_genre
                parentFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.framecontainer, genreFrag.newInstance(data))
                    .commit()
            }
        }

        binding.rvServerGenre.apply {
            adapter = genreListAdapter
            this.layoutManager = LinearLayoutManager(requireActivity())
            addItemDecoration(CustomItemDecoration())
        }
    }

//    // 네트워크로 데이터 전송, Retrofit 객체 생성
//    Retrofit retrofit = NetworkClient.getRetrofitClient(AddSnsActivity.this);
//    SnsApi api = retrofit.create(SnsApi.class);
//
//// 멀티파트로 파일을 보내는 경우 파라미터 생성 방법, (파일명, 파일 타입)
//    RequestBody fileBody = RequestBody.create(photoFile, MediaType.parse("image/*"));
//
//    MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", photoFile.getName(), fileBody);
//
//// 멀티파트를 텍스트로 보내는 경우 파라미터 보내는 방법 ( 내용, 텍스트 타입)
//    RequestBody contentBody = RequestBody.create(content, MediaType.parse("text/plain"));


    class GenreListAdapter(val genreList: MutableList<String>) :

        RecyclerView.Adapter<GenreListAdapter.CustomViewHolder>() {
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnCreateContextMenuListener {

            var tv_genre = itemView.findViewById<TextView>(R.id.tvPlayListTitle)

            fun bind(genre: String) {
                tv_genre.text = genre
                tv_genre.setOnClickListener {
                    myItemClickListener.onMyClick(it, genre, layoutPosition)
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
            fun onMyClick(view: View, data: String, position: Int)
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
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showDialogPush() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater.inflate(R.layout.dialog_pushmanage, null)
        val switch_dance = inflater.findViewById<Switch>(R.id.switch_dance)
        val switch_ballad = inflater.findViewById<Switch>(R.id.switch_ballad)
        val switch_pop = inflater.findViewById<Switch>(R.id.switch_pop)
        val switch_idol = inflater.findViewById<Switch>(R.id.switch_idol)
        val switch_rock = inflater.findViewById<Switch>(R.id.switch_rock)

        switch_dance.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_DANCE)
        switch_ballad.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_BALLAD)
        switch_pop.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_POP)
        switch_idol.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_IDOL)
        switch_rock.isChecked = ApplicationClass.sSharedPreferences.getNotification(ApplicationClass.CHANNEL_ROCK)

        switch_dance.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_DANCE, isChecked)
            ApplicationClass.receive_notification_dance = isChecked
        }

        switch_ballad.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_BALLAD, isChecked)
            ApplicationClass.receive_notification_ballad = isChecked
        }

        switch_pop.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_POP, isChecked)
            ApplicationClass.receive_notification_pop = isChecked
        }

        switch_idol.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_IDOL, isChecked)
            ApplicationClass.receive_notification_idol = isChecked
        }

        switch_rock.setOnCheckedChangeListener { _, isChecked ->
            ApplicationClass.sSharedPreferences.putNotification(ApplicationClass.CHANNEL_ROCK, isChecked)
            ApplicationClass.receive_notification_rock = isChecked
        }

        builder.apply {
            setView(inflater)
            setPositiveButton("확인") { dialog, _ ->
            }
        }

        builder.create().show()
    }
}