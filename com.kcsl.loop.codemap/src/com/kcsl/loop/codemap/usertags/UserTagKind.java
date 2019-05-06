package com.kcsl.loop.codemap.usertags;

public enum UserTagKind {
	
	LOOP("LOOP-"),
	BRANCH("BRANCH-"),
	CALLSITE("CALLSITE-"),
	TYPE("TYPE-"),
	METHOD("METHOD-"),
	REFERENCE("REFERENCE-"),
	LAMBDA_LOOP("LAMBDA-LOOP-");
	
	private String tagPrefix;
	
	UserTagKind(String tagPrefix) {
		this.tagPrefix = tagPrefix;
	}
	
	public final String getTagPrefix() {
		return tagPrefix;
	}
}
