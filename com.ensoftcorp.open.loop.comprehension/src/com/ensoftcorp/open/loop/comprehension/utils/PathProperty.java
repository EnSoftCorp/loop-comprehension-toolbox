package com.ensoftcorp.open.loop.comprehension.utils;

public enum PathProperty {
	StatementsWithCallSites ("StatementsWithCallSites"),
	StatementsWithJDKCallSites ("StatementsWithJDKCallSites"),
	StatementsWithoutCallSites ("StatementsWithoutCallSites"),
	ContainsCallSites ("ContainsCallSites"),
	ContainsJDKCallSites ("ContainsJDKCallSites"),
	ContainsJDKCallSitesOnly ("ContainsJDKCallSitesOnly"),
	ContainsNoCallSites ("ContainsNoCallSites"),
	ContainsNoJDKCallSites ("ContainsNoJDKCallSites"),
	ContainsNonJDKCallSites ("ContainsNonJDKCallSites");

    private final String name;       

    private PathProperty(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
       return this.name;
    }
}