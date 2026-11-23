// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.keyboard.clipboard

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import helium314.keyboard.latin.ClipboardHistoryEntry
import helium314.keyboard.latin.ClipboardHistoryManager
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.settings.Settings

class ClipboardAdapter(
       val clipboardLayoutParams: ClipboardLayoutParams,
       val keyEventListener: OnKeyEventListener
) : RecyclerView.Adapter<ClipboardAdapter.ViewHolder>() {

    var clipboardHistoryManager: ClipboardHistoryManager? = null

    var pinnedIconResId = 0
    var itemBackgroundId = 0
    var itemTypeFace: Typeface? = null
    var itemTextColor = 0
    var itemTextSize = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.clipboard_entry_key, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(getItem(position))
    }

    private fun getItem(position: Int) = clipboardHistoryManager?.getHistoryEntry(position)

    override fun getItemCount() = clipboardHistoryManager?.getHistorySize() ?: 0

    inner class ViewHolder(
            view: View
    ) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {

        private val pinnedIconView: ImageView
        private val contentTextView: TextView
        private val contentImageView: ImageView
        private val actionsRow: LinearLayout
        private val keepView: TextView
        private val editView: TextView
        private val deleteView: TextView
        private var confirmDelete = false

        init {
            view.apply {
                setOnClickListener(this@ViewHolder)
                setOnTouchListener(this@ViewHolder)
                setOnLongClickListener(this@ViewHolder)
                setBackgroundResource(itemBackgroundId)
                isHapticFeedbackEnabled = false
            }
            Settings.getValues().mColors.setBackground(view, ColorType.KEY_BACKGROUND)
            pinnedIconView = view.findViewById<ImageView>(R.id.clipboard_entry_pinned_icon).apply {
                visibility = View.GONE
                setImageResource(pinnedIconResId)
            }
            contentTextView = view.findViewById<TextView>(R.id.clipboard_entry_text_content).apply {
                typeface = itemTypeFace
                setTextColor(itemTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize)
            }
            contentImageView = view.findViewById(R.id.clipboard_entry_image_content)
            clipboardLayoutParams.setItemProperties(view)
            val colors = Settings.getValues().mColors
            colors.setColor(pinnedIconView, ColorType.CLIPBOARD_PIN)

            actionsRow = view.findViewById(R.id.clipboard_entry_actions)
            keepView = view.findViewById(R.id.clipboard_action_keep)
            editView = view.findViewById(R.id.clipboard_action_edit)
            deleteView = view.findViewById(R.id.clipboard_action_delete)
            for (v in listOf(keepView, editView, deleteView)) {
                v.setBackgroundResource(itemBackgroundId)
                colors.setBackground(v, ColorType.KEY_BACKGROUND)
                v.typeface = itemTypeFace
                v.setTextColor(itemTextColor)
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize)
            }
            keepView.setOnClickListener {
                clipboardHistoryManager?.toggleClipPinned(itemView.tag as Long)
            }
            editView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ClipEditActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(ClipEditActivity.EXTRA_ID, itemView.tag as Long)
                context.startActivity(intent)
            }
            deleteView.setOnClickListener {
                if (!confirmDelete) {
                    confirmDelete = true
                    deleteView.text = "Yakin?"
                } else {
                    clipboardHistoryManager?.deleteEntryById(itemView.tag as Long)
                }
            }
        }

        fun setContent(historyEntry: ClipboardHistoryEntry?) {
            if (historyEntry == null) return
            itemView.tag = historyEntry.id
            if (historyEntry.filename != null) {
                historyEntry.setImageAndDescription(contentImageView, contentTextView)
            } else {
                contentTextView.text = historyEntry.text?.take(1000) // truncate displayed text for performance reasons
            }
            pinnedIconView.visibility = if (historyEntry.isPinned) View.VISIBLE else View.GONE
            contentImageView.visibility = if (historyEntry.filename != null) View.VISIBLE else View.GONE
            contentTextView.visibility = if (contentTextView.text.isNullOrEmpty()) View.GONE else View.VISIBLE

            actionsRow.visibility = View.GONE
            confirmDelete = false
            deleteView.text = "Hapus"
            editView.text = "Edit"
            keepView.text = if (historyEntry.isPinned) "Lepas" else "Keep"
            editView.visibility = if (historyEntry.filename != null) View.GONE else View.VISIBLE
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                keyEventListener.onKeyDown(view.tag as Long)
            }
            return false
        }

        override fun onClick(view: View) {
            keyEventListener.onKeyUp(view.tag as Long)
        }

        override fun onLongClick(view: View): Boolean {
            actionsRow.visibility = if (actionsRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            return true
        }
    }
}
