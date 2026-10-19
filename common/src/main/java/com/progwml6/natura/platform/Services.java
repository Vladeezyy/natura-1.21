package com.progwml6.natura.platform;

import java.util.ServiceLoader;

/** The loader's {@link Platform}, from META-INF/services of the neoforge / fabric project (MultiLoader template). */
public final class Services {
    public static final Platform PLATFORM = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).findFirst()
            .orElseThrow(() -> new IllegalStateException("No Natura platform implementation found"));

    private Services() {}
}
