package com.example.myapplication

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.ImageView
import android.os.Build
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class Texting : AppCompatActivity() {

    private lateinit var imgButton : ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter

    private var lastImeInset = 0

    private fun RecyclerView.isNearBottom(
        thresholdPx: Int = (64 * resources.displayMetrics.density).toInt()
    ): Boolean {
        val range = computeVerticalScrollRange()
        val offset = computeVerticalScrollOffset()
        val extent = computeVerticalScrollExtent()
        val distanceToBottom = range - (offset + extent)
        return distanceToBottom <= thresholdPx
    }

    private fun RecyclerView.scrollToBottom(smooth: Boolean = false) {
        val last = (adapter?.itemCount ?: 0) - 1
        if (last >= 0) {
            if (smooth) smoothScrollToPosition(last) else scrollToPosition(last)
        }
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.texting)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.purple))
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(R.id.main)
        val inputBar = findViewById<View>(R.id.linearLayout)
        recyclerView = findViewById(R.id.chat_recycler_view)

        val initRootPadLeft   = root.paddingLeft
        val initRootPadTop    = root.paddingTop
        val initRootPadRight  = root.paddingRight
        val initRootPadBottom = root.paddingBottom

        val lp = inputBar.layoutParams as ConstraintLayout.LayoutParams
        val initInputMarginBottom = lp.bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                initRootPadLeft  + sys.left,
                initRootPadTop,
                initRootPadRight + sys.right,
                initRootPadBottom
            )

            val newBottomInset = maxOf(sys.bottom, ime.bottom)
            val newBottomMargin = initInputMarginBottom + newBottomInset
            if (lp.bottomMargin != newBottomMargin) {
                lp.bottomMargin = newBottomMargin
                inputBar.layoutParams = lp
            }

            val delta = newBottomInset - lastImeInset
            if (delta != 0) {
                if (delta > 0) {
                    recyclerView.post { recyclerView.scrollBy(0, delta) }
                    lastImeInset = newBottomInset
                }
                lastImeInset = newBottomInset
            }
            insets
        }

        val person: Person? = intent.getParcelableExtra("person", Person::class.java)

        imgButton = findViewById(R.id.texting_to_chat)
        imgButton.setOnClickListener {
            finish()
        }
        val nameView = findViewById<TextView>(R.id.nameView)
        val onlineView = findViewById<TextView>(R.id.onlineView)
        val imageView = findViewById<ImageView>(R.id.avatarView)
        val nameOnline = findViewById<LinearLayout>(R.id.nameOnline)

        if (person != null) {
            nameView.text = person.fullName
            onlineView.text = person.online
            imageView.setImageResource(person.img)
        }

        imageView.setOnClickListener {
            val intent = Intent(this, Account::class.java)
            intent.putExtra("person", person)
            startActivity(intent)
        }

        nameOnline.setOnClickListener {
            val intent = Intent(this, Account::class.java)
            intent.putExtra("person", person)
            startActivity(intent)
        }

        val chatMessage = MessageData.messageList
            .filter { it.idPerson == person?.idPerson }
            .sortedBy { it.dataTime }

        adapter = MessageAdapter(chatMessage)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        recyclerView.adapter = adapter


        val editText = findViewById<EditText>(R.id.message_text_edit)
        editText.isVerticalScrollBarEnabled = true
        editText.movementMethod = ScrollingMovementMethod.getInstance()

        editText.requestFocus()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText.findFocus(), InputMethodManager.SHOW_IMPLICIT)

        val addFileButton = findViewById<ImageButton>(R.id.add_file_button)
        val sendButton = findViewById<ImageButton>(R.id.send_button)
        var isActive = false

        editText.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                addFileButton.animateShow()
                sendButton.setImageResource(R.drawable.micro)
                sendButton.imageTintList = ColorStateList.valueOf((ContextCompat.getColor(this, R.color.gray)))
                isActive = false

            } else {
                addFileButton.animateHide()
                sendButton.setImageResource(R.drawable.send)

                val typedValue = TypedValue()
                val theme = sendButton.context.theme
                theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)

                sendButton.imageTintList = ColorStateList.valueOf(typedValue.data)
                isActive = true
            }
        }

        sendButton.setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isNotEmpty())
            {
                val message = Message(
                    idPerson = person?.idPerson ?: 0,
                    toFrom = true,
                    dataTime = System.currentTimeMillis(),
                    textMessage = text,
                    status = MessageStatus.SENT
                )

                MessageData.messageList = (MessageData.messageList + message) as MutableList<Message>

                adapter = MessageAdapter(
                    MessageData.messageList
                        .filter { it.idPerson == person?.idPerson }
                        .sortedBy { it.dataTime }
                )
                recyclerView.adapter = adapter

                recyclerView.scrollToPosition(adapter.itemCount - 1)

                editText.text.clear()
            }
        }
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
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val inputBar = findViewById<View>(R.id.linearLayout)
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            val barRect = android.graphics.Rect()
            inputBar.getGlobalVisibleRect(barRect)

            if (!barRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                currentFocus?.let { v ->
                    v.clearFocus()
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

}