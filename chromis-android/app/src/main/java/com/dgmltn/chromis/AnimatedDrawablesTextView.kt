package com.dgmltn.chromis

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by dmelton on 4/18/17.
 */

class AnimatedDrawablesTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : TextView(context, attrs, defStyle) {

    override fun setCompoundDrawablesWithIntrinsicBounds(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        listOf(left, top, right, bottom).forEach { (it as? Animatable)?.start() }
    }
}
