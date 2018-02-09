package com.ladrope.app.Model

/**
 * Created by USER on 2/9/18.
 */
class Comment() {
    var title: String? = null
    var message: String? = null

    constructor(message: String?, title: String?): this(){
        this.message = message
        this.title = title
    }
}