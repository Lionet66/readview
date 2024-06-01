package com.life.lib_readview

import android.content.Context
import android.graphics.Color

object ConfigUtils {
    /**
     * 设置textview属性配置。
     */
    @JvmStatic
    fun setTextviewConfig(config: ReadConfig, splitTextView: SplitTextView) {
        splitTextView.textSize = config.textSize.toFloat()
        splitTextView.setTextColor(Color.parseColor(config.textColor))
        splitTextView.setBackgroundColor(Color.parseColor(config.bgColor))
        config.bgResId?.let { splitTextView.setBackgroundResource(it) }
        config.letterSpacing?.let { splitTextView.letterSpacing = it }
        val padding = dp2px(splitTextView.context, config.padding)
        splitTextView.setPadding(padding, padding, padding, padding)
    }


    @JvmStatic
    fun dp2px(context: Context, dp: Int): Int {
        val scale: Float = context.resources.displayMetrics.density
        return return (dp * scale + 0.5f).toInt()
    }
}


//fun Int.dp2px(): Int {
//    val scale: Float = MyApp.newInstance().resources.displayMetrics.density
//    return return (this * scale + 0.5f).toInt()
//}


/**
 * 将字符串中所有连续的空格替换为两个空格
 *
 * @param input 输入的字符串
 * @return 替换后的字符串
 */
fun String.formatChapter(): String {
    val sb = StringBuilder()
    var lastCharWasSpace = false
    val c = 12288.toChar()
    val start = c + "" + c

    var array = mutableListOf<Char>().apply {
        addAll(listOf('\n', '\t', c, ' '))
        toCharArray()
    }

    for (i in 0 until length) {
        val c = this[i]

        // 如果当前字符是空格
        if (c in array) {
            // 如果上一个字符也是空格，则不添加空格到结果中
            if (lastCharWasSpace) {
                continue
            }
            // 否则，添加一个空格到结果中，并标记上一个字符是空格
            if (i == 0) {
                sb.append(start)
            } else if (i != length) {
                sb.append("\n\n")
                sb.append(start)
            }
            lastCharWasSpace = true
        } else {
            // 如果当前字符不是空格，添加它到结果中，并标记上一个字符不是空格
            sb.append(c)
            lastCharWasSpace = false
        }
    }
    return sb.toString()
}
