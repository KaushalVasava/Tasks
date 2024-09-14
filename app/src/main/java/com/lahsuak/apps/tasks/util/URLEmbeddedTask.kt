package com.lahsuak.apps.tasks.util

import com.nguyencse.URLConstants
import com.nguyencse.URLEmbeddedData
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.net.URL

class URLEmbeddedTask {
    suspend fun getResult(vararg params: String): URLEmbeddedData {
        val data = URLEmbeddedData()
        try {
            if(params.isNotEmpty()) {

                var url = params[0]
                url =
                    (if ((url.startsWith(URLConstants.PROTOCOL) || url.startsWith(URLConstants.PROTOCOL_S))) "" else URLConstants.PROTOCOL) + url

                val host = URL(url)
                data.host = host.host

                val doc: Document = Jsoup.connect(url).get()
                val elements: Elements = doc.select("meta")
                for (e in elements) {
                    val tag: String = e.attr("property").lowercase()
                    val content: String = e.attr("content")
                    when (tag) {
                        "og:url" -> {
                            val urlNew = URL(content)
                            data.host = urlNew.host
                        }

                        "og:image" -> data.thumbnailURL = content
                        "og:title" -> data.title = content
                        "og:description" -> data.description = content
                    }
                }
                data.favorURL = URLConstants.ROOT_URL_FAVOR_ICON + data.host
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return data
    }

//    fun onCompleted(result: URLEmbeddedData) {
//        listener?.onLoadURLCompleted(result)
//    }
//
//    interface OnLoadURLListener {
//        fun onLoadURLCompleted(data: URLEmbeddedData?)
//    }
}
