package com.hisn.receiver

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val navy = Color.rgb(16, 38, 69)
    private val blue = Color.rgb(34, 105, 165)
    private val muted = Color.rgb(95, 111, 128)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        window.navigationBarColor = navy

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(24))
            setBackgroundColor(Color.rgb(247, 249, 252))
        }
        val scroll = ScrollView(this).apply { addView(page) }

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
            Toast.makeText(this, "واجهة أولية: لم تُنفذ خدمة الاقتران بعد.", Toast.LENGTH_LONG).show()
        })
        page.addView(pairing)

        page.addView(space(16))
        val session = card()
        session.addView(label("جلسة المشاهدة", 17, navy, true))
        session.addView(button("طلب بدء المشاهدة") {
            Toast.makeText(this, "سيعمل الزر بعد إضافة WebRTC وربط جلسة iPhone.", Toast.LENGTH_LONG).show()
        })
        session.addView(button("إنهاء الجلسة") {
            Toast.makeText(this, "لا توجد جلسة فعلية نشطة حالياً.", Toast.LENGTH_SHORT).show()
        })
        page.addView(session)

        page.addView(space(18))
        page.addView(label("تنبيه الخصوصية", 15, navy, true))
        page.addView(label("يظل مؤشر مشاركة الشاشة وموافقة بدء البث خاضعين لنظام iOS. لا يدعم حِصن بثاً خفياً أو تجاوزاً لتنبيهات النظام.", 13, muted, false))
        page.addView(space(10))
        page.addView(label("الإصدار 0.3 — واجهة أولية غير متصلة ببث فعلي.", 12, muted, false))
        setContentView(scroll)
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
