package cn.entertech.affective.compat;

import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService;
import cn.entertech.affective.sdk.bean.AffectiveServiceWay;
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy;

public final class LegacyJavaConsumer {
    private LegacyJavaConsumer() {
    }

    public static String consume() {
        EnterAffectiveConfigProxy config = new EnterAffectiveConfigProxy();
        IAffectiveDataAnalysisService service =
                IAffectiveDataAnalysisService.Companion.getService(
                        AffectiveServiceWay.AffectiveLocalService
                );
        if (service == null) {
            return config.getUserId();
        }
        String sessionId = service.getSessionId();
        service.closeAffectiveServiceConnection();
        return sessionId;
    }
}
