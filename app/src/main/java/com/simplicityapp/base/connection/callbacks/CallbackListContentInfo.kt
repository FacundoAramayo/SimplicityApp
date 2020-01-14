package com.simplicityapp.base.connection.callbacks

import java.io.Serializable
import java.util.ArrayList

import com.simplicityapp.modules.notifications.model.ContentInfo

class CallbackListContentInfo : Serializable {

    var status = ""
    var count = -1
    var count_total = -1
    var pages = -1
    var news_infos: List<ContentInfo> = ArrayList()

}
