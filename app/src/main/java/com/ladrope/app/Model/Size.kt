package com.ladrope.app.Model

class Size() {
    var ankle: String? = null
    var belly: String? = null
    var bicep: String? = null
    var chest: String? = null
    var fullback: String? = null
    var head: String? = null
    var hips: String? = null
    var neck: String? = null
    var shoulder: String? = null
    var sleeve: String? = null
    var thigh: String? = null
    var trouserLength: String? = null
    var waist: String? = null
    var wrist: String? = null
    var unit: String? = null
    var model: String? = null


    constructor(ankle: String?,
                belly: String?,
                bicep: String?,
                chest: String?,
                fullback: String?,
                head: String?,
                hips: String?,
                neck: String?,
                shoulder: String?,
                sleeve: String?,
                thigh: String?,
                trouserLength: String?,
                waist: String?,
                wrist: String?,
                model: String?,
                unit: String?): this(){

        this.ankle = ankle
        this.belly = belly
        this.bicep = bicep
        this.chest = chest
        this.fullback = fullback
        this.head = head
        this.hips = hips
        this.neck = neck
        this.shoulder = shoulder
        this.sleeve = sleeve
        this.thigh = thigh
        this.trouserLength = trouserLength
        this.waist = waist
        this.wrist = wrist
        this.model = model
        this.unit = unit
    }
}