package com.anonymouschat.anonymouschatserver.web.socket.support;

public final class WsLogTag {
	private WsLogTag() {}
	public static String sys()  { return "[WS][SYS] "; }
	public static String err()  { return "[WS][ERR] "; }
	public static String chat() { return "[WS][CHAT] "; }
	public static String enter(){ return "[WS][ENTER] "; }
	public static String read() { return "[WS][READ] "; }
	public static String leave() { return "[WS][LEAVE] "; }
	public static String pong() { return "[WS][PONG] "; }
	public static String bc()   { return "[WS][BC] "; }
	public static String policy()   { return "[WS][POLICY] "; }
}
