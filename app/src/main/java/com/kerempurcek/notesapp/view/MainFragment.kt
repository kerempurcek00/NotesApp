package com.kerempurcek.notesapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kerempurcek.notesapp.R
import com.kerempurcek.notesapp.adapter.NotesAdapter
import com.kerempurcek.notesapp.databinding.FragmentMainBinding
import com.kerempurcek.notesapp.model.Notes
import com.kerempurcek.notesapp.roomdb.NotesDAO
import com.kerempurcek.notesapp.roomdb.NotesDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class MainFragment : Fragment() {

    private val mDisposable = CompositeDisposable()
    private lateinit var db: NotesDatabase
    private lateinit var notesDao: NotesDAO
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(requireContext(),NotesDatabase::class.java,"Notes").build()
        notesDao = db.NotesDAO()

    }
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addButton.setOnClickListener{addbutton(it)}

        binding.NotesRecylerView.layoutManager = LinearLayoutManager(requireContext())
        verilerial()




    }
    private fun verilerial(){
        mDisposable.add(
            notesDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )

    }

    private fun handleResponse(Notlar:List<Notes>){
        val adapter = NotesAdapter(Notlar)
        binding.NotesRecylerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }


    //FUNCTİONS

    fun addbutton(view:View){
        val action = MainFragmentDirections.actionMainFragmentToNoteWriteFragment(bilgi="yeni",id=0)
        Navigation.findNavController(view).navigate(action)

    }

}