package ivandesimone.trustapp.utils

import android.util.Log

/**
 * Debug class to use a unique tag
 */
class Debug {

	companion object {
		private const val TAG = "DEBUGGAO"

		fun d(msg: String) {
			Log.d(TAG, msg)
		}

		fun e(msg: String) {
			Log.e(TAG, msg)
		}
	}

}