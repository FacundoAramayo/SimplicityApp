package com.simplicityapp.modules.start.activity

import android.Manifest
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.simplicityapp.BuildConfig
import com.simplicityapp.R
import com.simplicityapp.base.BaseActivity
import com.simplicityapp.base.config.Constant
import com.simplicityapp.base.config.Constant.LOG_TAG
import com.simplicityapp.base.config.ThisApplication
import com.simplicityapp.base.config.analytics.AnalyticsConstants
import com.simplicityapp.base.rest.RetrofitService
import com.simplicityapp.base.utils.PermissionUtil
import com.simplicityapp.base.utils.Tools
import com.simplicityapp.databinding.ActivityLoginBinding
import com.simplicityapp.modules.main.activity.ActivityMain
import com.simplicityapp.modules.settings.model.CallbackDevice
import com.simplicityapp.modules.settings.services.DeviceAPI
import retrofit2.Call
import retrofit2.Response

private const val FCM_MAX_COUNT = 10

/**
 * A login screen that offers login via Google Sign-In after accept Terms & Conditions.
 */
class ActivityLogin : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var gso: GoogleSignInOptions? = null
    private var callback: Call<CallbackDevice>? = null
    private var fcmCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        initActivity(binding)
        configureGoogleSignIn()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso!!)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun initUI() {
        binding.signInButton.isEnabled = false
        startBackgroundAnimation()
    }

    private fun startBackgroundAnimation() {
        val animationDrawable: AnimationDrawable = binding.mainLayout.background as AnimationDrawable
        animationDrawable.setExitFadeDuration(3500)
        animationDrawable.start()
    }

    override fun initListeners() {
        binding.signInButton.setOnClickListener(this)
        binding.checkBox.setOnClickListener { checkBoxListener() }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
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
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(LOG_TAG, "signInWithCredential: success")
                    val user = mAuth.currentUser
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
        if (binding.checkBox.isChecked) {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.TERMS_AND_CONDITIONS_PREVIOUS)
            if (Tools.needRequestPermission()) {
                val permission = PermissionUtil.getDeniedPermission(this@ActivityLogin)
                if (permission.isNotEmpty()) {
                    requestPermissions(permission, 200)
                } else {
                    binding.signInButton.isEnabled = true
                }
            }
        } else {
            binding.signInButton.isEnabled = false
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mAuth.signOut()

        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this
        ) { updateUI(null) }
    }

    private fun revokeAccess() {
        mAuth.signOut()

        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this
        ) { updateUI(null) }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            registerUser(user)
            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.sign_in_button) {
            if (binding.checkBox.isChecked) {
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
            binding.checkBox.isChecked = false
        } else {
            AnalyticsConstants.logAnalyticsEvent(AnalyticsConstants.TERMS_AND_CONDITIONS_SUCCESS)
            Log.d(LOG_TAG, "Permissions OK")
            binding.checkBox.isChecked = true
            binding.signInButton.isEnabled = true
        }
    }

    private fun registerUser(user: FirebaseUser) {
        // initialize firebase
        ThisApplication.instance?.firebaseApp = FirebaseApp.initializeApp(this)

        // obtain regId & registering device to server
        obtainFirebaseToken(user, ThisApplication.instance?.firebaseApp)
    }

    private fun obtainFirebaseToken(user: FirebaseUser, firebaseApp: FirebaseApp?) {
        if (!Tools.checkConnection(this)) return
        fcmCount++

        val resultTask = FirebaseInstanceId.getInstance().instanceId
        resultTask.addOnSuccessListener { instanceIdResult ->
            val regId = instanceIdResult.token
            if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(user, regId)
        }

        resultTask.addOnFailureListener(OnFailureListener { e ->
            Log.e(LOG_TAG, "Failed obtain fcmID : " + e.message)
            if (fcmCount > FCM_MAX_COUNT) return@OnFailureListener
            obtainFirebaseToken(user, firebaseApp)
        })
    }

    /**
     * --------------------------------------------------------------------------------------------
     * For Firebase Cloud Messaging
     */
    private fun sendRegistrationToServer(user: FirebaseUser, token: String) {
        if (Tools.checkConnection(this) && !TextUtils.isEmpty(token) && sharedPref!!.isOpenAppCounterReach) {
            val api = RetrofitService.createService(DeviceAPI::class.java)
            val deviceInfo = Tools.getDeviceInfo(this)
            deviceInfo.regid = token
            deviceInfo.email = user.email
            deviceInfo.name = user.displayName

            callback = api.registerDevice(deviceInfo)
            callback!!.enqueue(object : retrofit2.Callback<CallbackDevice> {
                override fun onResponse(
                    call: Call<CallbackDevice>,
                    response: Response<CallbackDevice>
                ) {
                    val resp = response.body()
                    if (resp != null) {
                        if (resp.status == "success") {
                            sharedPref!!.setOpenAppCounter(0)
                            openMainActivity()
                        }
                    }
                }

                override fun onFailure(call: Call<CallbackDevice>, t: Throwable) {
                    t.printStackTrace()
                    openMainActivity()
                }
            })
        }
    }

    private fun openMainActivity() {
        val i = Intent(this@ActivityLogin, ActivityLoginSuccessful::class.java)
        i.putExtra(Constant.IS_FIRST_OPEN, true)
        startActivity(i)
        finish()
    }

    companion object {
        private val RC_SIGN_IN = 9001
    }
}