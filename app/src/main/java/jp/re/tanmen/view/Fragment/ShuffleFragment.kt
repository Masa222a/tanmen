package jp.re.tanmen.view.Fragment

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.squareup.picasso.Picasso
import jp.re.tanmen.R
import jp.re.tanmen.databinding.FragmentShuffleBinding
import jp.re.tanmen.model.API.ShopService
import jp.re.tanmen.viewModel.ShuffleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber.d

class ShuffleFragment : Fragment() {
    private lateinit var binding: FragmentShuffleBinding
    private val viewModel: ShuffleViewModel by viewModels()
    var progressDialog: ProgressDialog? = null
    private val shopService = ShopService.instance
    private var isFirst = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShuffleBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            shuffleViewModel = viewModel
        }
        this.progressDialog = ProgressDialog(activity)

        locationAuthority()
        setupObserve()

        return binding.root
    }
    
    override fun onResume() {
        super.onResume()
        locationAuthority()
    }

    private fun locationAuthority() {
        if (checkSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
            checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (shopService.location.value != null && !isFirst) {
                viewModel.getData()
            } else {
                val handler = Handler()
                val run = Runnable {
                    kotlin.run {
                        progressDialog?.dismiss()
                    }
                }
                progressDialog?.apply {
                    setTitle(getString(R.string.loading))
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    show()
                }
                handler.postDelayed(run, 3000)
            }
        } else {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setMessage(R.string.alert_dialog)
                .setPositiveButton(R.string.yes) { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    requireActivity().startActivity(intent)
                }
                .setNegativeButton(R.string.no) { _, _ ->

                }
            builder.create()
            builder.show()
        }
        isFirst = false
    }

    private fun checkSinglePermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(requireActivity().applicationContext, permission) == PackageManager.PERMISSION_GRANTED

    private fun setupObserve() {
        shopService.location.observe(viewLifecycleOwner) {
            viewModel.getData()
            progressDialog?.dismiss()
        }
        viewModel.data.observe(viewLifecycleOwner) {
            if (it == null) {
                d("オブザーブ内null")
                GlobalScope.launch(Dispatchers.Main) {
                    progressDialog?.dismiss()
                    binding.apply {
                        shopPhoto.visibility = View.GONE
                        nameLabel.visibility = View.GONE
                        shopName.visibility = View.GONE
                        addressLabel.visibility = View.GONE
                        shopAddress.visibility = View.GONE
                        alertMessage.visibility = View.GONE
                    }
                    AlertDialog.Builder(requireActivity())
                        .setMessage(getString(R.string.no_search_result))
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            val mainFragment = this@ShuffleFragment.parentFragment as MainFragment
                            val viewPager = activity?.findViewById(R.id.viewPager) as ViewPager2
                            viewPager.currentItem = 0
                            mainFragment.openBottomSheet()
                        }
                        .show()
                    d("店のdataが見つかりませんでした")
                }
            } else {
                d("data observe")
                GlobalScope.launch(Dispatchers.Main) {
                    binding.apply {
                        shopPhoto.visibility = View.VISIBLE
                        nameLabel.visibility = View.VISIBLE
                        shopName.visibility = View.VISIBLE
                        addressLabel.visibility = View.VISIBLE
                        shopAddress.visibility = View.VISIBLE
                        Picasso.get().load(it.image).resize(72, 72).into(shopPhoto)
                        shopName.text = it.name
                        shopAddress.text = it.address
                        alertMessage.visibility = View.GONE
                    }
                }
                progressDialog?.dismiss()
            }
        }
    }
}
