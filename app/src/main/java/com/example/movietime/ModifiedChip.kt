package com.example.movietime

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import java.lang.reflect.Method

class ModifiedChip(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    var checked = false
        set(value) {
            field = value
            if (value) {
                setBackgroundResource(R.drawable.chip_selected)
            } else {
                setBackgroundResource(R.drawable.chip_unselected)
            }
        }

    private var onCheckName: String? = null
    var onCheck: Method? = null

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ModifiedChip)
            checked = a.getBoolean(R.styleable.ModifiedChip_checked, false)
            onCheckName = a.getString(R.styleable.ModifiedChip_onCheck)
            if (onCheckName != null) {
                onCheck = context.javaClass.getMethod(onCheckName, this::class.java)
            }
            try {
            } catch (e: NoSuchMethodException) {
                Log.println(Log.ERROR, "Chip", e.toString())
                throw e
            }
            a.recycle()
        }

        setOnClickListener {
            onClick()
        }
        setBackgroundResource(if (checked) R.drawable.chip_selected else R.drawable.chip_unselected)
        textAlignment = TEXT_ALIGNMENT_CENTER
        gravity = android.view.Gravity.CENTER
    }

    private fun onClick() {
        checked = !checked
        onCheck?.invoke(context, this)
    }
}
