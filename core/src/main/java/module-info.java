module tech.kwik.core {
    requires tech.kwik.agent15;
    requires at.favre.lib.hkdf;
    requires io.whitfin.siphash;
    requires java.management;

    exports tech.kwik.core;
    exports tech.kwik.core.concurrent;
    exports tech.kwik.core.generic;
    exports tech.kwik.core.server;
    exports tech.kwik.core.log;

    // giữ cho qlog
    exports tech.kwik.core.common to tech.kwik.qlog;
    exports tech.kwik.core.frame to tech.kwik.qlog;
    exports tech.kwik.core.packet to tech.kwik.qlog;

    // 👇 mở công khai để app cũng dùng được
    exports tech.kwik.core.util;
    exports tech.kwik.core.cid;
    exports tech.kwik.core.impl;
    exports tech.kwik.core.receive;
}
