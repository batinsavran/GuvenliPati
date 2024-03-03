package com.example.guvenlipati

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException

private lateinit var firebaseUser: FirebaseUser
private lateinit var databaseReference: DatabaseReference
private lateinit var auth: FirebaseAuth
private lateinit var getContent: ActivityResultLauncher<Intent>
private var request: Int = 2020
private var filePath: Uri? = null
private lateinit var storage: FirebaseStorage
private lateinit var strgRef: StorageReference
private var imageUrl: String = ""


class RegisterPetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet)

        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser!!


        val petType = intent.getStringExtra("petType")

        val buttonPetFemale = findViewById<Button>(R.id.buttonPetFemale)
        val buttonPetMale = findViewById<Button>(R.id.buttonPetMale)
        val buttonPetVaccine = findViewById<Button>(R.id.buttonPetVaccine)
        val buttonPetUnVaccine = findViewById<Button>(R.id.buttonPetUnVaccine)
        val editTextPetName = findViewById<EditText>(R.id.editTextPetName)
        val editTextPetWeight = findViewById<EditText>(R.id.editTextWeight)
        val petAgeCombo = findViewById<AutoCompleteTextView>(R.id.ageCombo)
        val petTypeCombo = findViewById<AutoCompleteTextView>(R.id.typeCombo)
        var petGender: Boolean? = null
        var petVaccine: Boolean? = null
        val editTextAbout = findViewById<EditText>(R.id.editTextAbout)
        val addPetButton = findViewById<Button>(R.id.petRegisterButton)
        val buttonPaw = findViewById<ImageView>(R.id.buttonPaw2)
        val progressCard = findViewById<CardView>(R.id.progressCard)
        val backButton=findViewById<ImageButton>(R.id.backToSplash)
        val vaccineImage=findViewById<ImageView>(R.id.vaccine)
        val unVaccineImage=findViewById<ImageView>(R.id.unVaccine)


        backButton.setOnClickListener{
            showMaterialDialog()
        }


        when (petType) {
            "dog" -> {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    resources.getStringArray(R.array.dog_types_array)
                )
                petTypeCombo.setAdapter(adapter)
            }

            "cat" -> {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    resources.getStringArray(R.array.cat_types_array)
                )
                petTypeCombo.setAdapter(adapter)
            }

            "bird" -> {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    resources.getStringArray(R.array.bird_types_array)
                )
                petTypeCombo.setAdapter(adapter)
            }

            else -> {
                finish()
            }
        }


        buttonPetFemale.setOnClickListener {
            petGender = true
            selectMethod(buttonPetFemale, buttonPetMale)
        }

        buttonPetMale.setOnClickListener {
            petGender = false
            selectMethod(buttonPetMale, buttonPetFemale)
        }

        buttonPetVaccine.setOnClickListener {
            petVaccine = true
            selectMethod(buttonPetVaccine, buttonPetUnVaccine)
            vaccineImage.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            unVaccineImage.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        }

        buttonPetUnVaccine.setOnClickListener {
            petVaccine = false
            selectMethod(buttonPetUnVaccine, buttonPetVaccine)
            unVaccineImage.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            vaccineImage.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        }

        getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                onActivityResult(request, result.resultCode, result.data)
            }

        storage = Firebase.storage
        strgRef = storage.reference

        findViewById<ImageButton>(R.id.buttonAddProfileImage).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            getContent.launch(Intent.createChooser(intent, "Select Profile Image"))
        }

        addPetButton.setOnClickListener {
            databaseReference =
                FirebaseDatabase.getInstance().getReference("pets")
                    .child(firebaseUser.uid + editTextPetName.text.toString())

            if (editTextPetName.text.toString().isEmpty()) {
                showToast("Lütfen ad giriniz!")
                return@setOnClickListener
            }

            if (editTextPetWeight.text.toString().isEmpty()) {
                showToast("Lütfen ağırlık giriniz!")
                return@setOnClickListener
            }

            if (petAgeCombo.text.toString().isEmpty()) {
                showToast("Lütfen yaş giriniz!")
                return@setOnClickListener
            }

            if (petTypeCombo.text.toString().isEmpty()) {
                showToast("Lütfen tür giriniz!")
                return@setOnClickListener
            }

            if (petGender == null) {
                showToast("Lütfen cinsiyet seçiniz!")
                return@setOnClickListener
            }

            if (petVaccine == null) {
                showToast("Lütfen aşı bilgisi seçiniz!")
                return@setOnClickListener
            }

            if (editTextAbout.text.toString().isEmpty()) {
                showToast("Lütfen tüm alanları doldurunuz!")
                return@setOnClickListener
            }

            addPetButton.visibility = View.INVISIBLE
            progressCard.visibility = View.VISIBLE
            buttonPaw.visibility = View.INVISIBLE

            val hashMap: HashMap<String, Any> = HashMap()
            hashMap["userId"] = firebaseUser.uid
            hashMap["petPhoto"] = imageUrl
            hashMap["petSpecies"] = petType.toString()
            hashMap["petName"] = editTextPetName.text.toString()
            hashMap["petWeight"] = editTextPetWeight.text.toString()
            hashMap["petAge"] = petAgeCombo.text.toString()
            hashMap["petBreed"] = petTypeCombo.text.toString()
            hashMap["petGender"] = petGender!!
            hashMap["petVaccinate"] = petVaccine!!
            hashMap["petAbout"] = editTextAbout.text.toString()
            hashMap["petAdoptionStatus"] = false
            hashMap["petId"]= firebaseUser.uid + editTextPetName.text.toString()

            databaseReference.setValue(hashMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addPetButton.visibility = View.VISIBLE
                    progressCard.visibility = View.INVISIBLE
                    buttonPaw.visibility = View.VISIBLE
                    showBottomSheet()
                } else {
                    showToast("Hatalı işlem!")
                    addPetButton.visibility = View.VISIBLE
                    progressCard.visibility = View.INVISIBLE
                    buttonPaw.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showMaterialDialog(){
        MaterialAlertDialogBuilder(this)
            .setTitle("Emin Misiniz?")
            .setMessage("Eğer geri dönerseniz kaydınız silinecektir.")
            .setBackground(ContextCompat.getDrawable(this, R.drawable.background_dialog))
            .setPositiveButton("Sil") { _, _ ->
                showToast("Kaydınız iptal edildi.")
                finish()
            }
            .setNegativeButton("İptal") { _, _ ->
                showToast("İptal Edildi")
            }
            .show()
    }




    private fun selectMethod(selected: Button, unselected: Button) {
        selected.setBackgroundResource(R.drawable.sign2_edittext_bg2)
        selected.setTextColor(Color.WHITE)
        unselected.setBackgroundResource(R.drawable.sign2_edittext_bg)
        unselected.setTextColor(Color.BLACK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == request && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                showToast("Fotoğraf yükleniyor...")

                val inputStream = contentResolver.openInputStream(filePath!!)
                val exif = ExifInterface(inputStream!!)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val originalBitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, filePath)


                val rotationAngle = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }


                val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    0,
                    0,
                    originalBitmap.width,
                    originalBitmap.height,
                    matrix,
                    true
                )

                val imageStream = ByteArrayOutputStream()

                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, imageStream)

                val imageArray = imageStream.toByteArray()
                val imageFileName = "image_${System.currentTimeMillis()}.jpg"
                val ref: StorageReference = strgRef.child("pets/${firebaseUser.uid}/$imageFileName")
                ref.putBytes(imageArray)
                    .addOnSuccessListener {
                        showToast("Fotoğraf yüklendi!")
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            imageUrl = uri.toString()
                        }
                    }
                    .addOnFailureListener {
                        showToast("Başarısız, lütfen yeniden deneyin!")
                    }

                findViewById<CircleImageView>(R.id.circleImageProfilePhoto)
                    ?.setImageBitmap(rotatedBitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showBottomSheet(){
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet,null)
        view.findViewById<Button>(R.id.backToMain).setOnClickListener {
            finish()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showMaterialDialog()
    }

}



