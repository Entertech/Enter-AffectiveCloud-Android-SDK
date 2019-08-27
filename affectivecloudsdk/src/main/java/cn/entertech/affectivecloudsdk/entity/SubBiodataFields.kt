package cn.entertech.affectivecloudsdk.entity

data class SubBiodataFields(var subEEGFields:List<String>? = null, var subHrFields:List<String>? = null){
    override fun toString(): String {
        return "SubBiodataFields(subEEGFields=$subEEGFields, subHrFields=$subHrFields)"
    }
}