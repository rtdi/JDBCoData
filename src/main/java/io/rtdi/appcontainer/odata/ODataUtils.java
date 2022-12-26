package io.rtdi.appcontainer.odata;

public class ODataUtils {
	public static final String JDBCTYPE = "@jdbc.type";
	public static final String JDBCOBJECTNAME = "@jdbc.objectname";
	public static final String JDBCSCHEMANAME = "@jdbc.schemaname";
	public static final String JDBC_COMMENT = "@jdbc.comment";
	public static final String ODATACONTEXT = "@odata.context";
	public static final String CONTAINER = "CONTAINER";
	public static final String KIND = "$kind";
	public static final String VERSION = "$Version";
	public static final String METADATA = "$metadata";
	public static final String VALUE = "value";
	public static final String KEY = "$Key";
	public static final String SCALE = "$Scale";
	public static final String PRECISION = "$Precision";
	public static final String NULLABLE = "$Nullable";
	public static final String MAXLENGTH = "$MaxLength";
	public static final String TYPE = "$Type";
	public static final String JDBCDATATYPE = "@jdbc.datatype";
	public static final String VERSIONVALUE = "4.0";
	public static final String NEXTLINK = "@odata.nextLink";
	public static final String SOURCEDATATYPE = "@jdbc.datatype.source";
	public static final String JDBCLENGTH = "@jdbc.length";

	public static String encodeName(String name) {
		return name;
	}
	
	public static String decodeName(String text) {
		return text;
	}

}
