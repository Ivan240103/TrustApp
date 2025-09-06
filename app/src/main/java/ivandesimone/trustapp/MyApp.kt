package ivandesimone.trustapp

import android.app.Application
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import ivandesimone.trustapp.utils.Debug

/**
 * Custom Application class to perform WalletConnect initialization
 */
class MyApp : Application() {

	override fun onCreate() {
		super.onCreate()

		// ID from https://dashboard.reown.com/   (cloud.walletconnect.com)
		val projectId = "73348ea706de4887fd8b34a23ec46ff2"
		val serverUrl = "wss://relay.walletconnect.com?projectId=$projectId"
		val appMetaData = Core.Model.AppMetaData(
			name = "TrustApp",
			description = "Android application for trusted IoT data",
			url = "https://ivandesimone.altervista.org/",
			icons = listOf(), // TODO: add icon
			redirect = null
		)

		// init CoreClient
		CoreClient.initialize(
			relayServerUrl = serverUrl,
			connectionType = ConnectionType.AUTOMATIC,
			application = this,
			metaData = appMetaData
		) { error: Core.Model.Error ->
			Debug.e("CoreClient initialize error: $error")
		}

		// init SignClient
		SignClient.initialize(
			init = Sign.Params.Init(core = CoreClient),
			onError = { error ->
				Debug.e("SignClient initialize error: $error")
			}
		)
	}

}