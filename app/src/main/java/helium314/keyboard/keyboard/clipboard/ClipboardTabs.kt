// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.keyboard.clipboard

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import helium314.keyboard.latin.ClipboardHistoryManager
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.database.ClipboardDao
import helium314.keyboard.latin.settings.Settings

/** Dua tab (Temporary / Permanent) yang ditaruh di strip toolbar clipboard. */
class ClipboardTabs {
    private var tabTemp: TextView? = null
    private var tabPerm: TextView? = null

    fun attach(strip: ViewGroup, onSelect: (Boolean) -> Unit) {
        val t = makeTab(strip.context) { onSelect(false) }
        val p = makeTab(strip.context) { onSelect(true) }
        tabTemp = t
        tabPerm = p
        strip.addView(t)
        strip.addView(p)
    }

    private fun makeTab(context: Context, onClick: () -> Unit): TextView {
        val pad = (14 * context.resources.displayMetrics.density).toInt()
        val v = TextView(context)
        v.gravity = Gravity.CENTER
        v.setPadding(pad, 0, pad, 0)
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        v.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        v.setOnClickListener { onClick() }
        return v
    }

    fun update(manager: ClipboardHistoryManager) {
        val color = Settings.getValues().mColors.get(ColorType.KEY_TEXT)
        val pinnedActive = manager.showPinned
        val temp = manager.countEntries(false)
        val perm = manager.countEntries(true)
        style(tabTemp, "Temporary ($temp/${ClipboardDao.MAX_TEMP})", !pinnedActive, color)
        style(tabPerm, "Permanent ($perm)", pinnedActive, color)
    }

    private fun style(v: TextView?, label: String, active: Boolean, color: Int) {
        if (v == null) return
        v.text = label
        v.setTextColor(color)
        v.alpha = if (active) 1f else 0.5f
        v.setTypeface(null, if (active) Typeface.BOLD else Typeface.NORMAL)
    }
}
