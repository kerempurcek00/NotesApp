package com.kerempurcek.notesapp.model



import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data  class Notes (
    @ColumnInfo("Başlık")
    var Baslik:String,
    @ColumnInfo("Not")
    var Not:String,
    @ColumnInfo("Görsel")
    var Gorsel:ByteArray  // görseller 0 1 olarak tutulur oda byte array dir

){
    // obje oluştururken otomatik id oluştursun diye body ye yazdık yukarı değil
    @PrimaryKey(autoGenerate =true)   // otomatik artan
    var id = 0


}

