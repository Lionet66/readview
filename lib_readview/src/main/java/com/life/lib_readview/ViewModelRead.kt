package com.life.lib_readview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelRead : ViewModel() {
    var readConfig: MutableLiveData<ReadConfig> = MutableLiveData()

    init {
        readConfig.value = ReadConfig()
    }

    fun setConfig(config: ReadConfig) {
        readConfig.value = config
    }
}
