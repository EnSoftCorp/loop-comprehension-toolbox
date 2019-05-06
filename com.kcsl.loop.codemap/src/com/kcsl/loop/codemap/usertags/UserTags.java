package com.kcsl.loop.codemap.usertags;

import java.util.Set;

public class UserTags {

	private static Set<String> loopTags;
	private static Set<String> branchTags;
	private static Set<String> callsiteTags;
	private static Set<String> methodTags;
	private static Set<String> typeTags;
	
	public static Set<String> getLoopTags() {
		return loopTags;
	}
	public static void setLoopTags(Set<String> loopTags) {
		UserTags.loopTags = loopTags;
	}
	public static Set<String> getBranchTags() {
		return branchTags;
	}
	public static void setBranchTags(Set<String> branchTags) {
		UserTags.branchTags = branchTags;
	}
	public static Set<String> getCallsiteTags() {
		return callsiteTags;
	}
	public static void setCallsiteTags(Set<String> callsiteTags) {
		UserTags.callsiteTags = callsiteTags;
	}
	public static Set<String> getMethodTags() {
		return methodTags;
	}
	public static void setMethodTags(Set<String> methodTags) {
		UserTags.methodTags = methodTags;
	}
	public static Set<String> getTypeTags() {
		return typeTags;
	}
	public static void setTypeTags(Set<String> typeTags) {
		UserTags.typeTags = typeTags;
	}

	public static void addLoopTag(String loopTag) {
		loopTags.add(UserTagKind.LOOP.getTagPrefix()+loopTag);
	}
	public static void addBranchTag(String branchTag) {
		branchTags.add(UserTagKind.BRANCH.getTagPrefix()+branchTag);
	}
	public static void addCallsiteTag(String callsiteTag) {
		callsiteTags.add(UserTagKind.CALLSITE.getTagPrefix()+callsiteTag);
	}
	public static void addMethodTag(String methodTag) {
		methodTags.add(UserTagKind.METHOD.getTagPrefix()+methodTag);
	}
	public static void addTypeTag(String typeTag) {
		typeTags.add(UserTagKind.TYPE.getTagPrefix()+typeTag);
	}
	
}
