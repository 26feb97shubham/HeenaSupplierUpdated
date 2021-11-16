package com.dev.heenasupplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import com.dev.heenasupplier.utils.SharedPreferenceUtility.Companion.FIRSTTIME
import com.dev.heenasupplier.utils.Utility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class SplashActivity : AppCompatActivity() {
    private var selectLang:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseApp.initializeApp(this)
        getFCMToken()
        deviceId()
        setUpViews()
    }
    private fun setUpViews() {
       /* Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if(SharedPreferenceUtility.getInstance()
                    .get(SharedPreferenceUtility.IsWelcomeShow, false)) {
                selectLang = "ar"
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
                if (!TextUtils.isEmpty(SharedPreferenceUtility.getInstance()
                        .get(SharedPreferenceUtility.SelectedLang, ""))) {
//                    Utility.changeLanguage(this, SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.SelectedLang, ""])
                    Utility.setDefaultLanguage(this,
                        SharedPreferenceUtility.getInstance()
                            .get(SharedPreferenceUtility.SelectedLang, "")
                    )
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                     if (SharedPreferenceUtility.getInstance()
                             .get(SharedPreferenceUtility.IsLogin, false)) {
                         startActivity(Intent(this, HomeActivity::class.java))
                         finishAffinity()
                     } else {
                         startActivity(Intent(this, ChooseLoginSignUpActivity::class.java))
                         finishAffinity()
                     }
                } else {
                    startActivity(Intent(this, ChooseLangActivity::class.java))
                    finishAffinity()
                }

            }
            else{
                selectLang = "ar"
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.SelectedLang, selectLang)
                Utility.setDefaultLanguage(this,
                    SharedPreferenceUtility.getInstance()
                        .get(SharedPreferenceUtility.SelectedLang, "")
                )
                startActivity(Intent(this, ChooseLangActivity::class.java))
                finishAffinity()
            }
        },2000)*/

        if (Utility.getLanguage().isNullOrEmpty()){
            Utility.changeLanguage(this,"ar")
        }else{
            Utility.changeLanguage(this,Utility.getLanguage())
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(SharedPreferenceUtility.getInstance().get(FIRSTTIME,false)
                    && SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    /*  val intent = Intent(this, IntroSliderActivity::class.java)
                      startActivity(intent)
                      finish()*/
                    val intent = Intent(this, ChooseLangActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && !(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, ChooseLoginSignUpActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISSELECTLANGUAGE,false)
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ISINTRODUCTION,false))
                    && (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.IsLogin,false)))
                {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val intent = Intent(this, ChooseLangActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            },
            2000,
        )
    }
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("getInstanceId", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                val fcmToken = task.result
                Log.e("getInstanceId", fcmToken)
                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.FCMTOKEN,fcmToken.toString())

            })

    }
    private fun deviceId(){
        val deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        Log.e("deviceId", deviceId)
        SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.DeviceId,deviceId.toString())
    }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

}