package jp.re.tanmen.view.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import jp.re.tanmen.databinding.FragmentDetailBinding
import jp.re.tanmen.model.Entity.Shop
import timber.log.Timber

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    @SuppressLint("FragmentBackPressedCallback")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)

        val args = arguments?.getSerializable("shopDetail") as Shop
        if (args != null) {
            binding.apply {
                Picasso.get().load(args.image).resize(300, 300).into(shopPhoto)
                shopName.text = args.name
                shopAddress.text = args.address
                shopBusinessHours.text = args.hours
            }
        }
        Timber.d("$args")

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager
                    .beginTransaction()
                    .remove(this@DetailFragment)
                    .commit()
            }
        })

        return binding.root
    }

}