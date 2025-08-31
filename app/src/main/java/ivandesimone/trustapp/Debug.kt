package ivandesimone.trustapp

import android.util.Log

class Debug {

	companion object {
		fun d(msg: String) {
			Log.d("DEBUGGAO", msg)
		}

		fun e(msg: String) {
			Log.e("DEBUGGAO", msg)
		}
	}
}