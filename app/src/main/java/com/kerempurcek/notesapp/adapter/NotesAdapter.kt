package com.kerempurcek.notesapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.kerempurcek.notesapp.databinding.RecylerRowBinding
import com.kerempurcek.notesapp.model.Notes
import com.kerempurcek.notesapp.view.MainFragmentDirections

class NotesAdapter(val Notlar:List<Notes>):RecyclerView.Adapter<NotesAdapter.notesHolder>() {

    class notesHolder(val binding:RecylerRowBinding ):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): notesHolder {
        val recylerRowBinding = RecylerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return notesHolder(recylerRowBinding)
    }

    override fun getItemCount(): Int {
        return Notlar.size
    }

    override fun onBindViewHolder(holder: notesHolder, position: Int) {
        holder.binding.textView2.text = Notlar[position].Baslik
        holder.itemView.setOnClickListener{
            val action =MainFragmentDirections.actionMainFragmentToNoteWriteFragment("eski",Notlar[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}