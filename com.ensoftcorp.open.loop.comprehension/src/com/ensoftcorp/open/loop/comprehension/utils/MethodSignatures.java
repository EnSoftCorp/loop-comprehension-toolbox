package com.ensoftcorp.open.loop.comprehension.utils;

import java.util.Arrays;
import java.util.List;

/**
 * List of String method signatures, to be used with MethodSignatureMatcher
 * 
 * @author gsanthan
 *
 */
public interface MethodSignatures {
	
	/**
	 * Master list of string constants representing method signatures to be detected for termination pattern discovery
	 */
	
	// java.util.Iterator
	public static final String SIGNATURE_ITERATOR_CONSTRUCTION = "java.util.Iterator iterator()";
	public static final String SIGNATURE_LISTITERATOR_CONSTRUCTION = "java.util.ListIterator listIterator()";
	public static final String SIGNATURE_LISTITERATOR_PARAMETERIZED_CONSTRUCTION = "java.util.ListIterator listIterator(int)";
	
	public static final String SIGNATURE_ITERATOR_HAS_NEXT = "boolean hasNext()";
	public static final String SIGNATURE_ITERATOR_NEXT = "java.lang.Object next()";
	public static final String SIGNATURE_ITERATOR_REMOVE = "void remove()";
	
	// java.util.Enumeration
	public static final String SIGNATURE_ENUMERATION_NEXTELEMENT = "java.lang.Object nextElement()";
	public static final String SIGNATURE_ENUMERATION_HASMORELEMENTS = "boolean hasMoreElements()";
	
	// java.lang.String	
	public static final String SIGNATURE_STRING_CONCAT = "java.lang.String concat(java.lang.String)";
	
	// java.util.StringTokenizer
	public static final String SIGNATURE_STRING_NEXT_TOKEN = "java.lang.String nextToken()";
	public static final String SIGNATURE_HAS_NEXT_TOKEN = "boolean hasMoreTokens()";
	
	// IO
	public static final String SIGNATURE_READLINE = "java.lang.String readLine()";
	
	// java.util.List
	public static final String SIGNATURE_LIST_ADD = "boolean add(java.lang.Object)";
	public static final String SIGNATURE_LIST_ADDALL = "boolean addAll(java.util.Collection)";
	public static final String SIGNATURE_LIST_SET = "java.lang.Object set(int, java.lang.Object)";
	public static final String SIGNATURE_LIST_REMOVE = "java.lang.Object remove(int)";
	public static final String SIGNATURE_LIST_REMOVEALL = "boolean removeAll(java.util.Collection)";
	public static final String SIGNATURE_LIST_CLEAR = "void clear()";
	public static final String LIST_RETAINALL = "boolean retainAll(java.util.Collection)";
	
	// java.util.Stack
	public static final String SIGNATURE_STACK_PUSH = "java.lang.Object push(java.lang.Object)";
	public static final String SIGNATURE_STACK_POP = "java.lang.Object pop()";
	public static final String SIGNATURE_STACK_PEEK = "java.lang.Object peek()";

	// java.util.Map 
	public static final String MAP_CLEAR = "void clear()";
	public static final String MAP_PUTALL = "void putAll(java.util.Map)"; 
	public static final String MAP_PUT = "java.lang.Object put(java.lang.Object, java.lang.Object)"; 
	public static final String MAP_REMOVE = "java.lang.Object remove(java.lang.Object)";

	// java.util.Set
	public static final String SET_ADDALL = "boolean addAll(java.util.Collection)";
	public static final String SET_REMOVEALL = "boolean removeAll(java.util.Collection)";
	public static final String SET_ADD = "boolean add(java.lang.Object)";
	public static final String SET_REMOVE = "boolean remove(java.lang.Object)";
	public static final String SET_CLEAR = "void clear()";
	public static final String SET_RETAINALL = "boolean retainAll(java.util.Collection)";
	
