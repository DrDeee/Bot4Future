package re.fffutu.bot4future.logging;

import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

import java.io.File;

public class TriggeringPolicy extends SizeBasedTriggeringPolicy {
    private static boolean isStartUp = true;

    @Override
    public boolean isTriggeringEvent(File file, Object o) {
        if (isStartUp) {
            isStartUp = false;
            return true;
        }
        return super.isTriggeringEvent(file, o);
    }
}
