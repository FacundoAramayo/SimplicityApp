package com.simplicityapp.modules.start.ui

import android.Manifest
import android.content.Intent
import android.graphics.drawable.AnimationDrawable


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import com.simplicityapp.R
import com.simplicityapp.modules.main.ui.ActivityMain
import com.simplicityapp.base.data.SharedPref
import com.simplicityapp.base.utils.PermissionUtil

import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.simplicityapp.base.data.Constant
import com.simplicityapp.base.ui.ActivityInterface
import com.simplicityapp.base.utils.Tools


/**
 * A login screen that offers login via Google Sign-In after accept Terms & Conditions.
 */
class ActivityLogin : AppCompatActivity(), ActivityInterface, View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var checkBox: CheckBox? = null
    private var sharedPref: SharedPref? = null
    private var signInButton: SignInButton? = null
    private var gso: GoogleSignInOptions? = null
    private var mainLayout: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPref = SharedPref(this)

        initUI()
        initListeners()

        configureGoogleSignIn()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso!!)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun initUI() {
        signInButton = findViewById(R.id.sign_in_button)
        signInButton?.isEnabled = false
        checkBox = findViewById(R.id.checkBox)
        mainLayout = findViewById(R.id.main_layout)
        startBackgroundAnimation()
    }

    private fun startBackgroundAnimation() {
        val animationDrawable: AnimationDrawable = mainLayout?.background as AnimationDrawable
        animationDrawable.setExitFadeDuration(3500)
        animationDrawable.start()
    }

    override fun initListeners() {
        signInButton?.setOnClickListener(this)
        checkBox?.setOnClickListener { checkBoxListener() }
    }

    override fun getArguments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                updateUI(null)
            }

        }
    }

    //TODO: Configurar web_client_id -> Verificar si esto es lo implementado por nosotros
    private fun configureGoogleSignIn() {
        Log.d("Web_client_id", getString(R.string.web_client_id))
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth?.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    fun checkingPermissions(): Boolean {
        return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.READ_CONTACTS) &&
                checkPermission(Manifest.permission.GET_ACCOUNTS)
    }

    private fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
    }

    private fun checkBoxListener() {
        if (checkBox?.isChecked!!) {
            if (Tools.needRequestPermission()) {
                val permission = PermissionUtil.getDeniedPermission(this@ActivityLogin)
                if (permission.size != 0) {
                    requestPermissions(permission, 200)
                } else {
                    signInButton?.isEnabled = true
                }
            }
        } else {
            signInButton?.isEnabled = false
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mAuth?.signOut()

        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this
        ) { updateUI(null) }
    }

    private fun revokeAccess() {
        mAuth?.signOut()

        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this
        ) { updateUI(null) }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {

            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
            val i = Intent(this@ActivityLogin, ActivityMain::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.sign_in_button) {
            if (checkBox!!.isChecked) {
                signIn()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 200) {
            for (perm in permissions) {
                val rationale = shouldShowRequestPermissionRationale(perm)
                sharedPref!!.setNeverAskAgain(perm, !rationale)
            }

        }
        Log.d("LOG-", "Result")
        if (!checkingPermissions()) {
            checkBox?.isChecked = false
        } else {
            checkBox?.isChecked = true
            signInButton!!.isEnabled = true
        }
    }

    companion object {
        private val TAG = "ActivityLogin"
        private val RC_SIGN_IN = 9001
    }
}