	// java.util.Vector
	public static final String VECTOR_REMOVE = "java.lang.Object remove(int)";
	public static final String VECTOR_ADDALL = "boolean addAll(int, java.util.Collection)";
	public static final String VECTOR_REMOVEALL = "boolean removeAll(java.util.Collection)";
	public static final String VECTOR_CLEAR = "void clear()";
	public static final String VECTOR_ADDELEMENT = "void addElement(java.lang.Object)";
	public static final String VECTOR_REMOVEALLELEMENTS = "void removeAllElements()";
	public static final String VECTOR_REMOVEELEMENT = "boolean removeElement(java.lang.Object)";
	public static final String VECTOR_INSERTELEMENTAT = "void insertElementAt(java.lang.Object, int)"; 
	public static final String VECTOR_REMOVERANGE = "void removeRange(int, int)";
	public static final String VECTOR_ADD = "boolean add(java.lang.Object)";
	public static final String VECTOR_REMOVEELEMENTAT = "void removeElementAt(int)"; 
	public static final String VECTOR_GROW = "void grow(int)";
	public static final String VECTOR_ADD2 = "void add(int, java.lang.Object)";
	public static final String VECTOR_REMOVE2 = "boolean remove(java.lang.Object)";
	public static final String VECTOR_RETAINALL = "boolean retainAll(java.util.Collection)"; 
	public static final String VECTOR_ADDALL2 = "boolean addAll(java.util.Collection)";
	
	// java.util.Collection
	public static final String COLLECTION_REMOVEALL = "boolean removeAll(java.util.Collection)";
	public static final String COLLECTION_ADDALL = 	"boolean addAll(java.util.Collection)";
	public static final String COLLECTION_ADD = "boolean add(java.lang.Object)";
	public static final String COLLECTION_REMOVE = "boolean remove(java.lang.Object), void clear()";
	public static final String COLLECTION_RETAINALL = "boolean retainAll(java.util.Collection)";
	
	// java.util.Queue
	public static final String QUEUE_ADD = "boolean add(java.lang.Object)";
	public static final String QUEUE_OFFER = "boolean offer(java.lang.Object)";
	public static final String QUEUE_POLL = "java.lang.Object poll()";
	public static final String QUEUE_REMOVE = "java.lang.Object remove()";

	// java.util.Deque
	public static final String DEQUE_ADDFIRST = "void addFirst(java.lang.Object)";
	public static final String DEQUE_OFFERFIRST = "boolean offerFirst(java.lang.Object)";
	public static final String DEQUE_REMOVEFIRST = "java.lang.Object removeFirst()"; 
	public static final String DEQUE_REMOVELAST = "java.lang.Object removeLast()";
	public static final String DEQUE_REMOVELASTOCCURENCE = "boolean removeLastOccurrence(java.lang.Object)"; 
	public static final String DEQUE_POP = "java.lang.Object pop()";
	public static final String DEQUE_OFFERLAST = "boolean offerLast(java.lang.Object)";
	public static final String DEQUE_POLLFIRST = "java.lang.Object pollFirst()";
	public static final String DEQUE_OFFER = "boolean offer(java.lang.Object)";
	public static final String DEQUE_REMOVE = "boolean remove(java.lang.Object)";
	public static final String DEQUE_PUSH = "void push(java.lang.Object)";
	public static final String DEQUE_POLLLAST = "java.lang.Object pollLast()";
	public static final String DEQUE_POLL = "java.lang.Object poll()";
	public static final String DEQUE_ADD = "boolean add(java.lang.Object)";
	public static final String DEQUE_ADDLAST = "void addLast(java.lang.Object)";
	public static final String DEQUE_REMOVE2 = "java.lang.Object remove()";

