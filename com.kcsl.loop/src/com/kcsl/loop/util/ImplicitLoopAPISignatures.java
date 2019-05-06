package com.kcsl.loop.util;

import java.util.Arrays;
import java.util.List;

public class ImplicitLoopAPISignatures {

	/*
	 * Following APIs are the APIs that has a looping logic in them.
	 * They will be referred as Implicit Loop APIs.
	 * Some of these APIs have been defined in MethodSignatures such as map.putAll().
	 * These are rest of the APIs, mainly the stream APIs.
	 */
	
	public static final String STREAM_FILTER = "java.util.stream.Stream filter(java.util.function.Predicate)";
	public static final String MAP_TO_LONG = "java.util.stream.LongStream mapToLong(java.util.function.ToLongFunction)";
	public static final String OBJECT_REDUCE = "java.lang.Object reduce(java.lang.Object, java.util.function.BinaryOperator)";
	public static final String OBJECT_COLLECT = "java.lang.Object reduce(java.lang.Object, java.util.function.BinaryOperator)";
	public static final String STREAM_MAP = "java.util.stream.Stream map(java.util.function.Function)";
	public static final String VOID_FOREACH = "void forEach(java.util.function.Consumer)";
	public static final String VOID_SORT = "void sort(java.util.Comparator)";
	public static final String LONG_SUM = "long sum()";
	
	/*
	 * All signatures in a single List
	 */
	
	public static final List<String> IMPLICIT_LOOP_APIS = Arrays.asList(new String[] {
		STREAM_FILTER,
		MAP_TO_LONG,
		OBJECT_REDUCE,
		OBJECT_COLLECT,
		STREAM_MAP,
		VOID_FOREACH,
		VOID_SORT,
		LONG_SUM
	});
	
}
