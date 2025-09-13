package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.WindowInsetsControllerCompat

class Profile : Fragment() {

    private var blobView: BlobView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        blobView = v.findViewById(R.id.blobView)

        blobView?.apply {
            blobBackgroundColor = Color.BLACK
            startColor = Color.rgb(30, 60, 200)
            endColor   = Color.rgb(90, 0, 180)
            rings = 2
            showRings = true
            speed = 0.008f
        }
        return v
    }

    override fun onDestroyView() {
        blobView = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .isAppearanceLightStatusBars = false
    }
}