	// java.util.WeakHashMap
	public static final String WEAKHASHMAP_REMOVE = "java.lang.Object remove(java.lang.Object)";
	public static final String WEAKHASHMAP_PUT = "java.lang.Object put(java.lang.Object, java.lang.Object)";
	public static final String WEAKHASHMAP_CLEAR = "void clear()";
	public static final String WEAKHASHMAP_PUTALL = "void putAll(java.util.Map)";
	
	
	// java.util.HashTable
	public static final String HASHTABLE_PUTALL = "void putAll(java.util.Map)";
	public static final String HASHTABLE_REMOVE = "java.lang.Object remove(java.lang.Object)";
	public static final String HASHTABLE_PUT = "java.lang.Object put(java.lang.Object, java.lang.Object)";
	public static final String HASHTABLE_CLEAR = "void clear()";
	
	
	//java.io READ
	public static final String IO_CANREAD = "boolean canRead()";
	public static final String IO_READBOOLEAN = "boolean readBoolean()";
	public static final String IO_READBYTE = "byte readByte()";
	public static final String IO_READCHAR = "char readChar()";
	public static final String IO_READLINE1 = "char[] readline(boolean)";
	public static final String IO_READPASSWORD1 = "char[] readPassword()";
	public static final String IO_READPASSWORD = "char[] readPassword(java.lang.String, java.lang.Object[])";
	public static final String IO_READDOUBLE = "double readDouble()";
	public static final String IO_READFLOAT = "float readFloat()";
	public static final String IO_READ8 = "int read()";
	public static final String IO_READ2 = "int read(byte[])";
	public static final String IO_READ3 = "int read(byte[], int, int)";
	public static final String IO_READ4 = "int read(byte[], int, int, boolean)";
	public static final String IO_READ5 = "int read(char[])";
	public static final String IO_READ6 = "int read(char[], int, int)";
	public static final String IO_READ7 = "int read(java.nio.CharBuffer)";
	public static final String IO_READ0 = "int read0()";
	public static final String IO_READ11 = "int read1(byte[], int, int)";
	public static final String IO_READ1 = "int read1(char[], int, int)";
	public static final String IO_READBLOCKHEADER = "int readBlockHeader(boolean)";
	public static final String IO_READBYTES = "int readBytes(byte[], int, int)";
	public static final String IO_READBYTES0 = "int readBytes0(byte[], int, int)";
	public static final String IO_READINT = "int readInt()";
	public static final String IO_READUNSIGNEDBYTE = "int readUnsignedByte()";
	public static final String IO_READUNSIGNEDSHORT = "int readUnsignedShort()";
	public static final String IO_READUTFCHAR = "int readUTFChar(java.lang.StringBuilder, long)";
	public static final String IO_READCLASS = "java.lang.Class readClass(boolean)";
	public static final String IO_READENUM = "java.lang.Enum readEnum(boolean)";
	public static final String IO_READARRAY = "java.lang.Object readArray(boolean)";
	public static final String IO_READHANDLE = "java.lang.Object readHandle(boolean)";
	public static final String IO_READNULL = "java.lang.Object readNull()";
	public static final String IO_READOBJECT1 = "java.lang.Object readObject()";
	public static final String IO_READOBJECT0 = "java.lang.Object readObject0(boolean)";
	public static final String IO_READOBJECTOVERRIDE = "java.lang.Object readObjectOverride()";
	public static final String IO_READORDINARYOBJECT = "java.lang.Object readOrdinaryObject(boolean)";
	public static final String IO_READUNSHARED = "java.lang.Object readUnshared()";
	public static final String IO_READLINE4 = "java.lang.String readLine()";
	public static final String IO_READLINE2 = "java.lang.String readLine(boolean)";
	public static final String IO_READLINE3 = "java.lang.String readLine(java.lang.String, java.lang.Object[])";
	public static final String IO_READLONGUTF = "java.lang.String readLongUTF()";
	public static final String IO_READSTRING = "java.lang.String readString(boolean)";
	public static final String IO_READTYPESTRING = "java.lang.String readTypeString()";
	public static final String IO_READUTF1 = "java.lang.String readUTF()";
	public static final String IO_READUTF = "java.lang.String readUTF(java.io.DataInput)";
	public static final String IO_READUTFBODY = "java.lang.String readUTFBody(long)";
	public static final String IO_READLONG = "long readLong()";
	public static final String IO_READUTFSPAN = "long readUTFSpan(java.lang.StringBuilder, long)";
	public static final String IO_READSHORT = "short readShort()";
	public static final String IO_READBOOLEANS = "void readBooleans(boolean[], int, int)";
	public static final String IO_READCHARS = "void readChars(char[], int, int)";
	public static final String IO_READDOUBLES = "void readDoubles(double[], int, int)";
	public static final String IO_READEXTERNALDATA = "void readExternalData(java.io.Externalizable, java.io.ObjectStreamClass)";
	public static final String IO_READFIELDS = "void readFields()";
	public static final String IO_READFLOATS = "void readFloats(float[], int, int)";
	public static final String IO_READFULLY1 = "void readFully(byte[])";
	public static final String IO_READFULLY2 = "void readFully(byte[], int, int)";
	public static final String IO_READFULLY = "void readFully(byte[], int, int, boolean)";
	public static final String IO_READINTS = "void readInts(int[], int, int)";
	public static final String IO_READLONGS = "void readLongs(long[], int, int)";
	public static final String IO_READOBJECT = "void readObject(java.io.ObjectInputStream)";
	public static final String IO_READSERIALDATA = "void readSerialData(java.lang.Object, java.io.ObjectStreamClass)";
	public static final String IO_READSHORTS = "void readShorts(short[], int, int)";
	public static final String IO_READSTREAMHEADER = "void readStreamHeader()";
	
	// Ignore the following APIs for now - revisit later 
	public static final String IO_UNREAD1 = "void unread(byte[])";
	public static final String IO_UNREAD2 = "void unread(byte[], int, int)";
	public static final String IO_UNREAD3 = "void unread(char[])";
	public static final String IO_UNREAD4 = "void unread(char[], int, int)";
	public static final String IO_UNREAD = "void unread(int)";

	
	
