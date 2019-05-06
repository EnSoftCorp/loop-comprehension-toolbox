package com.kcsl.loop.catalog.monotonicity;

/**
 * @author Payas Awadhutkar
 */

public class MonotonicityPatternConstants {
	

	// Induction variable is a variable that influences a loop's termination condition
	
	// Relevent updates include either increments
	
	public static final String INCREMENT_OPERATOR = "INCREMENT_OPERATOR";
	
	// Or decrements
	
	public static final String DECREMENT_OPERATOR = "DECREMENT_OPERATOR";
	
	/* A monotonic loop is one in which 
	 * all induction variable updates go in one direction, 
	 * either all are increments or all decrements.
	 */
	
	public static final String MONOTONIC_LOOP = "MONOTONIC_LOOP";

	
	/*
	 * Pattern 1.1
	 * Induction variable is of type PRIMITIVE
	 * Induction variable is declared and defined locally  
	 */
	public static final String MONOTONICITY_PATTERN_LOCAL_PRIMITIVE = "MONOTONICITY_PATTERN_LOCAL_PRIMITIVE";
	
	/*
	 * Pattern 1.2
	 * Induction variable is of type PRIMITIVE
	 * Induction variable is result of a CALLSITE  
	 */
	
	public static final String MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE = "MONOTONICITY_PATTERN_CALLSITE_PRIMITIVE";
	
	/*
	 * Pattern 1.3
	 * Induction variable is of type PRIMITIVE
	 * Induction variable is a PARAMETER  
	 */
	
	public static final String MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE = "MONOTONICITY_PATTERN_PARAMETER_PRIMITIVE";
	
	/*
	 * Pattern 1.4
	 * Induction variable is of type PRIMITIVE
	 * Induction variable is a FIELD  
	 */
	
	public static final String MONOTONICITY_PATTERN_FIELD_PRIMITIVE = "MONOTONICITY_PATTERN_FIELD_PRIMITIVE";
	
	/*
	 * Pattern 2.1
	 * Induction variable is of type ARRAY
	 * Induction variable is declared and defined locally  
	 */
	
	public static final String MONOTONICITY_PATTERN_LOCAL_ARRAY = "MONOTONICITY_PATTERN_LOCAL_ARRAY";
	
	/*
	 * Pattern 2.2
	 * Induction variable is of type ARRAY
	 * Induction variable is result of a CALLSITE 
	 */
	
	public static final String MONOTONICITY_PATTERN_CALLSITE_ARRAY = "MONOTONICITY_PATTERN_CALLSITE_ARRAY";
	
	/*
	 * Pattern 2.3
	 * Induction variable is of type ARRAY
	 * Induction variable is a PARAMETER
	 */
	
	public static final String MONOTONICITY_PATTERN_PARAMETER_ARRAY = "MONOTONICITY_PATTERN_PARAMETER_ARRAY";
	
	/*
	 * Pattern 2.4
	 * Induction variable is of type ARRAY
	 * Induction variable is a FIELD 
	 */
	
	public static final String MONOTONICITY_PATTERN_FIELD_ARRAY = "MONOTONICITY_PATTERN_FIELD_ARRAY";
	
	/*
	 * Pattern 3.1
	 * Induction variable is of type COLLECTION and is iterated through a CURSOR
	 * Induction variable is declared and defined locally 
	 */
	
	public static final String MONOTONICITY_PATTERN_LOCAL_COLLECTION = "MONOTONICITY_PATTERN_LOCAL_COLLECTION";
	
	/*
	 * Pattern 3.2
	 * Induction variable is of type COLLECTION and is iterated through a CURSOR
	 * Induction variable is result of a CALLSITE 
	 */
	
	public static final String MONOTONICITY_PATTERN_CALLSITE_COLLECTION = "MONOTONICITY_PATTERN_CALLSITE_COLLECTION";
	
	/*
	 * Pattern 3.3
	 * Induction variable is of type COLLECTION and is iterated through a CURSOR
	 * Induction variable is a PARAMETER 
	 */
	
	public static final String MONOTONICITY_PATTERN_PARAMETER_COLLECTION = "MONOTONICITY_PATTERN_PARAMETER_COLLECTION";
	
	/*
	 * Pattern 3.4
	 * Induction variable is of type COLLECTION and is iterated through a CURSOR
	 * Induction variable is a FIELD 
	 */
	
	public static final String MONOTONICITY_PATTERN_FIELD_COLLECTION = "MONOTONICITY_PATTERN_FIELD_COLLECTION";
	
	/*
	 * IO Pattern 1
	 * Induction variable is result of an IO CALLSITE.
	 * The CALLSITE is invoked by a LOCAL variable
	 */
	
	public static final String MONOTONICITY_PATTERN_IO_LOCAL = "MONOTONICITY_PATTERN_IO_LOCAL";
	
	/*
	 * IO Pattern 2
	 * Induction variable is result of an IO CALLSITE.
	 * The CALLSITE is invoked by a PARAMETER
	 */
	
	public static final String MONOTONICITY_PATTERN_IO_PARAMETER = "MONOTONICITY_PATTERN_IO_PARAMETER";
	
	/*
	 * IO Pattern 3
	 * Induction variable is result of an IO CALLSITE.
	 * The CALLSITE is invoked by a FIELD
	 */
	
	public static final String MONOTONICITY_PATTERN_IO_FIELD = "MONOTONICITY_PATTERN_IO_FIELD";
	
	/*
	 * API Pattern 1
	 * Induction variable is result of an CALLSITE invoking an API other than IO.
	 * The CALLSITE is invoked by a LOCAL variable
	 */
	
	public static final String MONOTONICITY_PATTERN_API_LOCAL = "MONOTONICITY_PATTERN_API_LOCAL";
	
	/*
	 * API Pattern 2
	 * Induction variable is result of an CALLSITE invoking an API other than IO.
	 * The CALLSITE is invoked by a PARAMETER
	 */
	
	public static final String MONOTONICITY_PATTERN_API_PARAMETER = "MONOTONICITY_PATTERN_API_PARAMETER";
	
	/*
	 * API Pattern 3
	 * Induction variable is result of an CALLSITE invoking an API other than IO.
	 * The CALLSITE is invoked by a FIELD
	 */
	
	public static final String MONOTONICITY_PATTERN_API_FIELD = "MONOTONICITY_PATTERN_API_FIELD";
	
	/*
	 * CALLSITE Pattern
	 * Induction Variable is result of an CALLSITE (Any API)
	 * Other induction variable is unknown (no known comparator found)
	 */
	
	public static final String MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR = "MONOTONICITY_PATTERN_CALLSITE_UNKNOWN_COMPARATOR";
}
