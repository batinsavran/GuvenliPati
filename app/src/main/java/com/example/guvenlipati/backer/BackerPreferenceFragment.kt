package com.example.guvenlipati.backer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.guvenlipati.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BackerPreferenceFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_backer_preference, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var dogJob = false
        var catJob = false
        var birdJob = false
        var userAvailability: Int? = null
        var homeJob = false
        var feedingJob = false
        var walkingJob = false
        val dogs = view.findViewById<Chip>(R.id.dogs)
        val cats = view.findViewById<Chip>(R.id.cats)
        val birds = view.findViewById<Chip>(R.id.birds)
        val midWeek = view.findViewById<Chip>(R.id.midWeek)
        val weekEnd = view.findViewById<Chip>(R.id.weekEnd)
        val allDays = view.findViewById<Chip>(R.id.allDays)
        val home = view.findViewById<Chip>(R.id.job1)
        val feeding = view.findViewById<Chip>(R.id.job2)
        val walking = view.findViewById<Chip>(R.id.job3)
        val homeMoney = view.findViewById<EditText>(R.id.editTextBoarding)
        val feedingMoney = view.findViewById<EditText>(R.id.editTextBoarding2)
        val walkingMoney = view.findViewById<EditText>(R.id.editTextBoarding3)
        val saveButton = view.findViewById<Button>(R.id.JobOptionButton)

        dogs.setOnCheckedChangeListener { _, isChecked ->
            dogJob = isChecked
        }

        cats.setOnCheckedChangeListener { _, isChecked ->
            catJob = isChecked
        }

        birds.setOnCheckedChangeListener { _, isChecked ->
            birdJob = isChecked
        }

        midWeek.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userAvailability = 1
            }
        }

        weekEnd.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userAvailability = 2
            }
        }

        allDays.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userAvailability = 3
            }
        }

        home.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                homeJob = true
                homeMoney.isEnabled = true
            } else {
                homeJob = false
                homeMoney.isEnabled = false
                homeMoney.setText("0")
            }
        }

        feeding.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                feedingJob = true
                feedingMoney.isEnabled = true
            } else {
                feedingJob = false
                feedingMoney.isEnabled = false
                feedingMoney.setText("0")
            }
        }

        walking.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                walkingJob = true
                walkingMoney.isEnabled = true
            } else {
                walkingJob = false
                walkingMoney.isEnabled = false
                walkingMoney.setText("0")
            }
        }

        saveButton.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            databaseReference = FirebaseDatabase.getInstance().getReference("identifies")

            if (!dogs.isChecked && !cats.isChecked && !birds.isChecked) {
                showToast("Lütfen En Az Bir Hayvan Seçiniz!")
                return@setOnClickListener
            }
            if (!midWeek.isChecked && !weekEnd.isChecked && !allDays.isChecked) {
                showToast("Lütfen Bir Müsaitlik Durumu Seçiniz!")
                return@setOnClickListener
            }

            if (!home.isChecked && !feeding.isChecked && !walking.isChecked) {
                showToast("Lütfen Bir İş Seçiniz!")
                return@setOnClickListener
            }

            if (home.isChecked) {
                if (homeMoney.text.toString().isEmpty() || homeMoney.text.toString().toInt() <= 0) {
                    showToast("Lütfen Bir Tutar Giriniz!")
                    return@setOnClickListener
                }
            }

            if (feeding.isChecked) {
                if (feedingMoney.text.toString().isEmpty() || feedingMoney.text.toString()
                        .toInt() <= 0
                ) {
                    showToast("Lütfen Bir Tutar Giriniz!")
                    return@setOnClickListener
                }
            }

            if (walking.isChecked) {
                if (walkingMoney.text.toString().isEmpty() || walkingMoney.text.toString()
                        .toInt() <= 0
                ) {
                    showToast("Lütfen Bir Tutar Giriniz!")
                    return@setOnClickListener

                }
            }

            databaseReference = FirebaseDatabase.getInstance().getReference("identifies")
                .child(auth.currentUser!!.uid)

            databaseReference.child("dogBacker").setValue(dogJob)
            databaseReference.child("catBacker").setValue(catJob)
            databaseReference.child("birdBacker").setValue(birdJob)
            databaseReference.child("userAvailability").setValue(userAvailability)
            databaseReference.child("homeJob").setValue(homeJob)
            databaseReference.child("feedingJob").setValue(feedingJob)
            databaseReference.child("walkingJob").setValue(walkingJob)
            databaseReference.child("homeMoney").setValue(homeMoney.text.toString().toInt())
            databaseReference.child("feedingMoney").setValue(feedingMoney.text.toString().toInt())
            databaseReference.child("walkingMoney").setValue(walkingMoney.text.toString().toInt())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showBottomSheet()
                    } else {
                        showToast("Kayıt Başarısız")
                    }
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_add_backer, null)
        view.findViewById<Button>(R.id.backToMain).setOnClickListener {
            requireActivity().finish()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }

}