	/**
	 * Procedure to add new Patterns and corresponding API signature:
	 * 1. Add all APIs in the pattern to master list of signatures (above)
	 * 2. Add each API to INCREMENT_APIs, DECREMENT_APIs, or IO_APIs as applicable
	 * 3. If it is iterator pattern, add entry to the map in IteratorPatternSignatures and to the enum
	 */
	
	public static final List<String> ITERATOR_APIs = Arrays.asList(new String[] {
			SIGNATURE_ENUMERATION_HASMORELEMENTS,
			SIGNATURE_ENUMERATION_NEXTELEMENT,
			SIGNATURE_ITERATOR_HAS_NEXT,
			SIGNATURE_ITERATOR_NEXT,
			SIGNATURE_ITERATOR_REMOVE
	});
	
	public static final List<String> COLLECTION_APIs = Arrays.asList(new String[]{
			SIGNATURE_LIST_ADD,
			SIGNATURE_LIST_ADDALL,
			SIGNATURE_LIST_SET,
			SIGNATURE_LIST_REMOVE,
			SIGNATURE_LIST_REMOVEALL,
			SIGNATURE_LIST_CLEAR,
			LIST_RETAINALL,
			SIGNATURE_STACK_PUSH,
			SIGNATURE_STACK_POP,
			SIGNATURE_STACK_PEEK,
			MAP_CLEAR,
			MAP_PUTALL,
			MAP_PUT,
			MAP_REMOVE,
			SET_ADDALL,
			SET_REMOVEALL,
			SET_ADD,
			SET_REMOVE,
			SET_CLEAR,
			SET_RETAINALL,
			VECTOR_REMOVE,
			VECTOR_ADDALL,
			VECTOR_REMOVEALL,
			VECTOR_CLEAR,
			VECTOR_ADDELEMENT,
			VECTOR_REMOVEALLELEMENTS,
			VECTOR_REMOVEELEMENT,
			VECTOR_INSERTELEMENTAT,
			VECTOR_REMOVERANGE,
			VECTOR_ADD,
			VECTOR_REMOVEELEMENTAT,
			VECTOR_GROW,
			VECTOR_ADD2,
			VECTOR_REMOVE2,
			VECTOR_RETAINALL,
			VECTOR_ADDALL2,
			COLLECTION_REMOVEALL,
			COLLECTION_ADDALL,
			COLLECTION_ADD,
			COLLECTION_REMOVE,
			COLLECTION_RETAINALL,
			QUEUE_ADD,
			QUEUE_OFFER,
			QUEUE_POLL,
			QUEUE_REMOVE,
			DEQUE_ADDFIRST,
			DEQUE_OFFERFIRST,
			DEQUE_REMOVEFIRST,
			DEQUE_REMOVELAST,
			DEQUE_REMOVELASTOCCURENCE,
			DEQUE_POP,
			DEQUE_OFFERLAST,
			DEQUE_POLLFIRST,
			DEQUE_OFFER,
			DEQUE_REMOVE,
			DEQUE_PUSH,
			DEQUE_POLLLAST,
			DEQUE_POLL,
			DEQUE_ADD,
			DEQUE_ADDLAST,
			DEQUE_REMOVE2,
			WEAKHASHMAP_REMOVE,
			WEAKHASHMAP_PUT,
			WEAKHASHMAP_CLEAR,
			WEAKHASHMAP_PUTALL,
			HASHTABLE_PUTALL,
			HASHTABLE_REMOVE,
			HASHTABLE_PUT,
			HASHTABLE_CLEAR	
	});
	
