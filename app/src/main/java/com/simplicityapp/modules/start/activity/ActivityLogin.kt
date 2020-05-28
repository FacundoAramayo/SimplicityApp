package com.simplicityapp.modules.start.activity

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
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
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
import com.simplicityapp.base.persistence.preferences.SharedPref
import com.simplicityapp.base.utils.PermissionUtil
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.Constant.LOG_TAG
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.BuildConfig
import com.simplicityapp.modules.main.activity.ActivityMain
import com.simplicityapp.R


/**
 * A login screen that offers login via Google Sign-In after accept Terms & Conditions.
 */
class ActivityLogin : AppCompatActivity(), BaseActivity, View.OnClickListener {

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

        sharedPref =
            SharedPref(this)

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
                Log.w(LOG_TAG, "Google sign in failed", e)
                updateUI(null)
            }

        }
    }


    private fun configureGoogleSignIn() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.FIREBASE_CLIENT_ID)
            .requestEmail()
            .build()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(LOG_TAG, "signInWithCredential: success")
                    val user = mAuth?.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(LOG_TAG, "signInWithCredential: failure", task.exception)
                    Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun checkingPermissions(): Boolean {
        return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
    }

    private fun checkBoxListener() {
        if (checkBox?.isChecked!!) {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.TERMS_AND_CONDITIONS_PREVIOUS)
            if (Tools.needRequestPermission()) {
                val permission = PermissionUtil.getDeniedPermission(this@ActivityLogin)
                if (permission.isNotEmpty()) {
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
            i.putExtra(Constant.IS_FIRST_OPEN, true)
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
        if (!checkingPermissions()) {
            checkBox?.isChecked = false
        } else {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.TERMS_AND_CONDITIONS_SUCCESS)
            Log.d(LOG_TAG, "Permissions OK")
            checkBox?.isChecked = true
            signInButton!!.isEnabled = true
        }
    }

    companion object {
        private val TAG = "ActivityLogin"
        private val RC_SIGN_IN = 9001
    }
}