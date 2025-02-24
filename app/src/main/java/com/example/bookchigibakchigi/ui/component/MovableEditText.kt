package com.example.bookchigibakchigi.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

@SuppressLint("AppCompatCustomView")
class MovableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : EditText(context, attrs, defStyleAttr) {

    var savedBackgroundColor: Int? = null //

    var isMovable: Boolean = false // 외부에서 제어 가능하도록 설정
    private var dX = 0f
    private var dY = 0f

    init {
        // EditText가 포커스를 받을 수 있도록 설정
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isMovable) {
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    dX = this.x - event.rawX
//                    dY = this.y - event.rawY
//                    parent.requestDisallowInterceptTouchEvent(true) // 부모가 터치 이벤트를 가로채지 않도록 설정
//                    return true
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    this.animate()
//                        .x(event.rawX + dX)
//                        .y(event.rawY + dY)
//                        .setDuration(0)
//                        .start()
//                    return true
//                }
//                MotionEvent.ACTION_UP -> {
//                    parent.requestDisallowInterceptTouchEvent(false) // 부모 이벤트 가로채기 해제
//                    return true
//                }
//            }
//            return true // 이동 모드일 때 이벤트 소비
            return false
        }

        // 이동 모드가 아닐 때 기본 EditText 동작 수행
        val result = super.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP) {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }

        return result
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

//    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
//        super.onFocusChanged(focused, direction, previouslyFocusedRect)
//        if (!focused && savedBackgroundColor != null) {
//            // 포커스를 잃었을 때, 기존 배경색 복원
//            this.setBackgroundColor(savedBackgroundColor!!)
//        }
//    }

}