package com.ladrope.app.Model

/**
 * Created by USER on 1/31/18.
 */
class User() {
    var displayName: String? = null
    var email: String? = null
    var photoURL: String? = null
    var address: String? = null
    var coupons: Int? = null
    var gender: String? = null
    var height: Any? = null
    var phone: String? = null
    var size: Any? = null

    constructor(displayName: String?, email: String?, photoURL: String?, address: String?, coupons: Int?, gender: String?, height: Any?, phone: String?, size: Any?): this(){
        this.email = email
        this.displayName = displayName
        this.photoURL = photoURL
        this.address = address
        this.coupons = coupons
        this.gender = gender
        this.height = height
        this.phone = phone
        this.size = size
    }
}