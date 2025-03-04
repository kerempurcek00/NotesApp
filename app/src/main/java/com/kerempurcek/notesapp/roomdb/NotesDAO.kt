package com.kerempurcek.notesapp.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kerempurcek.notesapp.model.Notes
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
@Dao
interface NotesDAO {
    @Query("select * From Notes")
    fun getAll(): Flowable<List<Notes>>    // Flowable threading konusu rxjava ile ilgili demek istediği şey arka planda yap istediğim zaman çalıştır bana göster

    // id ye göre liste elemanları getirme
    @Query("select * From Notes where id=:id")
    fun findById(id:Int) : Flowable<Notes>

    @Insert
    fun insert(Notes: Notes) : Completable    // bana bişey göstermicek eklicek db ye

    @Delete
    fun delete(Notes: Notes) : Completable



}