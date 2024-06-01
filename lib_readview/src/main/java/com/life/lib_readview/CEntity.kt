package com.life.lib_readview

class CEntity {
    var charpterNum: Int = 0 //列表索引
    var name: String = "" //章节名
    var content: String = "" //章节内容
    var pages = ArrayList<String>()  //章节分页内容

    constructor(name: String, content: String) {
        this.name = name
        this.content = content
    }

}
