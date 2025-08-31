package ivandesimone.trustapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ivandesimone.trustapp.R

// TODO: max seconds to wait for data
// TODO: wallet connection + button connect to wallet
// TODO: name + profile pic selected with intent?
// TODO: other configurations?
class ProfileFragment : Fragment() {

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_profile, container, false)
	}

}