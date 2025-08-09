package ivandesimone.trustapp

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient

class MyApp : Application() {

	override fun onCreate() {
		super.onCreate()

		val projectId = "73348ea706de4887fd8b34a23ec46ff2"
		val serverUrl = "wss://relay.walletconnect.com?projectId=$projectId"
		val appMetaData = Core.Model.AppMetaData(
			name = "TrustApp",
			description = "Android application for trusted IoT data",
			url = "",
			icons = listOf(),
			redirect = null
		)

		// init CoreClient
		CoreClient.initialize(
			relayServerUrl = serverUrl,
			connectionType = ConnectionType.AUTOMATIC,
			application = applicationContext as Application,
			metaData = appMetaData
		) { error: Core.Model.Error ->
			// retry initialization ???
			Log.e("WalletConnect", "Init CoreClient error: $error")
		}

		// init SignClient
		val init = Sign.Params.Init(core = CoreClient)
		SignClient.initialize(init) { error ->
			Log.e("WalletConnect", "Init SignClient error: $error")
		}
	}

}