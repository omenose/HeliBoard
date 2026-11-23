// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.keyboard.clipboard

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import helium314.keyboard.latin.database.ClipboardDao

/** Layar kecil untuk mengedit teks satu entri clipboard. */
class ClipEditActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        val dao = ClipboardDao.getInstance(this)
        val original = dao?.getAll()?.firstOrNull { it.id == id }?.text
        if (dao == null || original == null) {
            finish()
            return
        }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.parseColor("#121212"))
        root.setPadding(dp(20), dp(56), dp(20), dp(24))

        val title = TextView(this)
        title.text = "Edit teks"
        title.setTextColor(Color.WHITE)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setPadding(0, 0, 0, dp(12))
        root.addView(title)

        val input = EditText(this)
        input.setText(original)
        input.setTextColor(Color.WHITE)
        input.textSize = 15f
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        input.minLines = 4
        input.gravity = Gravity.TOP or Gravity.START
        input.background = rounded("#2C2C2C")
        input.setPadding(dp(14), dp(12), dp(14), dp(12))
        val inputLp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        inputLp.bottomMargin = dp(14)
        root.addView(input, inputLp)

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val save = button("Simpan", "#2962FF")
        val cancel = button("Batal", "#424242")
        val lp1 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp1.rightMargin = dp(4)
        val lp2 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp2.leftMargin = dp(4)
        row.addView(save, lp1)
        row.addView(cancel, lp2)
        root.addView(row)
        setContentView(root)

        save.setOnClickListener {
            val newText = input.text.toString()
            if (newText == original) {
                finish()
            } else if (dao.replaceText(id, newText)) {
                finish()
            } else {
                Toast.makeText(this, "Teks kosong atau sudah ada", Toast.LENGTH_SHORT).show()
            }
        }
        cancel.setOnClickListener { finish() }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()

    private fun rounded(color: String): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(Color.parseColor(color))
        g.cornerRadius = dp(10).toFloat()
        return g
    }

    private fun button(label: String, color: String): TextView {
        val t = TextView(this)
        t.text = label
        t.setTextColor(Color.WHITE)
        t.textSize = 15f
        t.gravity = Gravity.CENTER
        t.setPadding(dp(12), dp(14), dp(12), dp(14))
        t.background = rounded(color)
        return t
    }

    companion object {
        const val EXTRA_ID = "clip_id"
    }
}
