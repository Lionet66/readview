package com.life.lib_readview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.life.lib_readview.ViewAdapter.HorizontalVpViewHolder
import com.life.lib_readview.databinding.ItemReadviewBinding

class ViewAdapter : RecyclerView.Adapter<HorizontalVpViewHolder>() {
    private val datas: MutableList<TempCentity> = ArrayList()
    private var dataCEntitys: MutableList<CEntity> = ArrayList()

    //    private val dataCEntitys: MutableList<CEntity> = ArrayList()
    private var config: ReadConfig?

    init {
        this.config = ReadConfig()
    }

    @SuppressLint("ResourceType")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalVpViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemReadviewBinding =
            DataBindingUtil.inflate(inflater, R.layout.item_readview, parent, false)
        ConfigUtils.setTextviewConfig(config!!, binding.tvContent)
        return HorizontalVpViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: HorizontalVpViewHolder, position: Int) {
        val binding: ItemReadviewBinding? = DataBindingUtil.getBinding(holder.itemView)
        val tempCentity = datas[position]
        binding?.tvContent?.text = tempCentity.pageStr
        binding?.tvName?.text = tempCentity.cEntity?.name
        val pages = tempCentity.cEntity?.pages;
        binding?.tvPage?.text = "${pages?.indexOf(tempCentity.pageStr)?.plus(1)}/${pages?.size}"
    }

    inner class HorizontalVpViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

    override fun getItemCount(): Int {
        return datas.size
    }

    fun setConfig(config: ReadConfig?) {
        this.config = config
    }

    private fun tranCentityToTemp(chapter: CEntity): MutableList<TempCentity> {
        var temps = mutableListOf<TempCentity>()
        for (s in chapter.pages) {
            temps.add(TempCentity(s, chapter))
        }
        return temps
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetDatas(chapter: CEntity) {
        println("重置数据resetDatas " + chapter.toString())
        datas.clear()
        datas.addAll(tranCentityToTemp(chapter))
        dataCEntitys.clear()
        dataCEntitys.add(chapter)
        notifyDataSetChanged()
    }

    fun insert(chapter: CEntity) {
        datas.addAll(tranCentityToTemp(chapter))
        dataCEntitys.add(chapter)
        notifyItemRangeInserted(datas.size, chapter.pages.size)
    }

    fun insertPre(chapter: CEntity) {
        datas.addAll(0, tranCentityToTemp(chapter))
        dataCEntitys.add(0, chapter)
        notifyItemRangeInserted(0, chapter.pages.size)
    }

    fun getCEntityFromPosition(position: Int): CEntity {
        return datas[position].cEntity!!
    }

    /**
     * 获取最后一个CEntity
     */
    fun getLastCEntity(): CEntity {
        return datas[datas.size - 1].cEntity!!
    }

    //加载更多
    fun isCacheNext(position: Int): Boolean {
        return dataCEntitys.indexOf(datas[position].cEntity) >= dataCEntitys.size - 2
    }

    //
    fun isCachePre(position: Int): Boolean {
        return dataCEntitys.indexOf(datas[position].cEntity) <= 1
    }

    fun getFirstCEntity(): CEntity {
        return datas[0].cEntity!!
    }

    /**
     * 移除数据
     *
     * @param list
     */
    private var removePageNum: Int = 0
    private val minChapterNum = 5 //保留(minChapterNum - 1)章内容,如minChapterNum=3则保留2章内容
    fun removePre() {
        if (dataCEntitys.size >= minChapterNum) {
            removePageNum = 0
            println("未移除@@@@@@@@@@@@ 前总共" + datas.size + "----dataCEntitys总" + dataCEntitys.size)
            for (i in 0..dataCEntitys.size - minChapterNum) {
                removePageNum += dataCEntitys[i].pages.size
                println("移除---" + i + "---" + dataCEntitys[i].toString())
            }
            datas.subList(0, removePageNum).clear()
            dataCEntitys.subList(0, dataCEntitys.size - (minChapterNum - 1)).clear()
            notifyItemRangeRemoved(0, removePageNum);
            println("移除@@@@@@@@@@@@  后剩余" + datas.size + "----dataCEntitys剩余" + dataCEntitys.size + "---移除数量：" + removePageNum)
        }
    }

//    fun removeNext() {
//        if (dataCEntitys.size >= minChapterNum) {
//            removePageNum = 0
//            println("未移除@@@@@@@@@@@@ 前总共" + datas.size + "----dataCEntitys总" + dataCEntitys.size)
//            for (i in minChapterNum - 1..dataCEntitys.size - 1) {
//                removePageNum += dataCEntitys[i].pages.size
//                println("移除---" + i + "---" + dataCEntitys[i].toString())
//            }
//            datas.subList(datas.size - removePageNum, datas.size).clear()
//            dataCEntitys.subList(minChapterNum - 1, dataCEntitys.size).clear()
//            notifyItemRangeRemoved(removePageNum - 1, removePageNum);
//            println("移除@@@@@@@@@@@@  后剩余" + datas.size + "----dataCEntitys剩余" + dataCEntitys.size + "---移除数量：" + removePageNum)
//        }
//    }
}