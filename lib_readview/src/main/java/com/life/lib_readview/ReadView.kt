package com.life.lib_readview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.abs


class ReadView : FrameLayout {
    private var chapterCur: CEntity? = null
    private var chapterLastTemp: CEntity? = null //用于判断是否加载下一章
    private var chapterStartTemp: CEntity? = null//用于判断是否加载上一章

    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ViewAdapter
    private lateinit var textView: SplitTextView
    private lateinit var viewModel: ViewModelRead


    object LoadPosition {
        val PRE = "PRE";//上一章
        val RESET = "CURRENT";//当前章
        val ADD = "NEXT";//下一章
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        viewModel = ViewModelProvider((context as ViewModelStoreOwner))[ViewModelRead::class.java]
        //添加textview
        textView = SplitTextView(context)
        textView.apply {
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.topMargin =
                getContext().resources.getDimension(R.dimen.title_height).toInt()
            layoutParams = params
            ConfigUtils.setTextviewConfig(ReadConfig(), this)
            this@ReadView.addView(this)
        }
        //添加viewpager2
        viewPager = ViewPager2(context)
        viewPager.apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            viewPagerAdapter =
                ViewAdapter()
            viewPager.adapter = viewPagerAdapter
            this@ReadView.addView(this)
        }
        //页面配置参数如字体大小改变则重新分页
        viewModel.readConfig.observe((context as LifecycleOwner), Observer<ReadConfig> {
            //TODO 布局属性改变了，配置信息需保存到本地
            ConfigUtils.setTextviewConfig(it, textView)
            viewPagerAdapter.setConfig(it)
            chapterCur?.content?.let {
                splitText(chapterCur!!, it, LoadPosition.RESET)
            }
        })

        //页面切换动画
        initViewPagerTransformer()

