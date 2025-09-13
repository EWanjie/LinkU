package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.com.example.myapplication.ChatAdapter
import com.example.myapplication.model.com.example.myapplication.buildChatItems

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Chat.newInstance] factory method to
 * create an instance of this fragment.
 */
class Chat : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: ChatAdapter

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

        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

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


        val chats = buildChatItems(
            allMessages = MessageData.messageList,
            allPersons  = PersonData.personList
        )
        adapter.submitList(chats)
    }

    private lateinit var imgButton : ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgButton = view.findViewById(R.id.main_to_find)

        imgButton.setOnClickListener {
            val intent = Intent(requireContext(), FindActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.chat)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ChatAdapter { item ->
            val person = PersonData.personList.find { it.idPerson == item.personId }
            val intent = Intent(requireContext(), Texting::class.java).apply {
                putExtra("person", person)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val chatItems = buildChatItems(
            allMessages = MessageData.messageList,
            allPersons = PersonData.personList
        )

        adapter.submitList(chatItems)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Chat.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Chat().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}