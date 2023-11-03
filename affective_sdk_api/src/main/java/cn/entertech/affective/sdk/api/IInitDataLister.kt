package cn.entertech.affective.sdk.api

interface IInitDataLister {
    fun initSuccess()

    fun initBioFail()

    fun initAffectionFail()

    fun initFail()
}