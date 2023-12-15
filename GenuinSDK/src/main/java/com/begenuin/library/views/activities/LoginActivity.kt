package com.begenuin.library.views.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.begenuin.library.R
import com.begenuin.library.SDKInitiate
import com.begenuin.library.views.DunkidonutsScreen
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private lateinit var txtPassword: AppCompatEditText
    private lateinit var txtEmail: AppCompatEditText
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        txtPassword = findViewById(R.id.txtPassword)
        txtEmail = findViewById(R.id.txtEmail)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener{
            validate()
//
//            val redirectDunkyDonut = Intent(this, DunkidonutsScreen::class.java)
//            startActivity(redirectDunkyDonut)
        }

    }

    private fun validate() {
        hideKeyboard(this)
        val passwordText: String = txtPassword.text.toString().trim { it <= ' ' }
        val email: String = txtEmail.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(
                this,
                getString(R.string.empty_email),
                Toast.LENGTH_SHORT
            ).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                this,
                getString(R.string.email_invalid),
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(passwordText) || !passwordText.contentEquals("123")) {
            Toast.makeText(
                this,
                getString(R.string.pwd_validation),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            SDKInitiate.emailId = email
            val redirectDunkyDonut = Intent(this, DunkidonutsScreen::class.java)
            startActivity(redirectDunkyDonut)
        }
    }
    private fun hideKeyboard(mContext: Activity) {
        try {
            //if (mContext.currentFocus != null) {
                val imm = mContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(mContext.currentFocus!!.windowToken, 0)
            //}
        } catch (e: Exception) {
            Log.d("Error", e.message.toString())
        }
    }
}