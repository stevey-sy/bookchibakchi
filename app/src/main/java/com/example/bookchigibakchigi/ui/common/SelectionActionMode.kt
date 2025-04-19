package com.example.bookchigibakchigi.ui.common

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.graphics.Rect
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookchigibakchigi.R

class SelectionActionMode(
    private val activity: AppCompatActivity,
    private val onDelete: () -> Unit,
    private val onSelectionModeChanged: (Boolean) -> Unit
) {
    private var actionMode: ActionMode? = null
    private var selectedCount: Int = 0

    private val actionModeCallback = object : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_book_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    onDelete()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            onSelectionModeChanged(false)
            actionMode = null
        }

        override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: Rect?) {
            view?.let {
                outRect?.set(
                    it.left,
                    it.top,
                    it.right,
                    it.top + it.resources.getDimensionPixelSize(R.dimen.action_mode_height)
                )
            }
        }
    }

    fun start(view: View) {
        if (actionMode == null) {
            actionMode = activity.startActionMode(actionModeCallback, ActionMode.TYPE_FLOATING)
            onSelectionModeChanged(true)
        }
    }

    fun updateSelectedCount(count: Int) {
        selectedCount = count
        actionMode?.title = "$count selected"
    }

    fun finish() {
        actionMode?.finish()
    }

    fun isActive(): Boolean = actionMode != null
} 