package com.ladrope.app.Model

/**
 * Created by USER on 2/9/18.
 */
class News() {
    var description: String? = null
    var image: String? = null
    var title: String? = null
    var link: String? = null

    constructor(link: String?, title: String?, image: String?, description: String?): this(){
        this.link = link
        this.title = title
        this.image = image
        this.description = description
    }
}