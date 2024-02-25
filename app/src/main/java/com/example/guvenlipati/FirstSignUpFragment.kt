package com.example.guvenlipati

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class FirstSignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.signUpButton).setOnClickListener {

            auth = FirebaseAuth.getInstance()

            val userEmail = view.findViewById<EditText>(R.id.editTextEmail)
            val userPassword = view.findViewById<EditText>(R.id.editTextPassword)
            val userConfirmPassword = view.findViewById<EditText>(R.id.editTextConfirmPassword)

            if (userEmail.text.toString().isEmpty() || !controlEmail(userEmail.text.toString())) {
                showToast("Hatalı ya da eksik E-posta!")
                return@setOnClickListener
            }

            if (userPassword.text.toString().length < 8) {
                showToast("Şifre 8 karakterden kısa olamaz!")
                return@setOnClickListener
            }

            if (userPassword.text.toString() != userConfirmPassword.text.toString()) {
                showToast("Şifreler uyuşmuyor!")
                userPassword.setText("")
                userConfirmPassword.setText("")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(
                userEmail.text.toString(),
                userPassword.text.toString()
            )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        (activity as MainActivity).goSecondSignUpFragment()
                    } else {
                        showToast("Bilinmeyen hata!")
                    }
                }
        }

        view.findViewById<ImageButton>(R.id.backToSplash).setOnClickListener{
            (activity as MainActivity).goSplashFragment()
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun controlEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}