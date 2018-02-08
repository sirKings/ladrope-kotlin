package com.ladrope.app.Model

/**
 * Created by USER on 2/1/18.
 */
class Cloth() {
    var name: String? = null
    var clothKey: String? = null
    var cost: Any? = null
    var gender: String? = null
    var image1: String? = null
    var image2: String? = null
    var image3: String? = null
    var image4: String? = null
    var label: String? = null
    var labelEmail: String? = null
    var labelId: String? = null
    var labelPhone: String? = null
    var likes: Int? = null
    var numComment: Int? = null
    var numSold: Int? = null
    var price: Long? = null
    var rating: Double? = null
    var tags: String? = null
    var tailorKey: String? = null
    var time: Any? = null
    var fabricType: String? = null
    var description: String? = null
    var options: Any? = null
    var cartKey: String? = null
    var selectedOption: Any? = null


    constructor(name: String?, clothKey: String?, cost: String?, gender: String?, image1: String?,
    image2: String?, image3: String?, image4: String?, label: String?,
                labelEmail: String?, labelPhone: String?, labelId: String?,
                likes: Int?, numComment: Int?, numSold: Int?, price: Long?,
                rating: Double?, tags: String?, fabricType: String, description: String?,
                tailorKey: String?, time: Any?, options: Any?, cartKey: String?, selectedOption: Any): this(){
        this.clothKey = clothKey
        this.cost = cost
        this.gender = gender
        this.image1 = image1
        this.image2 = image2
        this.image3 = image3
        this.image4 = image4
        this.label = label
        this.description = description
        this.labelEmail = labelEmail
        this.labelId = labelId
        this.labelPhone = labelPhone
        this.likes = likes
        this.numComment = numComment
        this.numSold = numSold
        this.price = price
        this.rating = rating
        this.tags = tags
        this.tailorKey = tailorKey
        this.time = time
        this.name = name
        this.fabricType = fabricType
        this.options = options
        this.cartKey = cartKey
        this.selectedOption = selectedOption

    }
}