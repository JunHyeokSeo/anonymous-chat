package com.anonymouschat.anonymouschatserver.common.log;

public final class LogTag {
    private LogTag() {}

    // --- General Tags ---
    public static final String AUTH = "[AUTH] ";
    public static final String SECURITY = "[SECURITY] ";
    public static final String USER = "[USER] ";
    public static final String BLOCK = "[BLOCK] ";
    public static final String CHAT = "[CHAT] ";
    public static final String MESSAGE = "[MESSAGE] ";
    public static final String ORCHESTRATOR = "[ORCHESTRATOR] ";
    public static final String IMAGE = "[IMAGE] ";

    // --- WebSocket Specific Tags ---
    public static final String WS_SYS = "[WS][SYS] ";
    public static final String WS_ERR = "[WS][ERR] ";
    public static final String WS_CHAT = "[WS][CHAT] ";
    public static final String WS_ENTER = "[WS][ENTER] ";
    public static final String WS_READ = "[WS][READ] ";
    public static final String WS_LEAVE = "[WS][LEAVE] ";
    public static final String WS_PONG = "[WS][PONG] ";
    public static final String WS_BROADCAST = "[WS][BROADCAST] ";
    public static final String WS_POLICY = "[WS][POLICY] ";
}
