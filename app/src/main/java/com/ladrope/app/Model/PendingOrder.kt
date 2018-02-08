package com.ladrope.app.Model

/**
 * Created by USER on 2/8/18.
 */
class PendingOrder() {
    var accepted: Boolean? = null
    var clientAddress: String? = null
    var clothId: String? = null
    var cost: Any? = null
    var date: Any? = null
    var displayName: String? = null
    var email: String? = null
    var image1: String? = null
    var label: String? = null
    var labelEmail: String? = null
    var labelId: String? = null
    var labelPhone: String? = null
    var name: String? = null
    var orderId: String? = null
    var price: Long? = null
    var size: Any? = null
    var startDate: Any? = null
    var status: String? = null
    var user: String?= null
    var userOrderKey: String? = null
    var options: Any? = null
    var time: Any? = null


    constructor(name: String?, clothId: String?, cost: Any?, clientAddress: String?, image1: String?,
                accepted: Boolean?, date: Any?, displayName: String?, label: String?,
                labelEmail: String?, labelPhone: String?, labelId: String?,
                email: String?, orderId: String?, price: Long?,
                size: Any?, startDate: Any?, status: String?, user: String?,
                userOrderKey: String?, options: Any?, time: String): this(){


        this.clothId = clothId
        this.cost = cost
        this.accepted = accepted
        this.image1 = image1
        this.date = date
        this.clientAddress = clientAddress
        this.displayName = displayName
        this.label = label
        this.labelEmail = labelEmail
        this.labelId = labelId
        this.labelPhone = labelPhone
        this.email = email
        this.orderId = orderId
        this.size = size
        this.price = price
        this.startDate = startDate
        this.name = name
        this.user = user
        this.status = status
        this.userOrderKey = userOrderKey
        this.options = options
        this.time = time
    }
}