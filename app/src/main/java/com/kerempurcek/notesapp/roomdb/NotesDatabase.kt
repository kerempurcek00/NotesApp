package com.kerempurcek.notesapp.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kerempurcek.notesapp.model.Notes


@Database(entities = [Notes::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun NotesDAO(): NotesDAO
}
