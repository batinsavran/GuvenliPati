package com.example.guvenlipati.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.guvenlipati.R
import com.example.guvenlipati.databinding.FragmentPaymentBinding
import com.example.guvenlipati.home.HomeActivity
import com.example.guvenlipati.models.Offer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTextCardHolderName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = s.toString()
                if (currentText.length > 20) {
                    binding.editTextCardHolderName.setText(currentText.substring(0, 20))
                    binding.editTextCardHolderName.setSelection(20)
                } else {
                    binding.cardName.text = currentText
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


        binding.editTextCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Önceki metin değişmeden önce yapılacak işlemler (Opsiyonel)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Metin değiştiğinde yapılacak işlemler
                val currentText = s.toString()
                if (currentText.length <= 19) {
                    val formattedText = formatCreditCardNumber(currentText)
                    binding.editTextCardNumber.removeTextChangedListener(this)
                    binding.editTextCardNumber.setText(formattedText)
                    binding.editTextCardNumber.setSelection(formattedText.length)
                    binding.editTextCardNumber.addTextChangedListener(this)
                    binding.creditCardNumber.text = formattedText
                } else {
                    binding.editTextCardNumber.setText(currentText.substring(0, 19))
                    binding.editTextCardNumber.setSelection(19)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            private fun formatCreditCardNumber(text: String): String {
                val trimmed = text.replace("\\s+".toRegex(), "") // Boşlukları kaldır
                val chunked = trimmed.chunked(4) // 4 karakterlik parçalara böl
                return chunked.joinToString(" ") // Her 4 karakter arasına boşluk ekle
            }
        })

        binding.editTextExpDate.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private var mm = ""
            private var yy = ""
            private var isDeleting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                isDeleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val clean = it.toString().replace("[^\\d.]|\\.".toRegex(), "")
                    val cleanC = current.replace("[^\\d.]|\\.".toRegex(), "")

                    var cl = clean.length
                    var sel = cl
                    var i = 2
                    while (i <= cl && i < 4) {
                        sel++
                        i += 2
                    }
                    if (clean == cleanC) sel--

                    if (cl <= 2) {
                        mm = clean
                    } else {
                        mm = clean.substring(0, 2)
                        yy = clean.substring(2)
                    }

                    if (mm.length < 2) {
                        mm = mm
                    }
                    if (yy.length > 2) {
                        yy = yy.substring(0, 2)
                    } else if (yy.length < 2 && cl > 2) {
                        yy = yy
                    }

                    current = if (cl <= 2 || isDeleting) {
                        mm
                    } else {
                        "$mm/$yy"
                    }
                    if (it.toString() != current) {
                        binding.editTextExpDate.setText(current)
                        binding.editTextExpDate.setSelection(if (sel < current.length) sel else current.length)
                    }
                }
                binding.cardDate.text = "$mm/$yy"
                if (binding.editTextExpDate.text.isEmpty()) {
                    binding.cardDate.text = "07/30"
                }
            }
        })


        binding.editTextCVV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 3) {
                        binding.editTextCVV.setText(s.subSequence(0, 3))
                        binding.editTextCVV.setSelection(3)
                    }
                }
            }
        })


        binding.ConfirmPaymentButton.setOnClickListener {
            if (binding.editTextCardHolderName.text.isEmpty()){
                showToast("İSİM SOYİSİM BOŞ GEÇİLEMEZ")
            }
            else if (binding.editTextCardNumber.text.length !=19){
                showToast("KART NUMARASINI TAM GİRİNİZ")
            }
            else if (binding.editTextExpDate.text.isEmpty()){
                showToast("KARTIN SON KULLANIM TARİHİ BOŞ GEÇİLEMEZ")
            }
            else if (binding.editTextCVV.text.length !=3){
                showToast("GÜVENLİK KODUNU TAM GİRİNİZ")
            }
            else if (!binding.checkBox.isChecked || !binding.checkBox2.isChecked || !binding.checkBox3.isChecked){
                showToast("SÖZLEŞMELERİ KABUL ETMENİZ GEREKİYOR")
            }
            else{
                val offerId = activity?.intent?.getStringExtra("offerId")
                val jobId = activity?.intent?.getStringExtra("jobId")

                val databaseReference =
                    offerId?.let { it1 ->
                        FirebaseDatabase.getInstance().getReference("offers").child(
                            it1
                        )
                    }

                val databaseReference2 =
                    jobId?.let { it1 ->
                        FirebaseDatabase.getInstance().getReference("jobs").child(
                            it1
                        )
                    }

                databaseReference?.updateChildren(
                    mapOf(
                        "priceStatus" to true
                    )
                )?.addOnSuccessListener {
                    showToast("ÖDEME BAŞARILI")
                    val intent = Intent(context,HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                    ?.addOnFailureListener {
                        showToast("ÖDEME BAŞARISIZ")
                    }

                databaseReference2?.updateChildren(
                    mapOf(
                        "jobStatus" to false
                    )
                )
            }

        }

    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottomsheet_payment, null)
        view.findViewById<Button>(R.id.backToMain).setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }
}