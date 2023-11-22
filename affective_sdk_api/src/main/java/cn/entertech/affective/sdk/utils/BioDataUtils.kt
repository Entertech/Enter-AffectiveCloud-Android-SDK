package cn.entertech.affective.sdk.utils

import kotlin.math.pow

object BioDataUtils {

    fun brainwave2Rate(
        alpha: Double,
        beta: Double,
        gamma: Double,
        delta: Double,
        theta: Double,
        brainwaveRate: (
            Double,
            Double,
            Double,
            Double,
            Double
        ) -> Unit
    ) {
        var total = gamma + beta + alpha + theta + theta
        if (total == 1.0) {
            brainwaveRate(
                gamma,
                beta,
                alpha,
                theta,
                theta
            )
            return
        }
        val gammaAbs = db2abs(gamma)
        val betaAbs = db2abs(beta)
        val alphaAbs = db2abs(alpha)
        val thetaAbs = db2abs(theta)
        val deltaAbs = db2abs(delta)
        total = gammaAbs + betaAbs + alphaAbs + thetaAbs + deltaAbs
        if (total != 0.0) {
            brainwaveRate(
                alphaAbs / total,
                betaAbs / total,
                gammaAbs / total,
                deltaAbs / total,
                thetaAbs / total
            )
        } else {
            brainwaveRate(
                gamma,
                beta,
                alpha,
                theta,
                theta
            )
        }
    }

    /**
     * 一些较大数值的权重更大，相对于原始值的变化更为敏感
     * */
    private fun db2abs(db: Double): Double {
        if (db == 0.0) {
            return 0.0
        }
        return 10.0.pow(db / 20)
    }
}