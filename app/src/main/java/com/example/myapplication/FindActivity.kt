package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FindActivity : AppCompatActivity(), MyAdapter.OnPersonClickListener {
    private lateinit var imgButton : ImageButton
    private lateinit var myAdapter: MyAdapter
    private lateinit var personList: List<Person>
    private lateinit var noContactsTextView: TextView
    private lateinit var recyclerview: RecyclerView


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("DiscouragedApi", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        setContentView(R.layout.activity_find)

        val editTextSearch = findViewById<EditText>(R.id.search_edit_text)
        val buttonClear = findViewById<ImageButton>(R.id.clean_button)
        noContactsTextView = findViewById(R.id.message_text)

        personList = PersonData.personList

        editTextSearch.requestFocus()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextSearch.findFocus(), InputMethodManager.SHOW_IMPLICIT)

        editTextSearch.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                buttonClear.animateHide()
                myAdapter.setFilterAdapter(personList)
                noContactsTextView.visibility = View.GONE
                recyclerview.visibility = View.VISIBLE
            } else {
                buttonClear.animateShow()
                filterList(it.toString())
            }
        }

        buttonClear.setOnClickListener {
            editTextSearch.text.clear()
            myAdapter.setFilterAdapter(personList)
            noContactsTextView.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
        }

        recyclerview = findViewById(R.id.recycler_view)

        recyclerview.layoutManager = LinearLayoutManager(this)
        myAdapter = MyAdapter(personList, this)
        recyclerview.adapter = myAdapter

        imgButton = findViewById(R.id.find_to_main)
        imgButton.setOnClickListener {
            finish()
        }

        this.showWeatherNotification2()
        val message = Message(
            idPerson = 5,
            toFrom = false,
            dataTime = System.currentTimeMillis(),
            textMessage = "Голосовое сообщение",
            status = MessageStatus.SENT
        )

        MessageData.messageList = (MessageData.messageList + message) as MutableList<Message>
    }

    fun filterList(text: String){
        val filterPersonList = personList.filter {
            it.fullName.lowercase().contains(text.lowercase())
        }.sortedBy {
            it.fullName.lowercase().indexOf(text.lowercase())
        }
        myAdapter.setFilterAdapter(filterPersonList)

        if(filterPersonList.isEmpty()){
            noContactsTextView.visibility = View.VISIBLE
            recyclerview.visibility = View.GONE
        }
        else{
            noContactsTextView.visibility = View.GONE
            recyclerview.visibility = View.VISIBLE
        }
    }

    override fun onPersonClick(person: Person) {
        val intent = Intent(this, Texting::class.java)
        intent.putExtra("person", person)
        startActivity(intent)
        finish()
    }

    fun View.animateShow(duration: Long = 150) {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                .start()
        }
    }

    fun View.animateHide(duration: Long = 150) {
        animate()
            .alpha(0f)
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(duration)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction {
                visibility = View.GONE
            }
            .start()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}