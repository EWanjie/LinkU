package com.example.myapplication

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Call.newInstance] factory method to
 * create an instance of this fragment.
 */
class Call : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: CallAdapter
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_call, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.call_view)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = CallAdapter()
        rv.adapter = adapter
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onResume() {
        super.onResume()

        val w = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(w, true)

        val root = requireView()
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple))
            v.setPadding(0, status.top, 0, 0)
            insets
        }
        ViewCompat.requestApplyInsets(root)

        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = false



        requireContext().showWeatherNotification()

        val message = Message(
            idPerson = 5,
            toFrom = false,
            dataTime = System.currentTimeMillis(),
            textMessage = "Ты где?",
            status = MessageStatus.SENT
        )

        MessageData.messageList = (MessageData.messageList + message) as MutableList<Message>



        val calls: List<CallClass> = CallData.callList
        val items = buildCallItemsWithHeaders(calls)
        adapter.submitList(items)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Call.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Call().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}