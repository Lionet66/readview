package com.life.lib_readview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity

/**
 * 分割文本的textview
 */
class SplitTextView : JustifyTextView {
    private val pages = ArrayList<String>()


    constructor(context: Context?) : super(context) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    private fun init() {
        gravity = Gravity.CENTER
    }

    interface OnSplitContentListener {
        fun onSuccess(pages: ArrayList<String>?)
    }

    private var listener: OnSplitContentListener? = null

    private fun setText(text: String) {
        super.setText(text.formatChapter())
    }

    fun splitContent(text: String, listener: OnSplitContentListener?) {
        this.listener = listener
        post {
            pages.clear()
            setText(text)
            resize()
        }
    }

    /**
     * 去除当前页无法显示的字
     *
     * @return 去掉的字数
     */
    private fun resize() {
        val oldContent = text
        val charNum = charNum
        val newContent = oldContent.subSequence(0, charNum)
        pages.add(newContent.toString())
        text = oldContent.subSequence(charNum, oldContent.length)
        if (charNum != oldContent.length) {
            resize()
        } else {
            listener!!.onSuccess(pages)
        }
    }

    private val charNum: Int
        /**
         * 获取当前页总字数
         */
        get() {
            val lineEnd = layout.getLineEnd(lineNum)
            return lineEnd
        }

    private val lineNum: Int
        /**
         * 获取当前页总行数
         */
        get() {
            val layout = layout
            val topOfLastLine = height - paddingTop - paddingBottom - lineHeight
            val lineForVertical = layout.getLineForVertical(topOfLastLine)
            return lineForVertical
        }

}