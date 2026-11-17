package com.hisn.receiver

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class MainActivity : Activity() {
    private val navy = Color.rgb(16, 38, 69)
    private val blue = Color.rgb(34, 105, 165)
    private val muted = Color.rgb(95, 111, 128)
    private val prefs by lazy { getSharedPreferences("hisn_local_security", MODE_PRIVATE) }
    private var unlocked = false
    private var lockWhenResumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        window.navigationBarColor = navy
        showPinGate()
    }

    override fun onStop() {
        super.onStop()
        if (unlocked && !isFinishing) lockWhenResumed = true
    }

    override fun onRestart() {
        super.onRestart()
        if (lockWhenResumed) {
            lockWhenResumed = false
            unlocked = false
            showPinGate()
        }
    }

    private fun showPinGate() {
        val hasPin = prefs.contains("pin_hash") && prefs.contains("pin_salt")
        val page = basePage()
        page.gravity = Gravity.CENTER_HORIZONTAL
        page.addView(space(44))
        page.addView(label("حِصن", 34, navy, true))
        page.addView(label(if (hasPin) "التطبيق محمي" else "إنشاء رمز حماية", 20, navy, true))
        page.addView(label(
            if (hasPin) "أدخل رمز PIN لفتح لوحة حِصن."
            else "أنشئ رمزاً من 6 أرقام. سيُطلب عند فتح التطبيق والعودة إليه.",
            14, muted, false
        ))
        page.addView(space(18))

        val pin = EditText(this).apply {
            hint = "رمز من 6 أرقام"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            imeOptions = EditorInfo.IME_ACTION_DONE
            textSize = 20f
            gravity = Gravity.CENTER
            letterSpacing = 0.35f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE)
        }
        page.addView(pin, LinearLayout.LayoutParams(-1, dp(56)))

        val confirm = if (hasPin) null else EditText(this).apply {
            hint = "تأكيد الرمز"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            imeOptions = EditorInfo.IME_ACTION_DONE
            textSize = 20f
            gravity = Gravity.CENTER
            letterSpacing = 0.35f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE)
        }
        if (confirm != null) {
            page.addView(space(10))
            page.addView(confirm, LinearLayout.LayoutParams(-1, dp(56)))
        }
        page.addView(space(14))
        val message = label("", 13, Color.rgb(184, 72, 55), false)
        page.addView(message)

        page.addView(button(if (hasPin) "فتح حِصن" else "حفظ رمز الحماية") {
            val entered = pin.text.toString()
            if (!entered.matches(Regex("\\d{6}"))) {
                message.text = "أدخل 6 أرقام بالضبط."
                return@button
            }
            if (!hasPin) {
                if (confirm?.text?.toString() != entered) {
                    message.text = "الرمزان غير متطابقين."
                    return@button
                }
                savePin(entered)
                unlocked = true
                showDashboard()
            } else {
                if (verifyPin(entered)) {
                    unlocked = true
                    showDashboard()
                } else {
                    message.text = "الرمز غير صحيح. حاول مرة أخرى."
                    pin.text.clear()
                }
            }
        })
        page.addView(space(18))
        page.addView(label("رمز الحماية يُحفظ محلياً على هذا الهاتف، ولا يُرفع إلى GitHub أو الخادم.", 12, muted, false))
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun savePin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivePinHash(pin, salt)
        prefs.edit()
            .putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
    }

    private fun verifyPin(pin: String): Boolean {
        val saltText = prefs.getString("pin_salt", null) ?: return false
        val hashText = prefs.getString("pin_hash", null) ?: return false
        val salt = Base64.decode(saltText, Base64.NO_WRAP)
        val expected = Base64.decode(hashText, Base64.NO_WRAP)
        val actual = derivePinHash(pin, salt)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derivePinHash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 120_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun showDashboard() {
        val page = basePage()
        page.addView(label("حِصن", 30, navy, true))
        page.addView(label("لوحة مشاهدة شاشة iPhone", 15, muted, false))
        page.addView(space(20))

        val status = card()
        status.addView(label("حالة الجهاز", 16, navy, true))
        status.addView(label("● غير متصل", 18, Color.rgb(184, 72, 55), true))
        status.addView(label("ستظهر صورة الشاشة هنا بعد تنفيذ الربط وتشغيل البث بموافقة صاحب iPhone.", 13, muted, false))
        page.addView(status)

        page.addView(space(16))
        val pairing = card()
        pairing.addView(label("اقتران جهاز iPhone", 17, navy, true))
        pairing.addView(label("أدخل رمز الاقتران الذي يظهر داخل تطبيق حِصن على iPhone.", 13, muted, false))
        val code = EditText(this).apply {
            hint = "رمز الاقتران"
            textSize = 16f
            singleLine = true
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(Color.WHITE)
        }
        pairing.addView(code, LinearLayout.LayoutParams(-1, dp(52)))
        pairing.addView(button("ربط الجهاز") {
            Toast.makeText(this, "الواجهة أولية: خدمة الاقتران لم تُنفذ بعد.", Toast.LENGTH_LONG).show()
        })
        page.addView(pairing)

        page.addView(space(16))
        val session = card()
        session.addView(label("جلسة المشاهدة", 17, navy, true))
        session.addView(button("طلب بدء المشاهدة") {
            Toast.makeText(this, "سيعمل بعد إضافة WebRTC وربط جلسة iPhone.", Toast.LENGTH_LONG).show()
        })
        session.addView(button("إنهاء الجلسة") {
            Toast.makeText(this, "لا توجد جلسة فعلية نشطة حالياً.", Toast.LENGTH_SHORT).show()
        })
        page.addView(session)

        page.addView(space(18))
        page.addView(label("حماية التطبيق", 15, navy, true))
        page.addView(label("يُطلب رمز PIN عند فتح حِصن وعند العودة إليه بعد الانتقال لتطبيق آخر.", 13, muted, false))
        page.addView(button("قفل التطبيق الآن") {
            unlocked = false
            showPinGate()
        })
        page.addView(space(12))
        page.addView(label("يظل مؤشر مشاركة الشاشة وموافقة بدء البث خاضعين لنظام iOS. لا يدعم حِصن بثاً خفياً أو تجاوزاً لتنبيهات النظام.", 13, muted, false))
        page.addView(space(10))
        page.addView(label("الإصدار 0.4 — حماية PIN مضافة؛ البث الفعلي غير منفذ بعد.", 12, muted, false))
        setContentView(ScrollView(this).apply { addView(page) })
    }

    private fun basePage() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(22), dp(20), dp(24))
        setBackgroundColor(Color.rgb(247, 249, 252))
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = rounded(Color.WHITE)
        elevation = dp(2).toFloat()
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean) =
        TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.RIGHT
            textDirection = View.TEXT_DIRECTION_RTL
            setPadding(0, dp(4), 0, dp(7))
        }

    private fun button(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        textSize = 15f
        setTextColor(Color.WHITE)
        background = rounded(blue)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(9) }
    }

    private fun rounded(fill: Int) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(14).toFloat()
        setStroke(dp(1), Color.rgb(225, 232, 240))
    }

    private fun space(height: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
