package cn.entertech.affectivecloudsdk.entity

 class RealtimeDceegSsvepData {
     var eegAlphaPower: Double? = null
     var eegBetaPower: Double? = null
     var eegDeltaPower: Double? = null
     var eegGammaPower: Double? = null
     var eegQuality: Int? = null
     var eegThetaPower: Double? = null
     var eeglWave: List<Double>? = null
     var eegrWave: List<Double>? = null
     var ssvepFreqCorr: Map<String, Double>? = null
     var ssvepFreqPower: Map<String, Double>? = null
     override fun toString(): String {
         return "RealtimeDceegSsvepData(eegAlphaPower=$eegAlphaPower, eegBetaPower=$eegBetaPower, eegDeltaPower=$eegDeltaPower, eegGammaPower=$eegGammaPower, eegQuality=$eegQuality, eegThetaPower=$eegThetaPower, eeglWave=$eeglWave, eegrWave=$eegrWave, ssvepFreqCorr=$ssvepFreqCorr, ssvepFreqPower=$ssvepFreqPower)"
     }


 }

