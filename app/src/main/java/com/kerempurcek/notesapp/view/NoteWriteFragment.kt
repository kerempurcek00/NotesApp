package com.kerempurcek.notesapp.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.kerempurcek.notesapp.R
import com.kerempurcek.notesapp.databinding.FragmentMainBinding
import com.kerempurcek.notesapp.databinding.FragmentNoteWriteBinding
import com.kerempurcek.notesapp.model.Notes
import com.kerempurcek.notesapp.roomdb.NotesDAO
import com.kerempurcek.notesapp.roomdb.NotesDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class NoteWriteFragment : Fragment() {
    private var _binding: FragmentNoteWriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    //galeriye gitmek için
    private lateinit var  activityResultLauncher: ActivityResultLauncher<Intent>




    //uygulamaya sürekli istek atıyoruz ya bizde tabi 3 tane var malzeme isim resim oluyor ama 200 300 istek yollandığında bu kod kullan at mantığı ile kullanıp siliyor
    private  val mDisposable =  CompositeDisposable()

    private  var secilengorsel : Uri? = null      // uri kaynak gösterir yeri nerde
    private var secilenbitmap : Bitmap? = null    // aldığı kaynağı jpg png görsele çeviriyor

    private lateinit var db:NotesDatabase
    private lateinit var notesDao:NotesDAO
    private var secilenNote : Notes? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),NotesDatabase::class.java,"Notes").build()
        notesDao = db.NotesDAO()
    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteWriteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener{selectImage(it)}
        binding.KaydetButton.setOnClickListener{save(it)}
        binding.SilButton.setOnClickListener{delete(it)}


        arguments?.let {

            val bilgi = NoteWriteFragmentArgs.fromBundle(it).bilgi

            if(bilgi=="yeni"){
                //yeni not eklenecek
                binding.SilButton.isEnabled=false // yeni not eklerken neden sil butonuna basılsın o yüzden sil butonu deaktif oldu
                binding.SilButton.setBackgroundColor(Color.GRAY)
                binding.KaydetButton.isEnabled=true //kaydet butonu aktif
            }else{
                //eski eklenen not gösterilecek

                binding.SilButton.isEnabled=true       //tam tersi olur çünkü eklenen notu silebiliriz ama tekrar kayıt edemeyiz
                binding.KaydetButton.isEnabled=false
                binding.KaydetButton.setBackgroundColor(Color.GRAY)
                // id ye göre veri göstermek
                val id = NoteWriteFragmentArgs.fromBundle(it).id
                mDisposable.add(
                    notesDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleresponse)
                )


            }

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }


    //FUNCTİONS

    fun save(view: View){
        val baslık = binding.HeaderText.text.toString()
        val notes_ = binding.editTextTextMultiLine.text.toString()

        // kullanıcı resim  seçip seçmemişmi kontrol sağlayalım  ve kaydedelim resmi
        if(secilenbitmap!=null){
            val kucukBitmap = kucukBitmapOlustur(secilenbitmap!!,300)     // !! koymak resim değişebilir veya değişken değişebilir
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()
            val notes = Notes(baslık,notes_,byteDizisi)

            //rxjava
            mDisposable.add( notesDao.insert(notes)
                .subscribeOn(Schedulers.io())   //io veri tabanı okuma internete gidip gelmek arka planda yapılan iş io=input output computation var buda cpu ya yüklenip büyük hesaplamalar yaparken kullanılır
                .observeOn(AndroidSchedulers.mainThread()) //ön planda gösterecek
                .subscribe(this::handleResponseforInsert) //bunun sonucunda ne olacağını bir fonskiyona atayabiliriz
            )

        }


    }
    private fun handleResponseforInsert(){
        //bir önceki fragmenta dön gibi bişey yazıp veri ekledikten sonra fragmenta döndürebiliriz.
        val action = NoteWriteFragmentDirections.actionNoteWriteFragmentToMainFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun delete(view: View){
        if(secilenNote != null){
            mDisposable.add(
                notesDao.delete(Notes = secilenNote!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseforInsert)
            )

        }

    }


    private fun handleresponse(note:Notes){
        //veritabanından verileri çekip textlerin içine yazdık
        binding.HeaderText.setText(note.Baslik)
        binding.editTextTextMultiLine.setText(note.Not)
        //arraydiziisi olan resmi bitmape çevirip göstermek
        val bitmap = BitmapFactory.decodeByteArray(note.Gorsel,0,note.Gorsel.size)  // 0 dan başla görsel boyutuna kadar olan 0 ve 1 leri al bitmape çevir
        binding.imageView.setImageBitmap(bitmap)
        secilenNote = note
    }



    fun selectImage(view: View){


        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU ) {
            //------ APİ 33 ÜSTÜYSE EĞER İZİN VERİP GALERİYE ERİŞMEK ----




            // checkSelfPermission izin vermişmi kontrol et
            //ContextCompat androidversiyonu kontrol et
            // PackageManager.PERMISSION_GRANTED  ---> izin vermek
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin verilmemiş, izin istememiz gerekiyor

                //ActivityCompat ----> Sürüm kontrolü yap
                //shouldShowRequestPermissionRationale -->  Kullanıcı bir izni reddettiğinde ve "Bir daha sorma" işaretlenmediyse, bu metod true döner. İzin istemeden önce kullanıcıya açıklama yapmanız gerekip gerekmediğini kontrol etmek için kullanılır.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    //snackbar göstermemiz lazım kullanıcan neden izin istiyoruz bir kez daha ona gösterip izin istememiz gerekli
                    Snackbar.make(
                        view,
                        "Galeriye ulaşıp görsel seçmemiz lazım!",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "İzin Ver", View.OnClickListener {
                            // izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                        }
                    ).show() // length ındefinite kullanıcı tıklamadan mesaj gitmez

                } else {
                    // izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            } else {
                //izin verilmiş ,galeriye gidebilirim
                val intentToGallery = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )  // ACTION_PICK = görsel alıcam
                activityResultLauncher.launch(intentToGallery)


            }



        }else{
            // -- API 33 VE ALTIYSA RESİMLERE ERİŞMEK  VE İZİN İSTEMEK


            // checkSelfPermission izin vermişmi kontrol et
            //ContextCompat androidversiyonu kontrol et
            // PackageManager.PERMISSION_GRANTED  ---> izin vermek
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin verilmemiş, izin istememiz gerekiyor

                //ActivityCompat ----> Sürüm kontrolü yap
                //shouldShowRequestPermissionRationale -->  Kullanıcı bir izni reddettiğinde ve "Bir daha sorma" işaretlenmediyse, bu metod true döner. İzin istemeden önce kullanıcıya açıklama yapmanız gerekip gerekmediğini kontrol etmek için kullanılır.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //snackbar göstermemiz lazım kullanıcan neden izin istiyoruz bir kez daha ona gösterip izin istememiz gerekli
                    Snackbar.make(
                        view,
                        "Galeriye ulaşıp görsel seçmemiz lazım!",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "İzin Ver", View.OnClickListener {
                            // izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                        }
                    ).show() // length ındefinite kullanıcı tıklamadan mesaj gitmez

                } else {
                    // izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            } else {
                //izin verilmiş ,galeriye gidebilirim
                val intentToGallery = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )  // ACTION_PICK = görsel alıcam
                activityResultLauncher.launch(intentToGallery)


            }



        }

    }

    private fun registerLauncher(){

        // galeriye gitmek
        //StartActivityResult  bu aktivitenin sonunda nolacak veya aktivite gerçekleştimi kontrol et
        // activityResultLauncher --> galeriye gitme kodu
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            // AppCompatActivity.RESULT_OK -->>> resim seçilmiş mi  seçildiyse ok seçilmediyse cancelled
            if(result.resultCode == AppCompatActivity.RESULT_OK ){
                val intentFromResult =result.data
                if(intentFromResult!=null) {
                    secilengorsel = intentFromResult.data


                    // try catch yapmamızın sebebi resmi kaydediyoruz telefona olduki sdkart çıktı hafıza değişti falan uygulamayı çökmesini engellemek için
                    try {
                        // Apı 28 den büyük olanlar ve olmayanlar için resmi imageview da göstermek
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                secilengorsel!!
                            )
                            secilenbitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenbitmap)
                        } else {
                            // alınan resmin urisini resim formatına dönüştürür
                            // requireActivity().contentResolver ------>>>>> requireActivity() mevcut aktiviteye erişirken, contentResolver, içerik sağlayıcılar (ContentProvider) aracılığıyla verileri okuma/yazma işlemlerini yapar. Örneğin, kişiler, medya veya dosyalar gibi içeriklere erişmek için kullanılır.
                            secilenbitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                secilengorsel
                            )
                            binding.imageView.setImageBitmap(secilenbitmap)
                        }
                    }catch(e:Exception){
                        println(e.localizedMessage)
                    }
                }


            }



        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->

            if(result){
                // doğruysa izin verildi
                // galeriye git
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)  // ACTION_PICK = görsel alıcam
                activityResultLauncher.launch(intentToGallery)
            }else {
                //izin verilmedi
                Toast.makeText(requireContext(),"İzin Verilmedi!!",Toast.LENGTH_LONG)

            }

        }
    }

    //resim boyutunu küçültmek gerekli çünkü veritabanına kayıt yapıcaz
    private fun kucukBitmapOlustur(kullanicininSectigiBitmap:Bitmap,maximumBoyut:Int):Bitmap{
        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height
        //resmin genişliği ve uzunluğuna böldük yatay mı dikey mi ve oranı nedir bulmak adına
        val resimOrani:Double = width.toDouble()/ height.toDouble()

        if(resimOrani>1){
            //resim yatay
            width = maximumBoyut
            val  kisaltilmisYukseklik = width/resimOrani               // resim genişliğine göre resmi bozmayacak derece yüksekliğini kısaltıyoruz
            height = kisaltilmisYukseklik.toInt()
        }else{
            //resim dikey
            // yukardakinin tam tersi olcak bu sefer yüksekliğe göre genişlik ayarlancak
            height = maximumBoyut
            val kisaltilmisGenislik = height * resimOrani   // * olmasının sebebi eğer bölersek genişlik büyür çünkü 1 den küçük çıkarsa resim oranı 0.5 mesala çıkarsa böldüğünde genişlik büyür çarparsan küçülür
            width = kisaltilmisGenislik.toInt()

        }



        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }




}