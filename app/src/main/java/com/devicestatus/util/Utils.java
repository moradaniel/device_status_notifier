package com.devicestatus.util;

import java.util.Arrays;

public class Utils {
    public static String makeExceptionLogEntry(Exception e, String msg, String context) {
        // Putting the trace on a single line because we pay PER LOG ENTRY and multi-line stack traces would waste money.
        String trace = Arrays.stream(e.getStackTrace()).map(f -> "___" + f.getClassName() + "." + f.getMethodName() + ":" + f.getLineNumber())
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        // String.format() is slow, but exceptions should be rare. Do we care about performance if we're getting exceptions?
        return String.format("%s type=%s message=%s context=%s trace=%s",
                msg,
                e.getClass().getName(),
                chainedString(e).replaceAll("\\n", ""),
                context,
                trace);
    }

    public static String chainedString(Throwable throwable) {
        StringBuilder SB = new StringBuilder(throwable.toString());
        while(throwable != throwable.getCause() && (throwable = throwable.getCause()) != null)
            SB.append(" caused by: ").append(throwable);
        return SB.toString();
    }

    public static String chainedString(String msg, Throwable throwable) {
        StringBuilder SB = new StringBuilder(msg);
        do {
            SB.append(" caused by: ").append(throwable);
        } while(throwable != throwable.getCause() && (throwable = throwable.getCause()) != null);
        return SB.toString();
    }

}
