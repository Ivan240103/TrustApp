package ivandesimone.trustapp.ui.details

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Custom map layout to intercept touch going to the map
 */
class TouchMapFrameLayout @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		when (ev.actionMasked) {
			MotionEvent.ACTION_DOWN,
			MotionEvent.ACTION_MOVE,
			MotionEvent.ACTION_POINTER_DOWN -> parent?.requestDisallowInterceptTouchEvent(true)
			MotionEvent.ACTION_UP,
			MotionEvent.ACTION_CANCEL -> parent?.requestDisallowInterceptTouchEvent(false)
		}
		return false
	}

	override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
		// extra safety so parent never intercepts mid-gesture (one or two fingers)
		if (ev.actionMasked == MotionEvent.ACTION_DOWN ||
			ev.actionMasked == MotionEvent.ACTION_MOVE ||
			ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
			parent?.requestDisallowInterceptTouchEvent(true)
		} else if (ev.actionMasked == MotionEvent.ACTION_UP ||
			ev.actionMasked == MotionEvent.ACTION_CANCEL) {
			parent?.requestDisallowInterceptTouchEvent(false)
		}
		return super.dispatchTouchEvent(ev)
	}
}