        //滑动监听
        initViewPagerListener()

    }

    /**
     * 页面切换动画
     */
    private fun initViewPagerTransformer() {
        viewPager.setPageTransformer { page, position ->
            if (position <= 0.0f) {
                //被滑动的那页及之前全部的已被划走的页
                page.translationX = 0.0f
                page.translationZ = 0.0f
            } else {
                //在被滑动页下方的页
                //设置每一页相对于【其自身左侧】的偏移
                page.translationX = -page.width * position
                page.translationZ = -position
            }
        }
    }

    // viewpager滑动状态判断，true滑动了，false没有滑动 左右滑动监听参数
    private var isScrolling = false
    private var downX = 0f // 按下时x坐标
    private var isLeft = false // 是否往左滑
    private var isCacheing = false;//是否正在加载缓存

    //点击事件监听
    private var lastDownTime: Long = 0; //上次点击落下的时间
    private var lastUpOperateTime: Long = 0;//防止连续点击
    private var modelLTR = true //左右滑动模式或上下滑动
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                lastDownTime = System.currentTimeMillis()
            }

            MotionEvent.ACTION_UP -> {
                isLeft = ev.x < downX//别处使用，判断是否左滑
                if (abs(ev.x - downX) < 50) {//不是滑动，而是点击事件处理
                    val upTime = System.currentTimeMillis()
                    if (upTime - lastDownTime < 300 && upTime - lastUpOperateTime > 300) {//不是滑动，而是点击事件处理（小于300毫秒），后边时间判断是为了防止连续点击
                        lastUpOperateTime = upTime
                        //左右滑动模式
                        if (modelLTR) {
                            if (ev.y > height * 0.3 && ev.y < height * 0.65
                                && ev.x >= width * 0.2 && ev.x <= width * 0.8
                            ) {
                                mMiddleClickListener.invoke();
                            } else if (ev.x > width * 0.5) {
                                viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                            } else if (ev.x < width * 0.5) {
                                viewPager.setCurrentItem(viewPager.currentItem - 1, true)
                            }
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     *滑动监听
     */
    private fun initViewPagerListener() {
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            var position = 0;
            lateinit var firstCEntity: CEntity
            lateinit var lastCEntity: CEntity
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                chapterCur = viewPagerAdapter.getCEntityFromPosition(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                //state 1开始滑动 2滑动结束 0什么都没做 滑动过程1-2-0
                if (state == 1) isScrolling = false

                if (state == 2 && !isCacheing) {
                    isScrolling = true
                    lastCEntity = viewPagerAdapter.getLastCEntity()
                    firstCEntity = viewPagerAdapter.getFirstCEntity()
                    if (isLeft && chapterLastTemp != lastCEntity) {
                        position = viewPager.currentItem
                        if (viewPagerAdapter.isCacheNext(position)) {
                            chapterLastTemp = lastCEntity
                            println(
                                "滑动监听获取下一章，当前以及章节信息：position" + position + "---" +
                                        lastCEntity.name + "---" + lastCEntity.charpterNum
                            )
                            loader(LoadPosition.ADD, lastCEntity)
                        }
                    }
                    if (!isLeft && chapterStartTemp != firstCEntity) {
                        position = viewPager.currentItem
                        if (viewPagerAdapter.isCachePre(position)) {
                            println(
                                "滑动监听 获取上一章，当前以及章节信息position：" + position + "---" +
                                        firstCEntity.name + "---" + firstCEntity.charpterNum
                            )
                            loader(LoadPosition.PRE, firstCEntity)
                            chapterStartTemp = firstCEntity
                        }
                    }

                }

                if (state == 0) {
                    if (!isScrolling) {  // 没有滑动，说明滑到底了
                        if (isLeft && !isCacheing && viewPager.currentItem == viewPagerAdapter.itemCount - 1) {
                            // 左滑，查看下一页
                            lastCEntity = viewPagerAdapter.getLastCEntity()
                            loader(LoadPosition.ADD, lastCEntity)
                        }
                        if (!isLeft && !isCacheing && viewPager.currentItem == 0) {
                            // 右滑，查看上一页
                            firstCEntity = viewPagerAdapter.getFirstCEntity()
                            loader(LoadPosition.PRE, firstCEntity)
                        }
                    }
                }
            }
        })
    }


    /**
     * 分割章节文本
     */
    private fun splitText(c: CEntity, text: String, type: String) {
        textView.splitContent(text, object : SplitTextView.OnSplitContentListener {
            override fun onSuccess(pages: ArrayList<String>?) {
                pages?.let {
                    c.pages.clear()
                    c.pages.addAll(it)
                    when (type) {
                        LoadPosition.RESET -> viewPagerAdapter.resetDatas(c)
                        LoadPosition.ADD -> {
                            viewPagerAdapter.insert(c)
                            viewPagerAdapter.removePre()
                        }

                        LoadPosition.PRE -> {
                            viewPagerAdapter.insertPre(c)
//                            viewPaerAdapter.removeNext()
                        }
                    }
                    //若只有一页数据则加载下一章
                    if (viewPagerAdapter.itemCount <= 1) {
                        println("消息，只有一页，加载下章")
                        loader(LoadPosition.ADD, chapterCur)
                    }

                }
            }
        })
    }

    /**
     * 加载器
     */
    private fun loader(flag: String, chapter: CEntity?) {
        //准备加载
        isCacheing = true
        MainScope().launch {
            mLoadListener.invoke(flag, chapter).let {
                isCacheing = false
                //加载完成
                it?.let {
                    when (flag) {
                        LoadPosition.PRE -> setPreChapter(it) //上一章
                        LoadPosition.RESET -> setCurChapter(it) //当前章
                        LoadPosition.ADD -> setNextChapter(it)//下一章
                    }
                }
            }
        }
    }

    /**
     * 设置当前章节
     */
    private fun setCurChapter(chapter: CEntity) {
        chapterCur = chapter
        chapterCur?.content?.let { splitText(chapterCur!!, it, LoadPosition.RESET) }
    }

    /**
     * 设置下一章
     */
    private fun setNextChapter(chapter: CEntity) {
        chapter?.content?.let { splitText(chapter!!, it, LoadPosition.ADD) }
    }

    /**
     * 设置上一章
     */
    private fun setPreChapter(chapter: CEntity) {
        chapter?.content?.let { splitText(chapter!!, it, LoadPosition.PRE) }
    }


    //加载监听
    private lateinit var mLoadListener:
            suspend (flag: String, loadFromChapter: CEntity?) -> CEntity?

    /**
     * 第一个参数加载标记，即成功之后添加的位置
     * 第二个参数：加载时需要参照的章节，比如当前显示的是第一章，可能已经缓存到第三章了，需要加载第四章，就根据这个第三章去判断
     */
    fun setOnLoadListener(
        listener: suspend (flag: String, loadFromChapter: CEntity?) -> CEntity?
    ) {
        mLoadListener = listener
    }

    private lateinit var mMiddleClickListener: () -> Unit

    //点击中间区域，一般显示菜单
    fun setOnMiddleClickListener(listener: () -> Unit) {
        mMiddleClickListener = listener
    }

    /**
     * 设置当前页面
     */
    fun loadCurrent() {
        loader(LoadPosition.RESET, null)
    }

    /**
     * 设置配置信息
     */
    fun setConfig(config: ReadConfig) {
        viewModel.setConfig(config)
    }

    fun getCurrentChapter(): CEntity {
        return chapterCur!!
    }
}
