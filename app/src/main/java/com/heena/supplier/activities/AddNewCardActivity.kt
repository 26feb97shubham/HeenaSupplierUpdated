package com.heena.supplier.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.heena.supplier.R
import com.heena.supplier.extras.AsteriskPasswordTransformationMethod
import com.heena.supplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.fragment_add_new_card.*

class AddNewCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_card)
        tv_save_card.setOnClickListener {
            startActivity(Intent(this, PaymentFragmentActivity::class.java))
        }
        addTextWatcher()
    }

    companion object {
        private var instance: SharedPreferenceUtility? = null

        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }


    private fun addTextWatcher() {
        et_card_title.doOnTextChanged { text, _, _, _ ->
            tv_account_holdee_name_1.text = text
        }

        et_card_number.doOnTextChanged { text, _, _, _ ->
            tv_card_number_1.text = text
        }

        et_expiry.doOnTextChanged { text, _, _, _ ->
            tv_expiry_date_1.text = text
        }

        et_expiry.doAfterTextChanged {
            if (it!!.isNotEmpty() && it.length == 3) {
                val c: Char = it[it.length - 1]
                if ('/' == c) {
                    it.delete(it.length - 1, it.length)
                }
            }
            if (it.isNotEmpty() && it.length == 3) {
                val c: Char = it[it.length - 1]
                if (Character.isDigit(c) && TextUtils.split(it.toString(), "/").size <= 2) {
                    it.insert(it.length - 1, "/")
                }
            }
        }

        et_cvv.transformationMethod = AsteriskPasswordTransformationMethod()
    }
}