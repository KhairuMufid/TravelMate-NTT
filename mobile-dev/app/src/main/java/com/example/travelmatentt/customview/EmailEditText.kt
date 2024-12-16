package com.example.travelmatentt.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.travelmatentt.R

class EmailEditText : AppCompatEditText {

    private lateinit var emailIcon: Drawable

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {

        emailIcon = ContextCompat.getDrawable(context, R.drawable.email_24dp) as Drawable
        setEditCompoundDrawables(startOfTheText = emailIcon)

        compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.icon_text_padding)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    setError(resources.getString(R.string.invalid_email), null)
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun setEditCompoundDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }
}