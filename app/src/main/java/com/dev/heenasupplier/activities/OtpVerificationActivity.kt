package com.dev.heenasupplier.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.Toast
import com.dev.heenasupplier.R
import com.dev.heenasupplier.models.ForgotPasswordVerifyResponse
import com.dev.heenasupplier.models.RegisterVerifyResendResponse
import com.dev.heenasupplier.models.RegistrationVerifyResponse
import com.dev.heenasupplier.rest.APIClient
import com.dev.heenasupplier.rest.APIInterface
import com.dev.heenasupplier.utils.ConstClass
import com.dev.heenasupplier.utils.LogUtils
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.activity_otp_verification2.*
import kotlinx.android.synthetic.main.activity_sign_up2.*
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.Callback
import org.json.JSONException
import retrofit2.Response
import java.io.IOException


class OtpVerificationActivity : AppCompatActivity() {
    lateinit var ref: String
    lateinit var pin: String
    lateinit var emailaddress : String
    var doubleClick:Boolean=false
    lateinit var apiInterface: APIInterface
    lateinit var builder : FormBody.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification2)
        setUpViews()
    }

    private fun setUpViews() {
        if(intent.extras!=null){
            emailaddress = intent.getStringExtra(ConstClass.EMAILADDRESS).toString()
            ref = intent.getStringExtra("ref").toString()
        }

        pin = ""

        btnVerify.isEnabled=false
        btnVerify.alpha=0.5f

        firstPinView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) =
                if(charSeq!!.length==4){
                    btnVerify.isEnabled=true
                    btnVerify.alpha=1f
                    SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this@OtpVerificationActivity, firstPinView)
                }
                else{
                    btnVerify.isEnabled=false
                    btnVerify.alpha=0.5f
                }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        pin = firstPinView.text.toString().trim()


        btnVerify.setOnClickListener {
            btnVerify.startAnimation(AlphaAnimation(1f, 0.5f))
            validateAndVerification()

        }

        resend.setOnClickListener {
            resend.startAnimation(AlphaAnimation(1f, 0.5f))
            firstPinView.setText("")
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this@OtpVerificationActivity, it)
            resendOtp()
        }
    }

    private fun validateAndVerification() {
        pin = firstPinView.text.toString().trim()
        if (TextUtils.isEmpty(pin)) {
            firstPinView.error=getString(R.string.please_enter_your_otp)
                        LogUtils.shortToast(this@OtpVerificationActivity, getString(R.string.please_enter_your_otp))

        }
        else if ((pin.length < 4)) {
            firstPinView.error=getString(R.string.otp_length_valid)
                        LogUtils.shortToast(this@OtpVerificationActivity, getString(R.string.otp_length_valid))
        }

        else {
            if(ref=="1"){
//                SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.UserId, user_id.toInt()
                verifyAccount()

            }
            else{
               /* startActivity(
                    Intent(this@OtpVerificationActivity, ResetPasswordActivity::class.java)
                        .putExtra("user_id", user_id))
                val bundle=Bundle()
                bundle.putString("user_id", user_id)*/
                forgotPassVerify()

            }
        }

    }

    private fun forgotPassVerify() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otpVerificationprogressBar.visibility= View.VISIBLE
        apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        builder = APIClient.createBuilder(arrayOf("email", "otp", "lang"),
            arrayOf(emailaddress, pin.trim({ it <= ' ' }), SharedPreferenceUtility.getInstance()
                .get(SharedPreferenceUtility.SelectedLang, "")
                .toString()))


        val call = apiInterface.forgotpassverifyotp(builder.build())
        call!!.enqueue(object : Callback<ForgotPasswordVerifyResponse?>{
            override fun onResponse(
                call: Call<ForgotPasswordVerifyResponse?>,
                response: Response<ForgotPasswordVerifyResponse?>
            ) {
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                if (response.isSuccessful){
                    if (response.body()!!.status==1){
                        startActivity(
                            Intent(this@OtpVerificationActivity, ResetPasswordActivity::class.java).putExtra(ConstClass.EMAILADDRESS, emailaddress).putExtra("otp",pin))
                    }else{
                        LogUtils.longToast(this@OtpVerificationActivity, response.body()!!.message)
                    }
                }else{
                    LogUtils.longToast(this@OtpVerificationActivity, getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<ForgotPasswordVerifyResponse?>, t: Throwable) {
                LogUtils.e("msg", t.message)
                LogUtils.shortToast(this@OtpVerificationActivity,t.localizedMessage)
                otpVerificationprogressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }

    private fun verifyAccount() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otpVerificationprogressBar.visibility= View.VISIBLE
        apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        builder = APIClient.createBuilder(arrayOf("email", "otp", "lang"),
                arrayOf(emailaddress, pin.trim({ it <= ' ' }), SharedPreferenceUtility.getInstance()
                    .get(SharedPreferenceUtility.SelectedLang, "")
                    .toString()))
         val call = apiInterface.verifyotp(builder.build())
         call!!.enqueue(object : Callback<RegistrationVerifyResponse?> {
             override fun onResponse(call: Call<RegistrationVerifyResponse?>, response: Response<RegistrationVerifyResponse?>) {
                 otpVerificationprogressBar.visibility= View.GONE
                 window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                 try {
                     if (response.isSuccessful){
                         if (response.body()!!.status==1){
                             SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsVerified, true)
                             SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsLogin, true)
                             startActivity(Intent(this@OtpVerificationActivity, MembershipRegistrationActivity::class.java).putExtra(ConstClass.EMAILADDRESS, emailaddress))

                         }else{
                             LogUtils.longToast(this@OtpVerificationActivity, response.body()!!.message)
                         }
                     }else{
                         LogUtils.longToast(this@OtpVerificationActivity, getString(R.string.response_isnt_successful))
                     }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<RegistrationVerifyResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(this@OtpVerificationActivity, getString(R.string.check_internet))
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

        /*startActivity(
                Intent(this@OtpVerificationActivity, ResetPasswordActivity::class.java)
                        .putExtra("user_id", user_id))
        val bundle=Bundle()
        bundle.putString("user_id", user_id)*/

//
//        startActivity(
//                Intent(this@OtpVerificationActivity, ResetPasswordActivity::class.java))

    }
    private fun resendOtp() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        otpVerificationprogressBar.visibility= View.VISIBLE


        apiInterface = APIClient.getClient()!!.create(APIInterface::class.java)
        builder = APIClient.createBuilder(arrayOf("email"),
                arrayOf(emailaddress))
        val call = apiInterface.registerverivyresend(builder.build())

        call!!.enqueue(object : Callback<RegisterVerifyResendResponse?>{
            override fun onResponse(call: Call<RegisterVerifyResendResponse?>, response: Response<RegisterVerifyResendResponse?>) {
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                try {
                    if (response.isSuccessful){
                        if (response.body()!!.status==1){
                            SharedPreferenceUtility.getInstance().save(SharedPreferenceUtility.IsResend, true)
                            LogUtils.longToast(this@OtpVerificationActivity, response.body()!!.message)
                        }else{
                            LogUtils.longToast(this@OtpVerificationActivity, response.body()!!.message)
                        }
                    }else{
                        LogUtils.longToast(this@OtpVerificationActivity, getString(R.string.response_isnt_successful))
                    }
                }catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<RegisterVerifyResendResponse?>, throwable: Throwable) {
                LogUtils.e("msg", throwable.message)
                LogUtils.shortToast(this@OtpVerificationActivity, getString(R.string.check_internet))
                otpVerificationprogressBar.visibility= View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

        })
    }


    override fun onBackPressed() {
        exitApp()
    }
    private fun exitApp() {
        val toast = Toast.makeText(
            this,
            getString(R.string.please_click_back_again_to_exist),
            Toast.LENGTH_SHORT
        )


        if(doubleClick){
            finishAffinity()
            doubleClick=false
        }
        else{

            doubleClick=true
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                toast.show()
                doubleClick=false
            }, 500)
        }
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