	/**
	 * Add APIs that represent "increment" behavior for monotonicity analysis
	 */
	public static final List<String> INCREMENT_APIs = Arrays.asList(new String[]{
			SIGNATURE_ITERATOR_NEXT,
			SIGNATURE_ENUMERATION_NEXTELEMENT,
			SIGNATURE_STRING_NEXT_TOKEN,
			SIGNATURE_LIST_ADD,
			SIGNATURE_LIST_ADDALL,
			SIGNATURE_STACK_PUSH,
			MAP_PUTALL,
			MAP_PUT,
			SET_ADDALL,
			SET_ADD,
			VECTOR_ADDALL,
			VECTOR_ADDELEMENT,
			VECTOR_INSERTELEMENTAT,
			VECTOR_ADD,
			VECTOR_GROW,
			VECTOR_ADD2,
			VECTOR_ADDALL2,
			COLLECTION_ADDALL,
			COLLECTION_ADD,
			QUEUE_ADD,
			QUEUE_OFFER,
			DEQUE_ADDFIRST,
			DEQUE_OFFERFIRST,
			DEQUE_OFFERLAST,
			DEQUE_OFFER,
			DEQUE_PUSH,
			DEQUE_ADD,
			DEQUE_ADDLAST,
			WEAKHASHMAP_PUT,
			WEAKHASHMAP_PUTALL,
			HASHTABLE_PUTALL,
			HASHTABLE_PUT
	});
	
	
	/**
	 * Add APIs that represent "decrement" behavior for monotonicity analysis
	 */
	public static final List<String> DECREMENT_APIs = Arrays.asList(new String[]{
			SIGNATURE_ITERATOR_REMOVE,
			SIGNATURE_LIST_REMOVE,
			SIGNATURE_LIST_REMOVEALL,
			SIGNATURE_LIST_CLEAR,
			SIGNATURE_STACK_POP,
			MAP_CLEAR,
			MAP_REMOVE,
			SET_REMOVEALL,
			SET_REMOVE,
			SET_CLEAR,
			SET_RETAINALL,
			VECTOR_REMOVE,
			VECTOR_REMOVEALL,
			VECTOR_CLEAR,
			VECTOR_REMOVEALLELEMENTS,
			VECTOR_REMOVEELEMENT,
			VECTOR_REMOVERANGE,
			VECTOR_REMOVEELEMENTAT,
			VECTOR_REMOVE2,
			VECTOR_RETAINALL,
			COLLECTION_REMOVEALL,
			COLLECTION_REMOVE,
			COLLECTION_RETAINALL,
			QUEUE_POLL,
			QUEUE_REMOVE,
			DEQUE_REMOVEFIRST,
			DEQUE_REMOVELAST,
			DEQUE_REMOVELASTOCCURENCE,
			DEQUE_POP,
			DEQUE_POLLFIRST,
			DEQUE_REMOVE,
			DEQUE_POLLLAST,
			DEQUE_POLL,
			DEQUE_REMOVE2,
			WEAKHASHMAP_REMOVE,
			WEAKHASHMAP_CLEAR,
			HASHTABLE_REMOVE,
			HASHTABLE_CLEAR
	});
	
	/**
	 * Add APIs that represent "IO" behavior for monotonicity analysis
	 */
	public static final List<String> IO_APIs = Arrays.asList(new String[]{
			SIGNATURE_READLINE,
			 IO_CANREAD,
			 IO_READBOOLEAN, 
			 IO_READBYTE,
			 IO_READCHAR, 
			 IO_READLINE1, 
			 IO_READPASSWORD1, 
			 IO_READPASSWORD,
			 IO_READDOUBLE,
			 IO_READFLOAT, 
			 IO_READ8, 
			 IO_READ2, 
			 IO_READ3, 
			 IO_READ4, 
			 IO_READ5, 
			 IO_READ6, 
			 IO_READ7, 
			 IO_READ0, 
			 IO_READ11, 
			 IO_READ1, 
			 IO_READBLOCKHEADER, 
			 IO_READBYTES, 
			 IO_READBYTES0, 
			 IO_READINT, 
			 IO_READUNSIGNEDBYTE, 
			 IO_READUNSIGNEDSHORT, 
			 IO_READUTFCHAR, 
			 IO_READCLASS, 
			 IO_READENUM, 
			 IO_READARRAY, 
			 IO_READHANDLE, 
			 IO_READNULL, 
			 IO_READOBJECT1, 
			 IO_READOBJECT0, 
			 IO_READOBJECTOVERRIDE, 
			 IO_READORDINARYOBJECT, 
			 IO_READUNSHARED, 
			 IO_READLINE4, 
			 IO_READLINE2, 
			 IO_READLINE3, 
			 IO_READLONGUTF, 
			 IO_READSTRING, 
			 IO_READTYPESTRING, 
			 IO_READUTF1, 
			 IO_READUTF, 
			 IO_READUTFBODY, 
			 IO_READLONG,
			 IO_READUTFSPAN, 
			 IO_READSHORT, 
			 IO_READBOOLEANS, 
			 IO_READCHARS, 
			 IO_READDOUBLES, 
			 IO_READEXTERNALDATA, 
			 IO_READFIELDS, 
			 IO_READFLOATS, 
			 IO_READFULLY1, 
			 IO_READFULLY2, 
			 IO_READFULLY, 
			 IO_READINTS, 
			 IO_READLONGS, 
			 IO_READOBJECT, 
			 IO_READSERIALDATA, 
			 IO_READSHORTS, 
			 IO_READSTREAMHEADER
	});
}