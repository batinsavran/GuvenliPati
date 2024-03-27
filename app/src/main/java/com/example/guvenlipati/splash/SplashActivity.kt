package com.example.guvenlipati.splash

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guvenlipati.R
import com.example.guvenlipati.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

private lateinit var auth: FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goHomeActivity()
            Toast.makeText(this, auth.currentUser?.email, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    fun goFirstSignUpFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainerView, FirstSignUpFragment
                    ()
            )
            .commit()
    }

    fun goSecondSignUpFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, SecondSignUpFragment())
            .commit()
    }

    fun goLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, LoginFragment())
            .commit()
    }

    fun goSplashFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, SplashFragment())
            .commit()
    }

    fun goHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


}