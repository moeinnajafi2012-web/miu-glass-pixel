package com.mui.glasslauncher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var appList: LinearLayout
    private lateinit var search: EditText
    private var apps: List<android.content.pm.ResolveInfo> = emptyList()

    private val white = Color.WHITE
    private val muted = Color.rgb(220, 228, 245)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(28, 38, 68)
        window.navigationBarColor = Color.rgb(20, 28, 52)
        window.decorView.systemUiVisibility = 0
        buildHome()
        loadApps()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun glass(radius: Float = 28f): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radius.toInt()).toFloat()
            setColor(Color.argb(42, 255, 255, 255))
            setStroke(dp(1), Color.argb(75, 255, 255, 255))
        }

    private fun text(value: String, size: Float, color: Int = white, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun buildHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(27, 43, 78), Color.rgb(66, 74, 125), Color.rgb(34, 54, 91))
            )
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(text("MUI", 18f, white, true), LinearLayout.LayoutParams(0, dp(40), 1f))
        top.addView(text("●  ●  ●", 12f, muted), LinearLayout.LayoutParams(-2, dp(40)))
        root.addView(top)

        val clockCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = glass(32f)
            setPadding(dp(18), dp(24), dp(18), dp(24))
        }
        val time = text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), 54f, white, true)
        clockCard.addView(time)
        clockCard.addView(text(SimpleDateFormat("EEEE  •  d MMMM", Locale.getDefault()).format(Date()), 14f, muted))
        val clockParams = LinearLayout.LayoutParams(-1, dp(160))
        clockParams.setMargins(0, dp(28), 0, dp(18))
        root.addView(clockCard, clockParams)

        val greeting = text("برنامه‌های شما", 19f, white, true)
        greeting.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        root.addView(greeting, LinearLayout.LayoutParams(-1, dp(42)))

        search = EditText(this).apply {
            hint = "جست‌وجوی برنامه‌ها..."
            setHintTextColor(Color.argb(180, 255, 255, 255))
            setTextColor(white)
            textSize = 15f
            singleLine = true
            background = glass(24f)
            setPadding(dp(18), 0, dp(18), 0)
        }
        val searchParams = LinearLayout.LayoutParams(-1, dp(52))
        searchParams.setMargins(0, dp(6), 0, dp(14))
        root.addView(search, searchParams)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        appList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = glass(26f)
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        scroll.addView(appList)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottom = text("Pixel-inspired • Android 16", 12f, muted)
        bottom.gravity = Gravity.CENTER
        val bp = LinearLayout.LayoutParams(-1, dp(38))
        bp.topMargin = dp(10)
        root.addView(bottom, bp)

        setContentView(root)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
        renderApps("")
    }

    private fun renderApps(filter: String) {
        appList.removeAllViews()
        val filtered = apps.filter {
            it.loadLabel(packageManager).toString().contains(filter, ignoreCase = true)
        }
        if (filtered.isEmpty()) {
            val empty = text("برنامه‌ای پیدا نشد", 15f, muted)
            empty.gravity = Gravity.CENTER
            appList.addView(empty, LinearLayout.LayoutParams(-1, dp(70)))
            return
        }

        filtered.forEach { info ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = rippleBackground()
                isClickable = true
                isFocusable = true
            }
            val icon = ImageView(this).apply {
                setImageDrawable(info.loadIcon(packageManager))
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            row.addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)))
            val label = text(info.loadLabel(packageManager).toString(), 15f, white, true)
            label.setPadding(dp(14), 0, 0, 0)
            row.addView(label, LinearLayout.LayoutParams(0, dp(48), 1f))
            row.setOnClickListener {
                val launch = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launch != null) startActivity(launch)
            }
            appList.addView(row, LinearLayout.LayoutParams(-1, dp(58)))
        }
    }

    private fun rippleBackground(): android.graphics.drawable.Drawable {
        val mask = GradientDrawable().apply {
            cornerRadius = dp(18).toFloat()
            setColor(Color.WHITE)
        }
        return android.graphics.drawable.RippleDrawable(
            android.content.res.ColorStateList.valueOf(Color.argb(45, 255, 255, 255)),
            null,
            mask
        )
    }
}
