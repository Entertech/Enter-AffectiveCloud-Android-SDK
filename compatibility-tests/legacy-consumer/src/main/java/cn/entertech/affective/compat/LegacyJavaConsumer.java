package cn.entertech.affective.compat;

import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy;

public final class LegacyJavaConsumer {
    private LegacyJavaConsumer() {
    }

    public static String consume() {
        EnterAffectiveConfigProxy config = new EnterAffectiveConfigProxy();
        return config.getUserId();
    }
}
