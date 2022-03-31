package io.rtdi.appcontainer.odata;

import io.rtdi.appcontainer.odata.entity.metadata.EntityTypeProperty;

public enum ODataTypes {
	/**
	 * Sequence of UTF-8 characters
	 */
	STRING("Edm.String"),
	/**
	 * Binary data
	 */
	BINARY("Edm.Binary"),
	/**
	 * Binary-valued logic
	 */
	BOOLEAN("Edm.Boolean"),
	/**
	 * Unsigned 8-bit integer
	 */
	BYTE("Edm.Byte"),
	/**
	 * Date without a time-zone offset
	 */
	DATE("Edm.Date"),
	/**
	 * Date and time with a time-zone offset, no leap seconds
	 */
	DATETIMEOFFSET("Edm.DateTimeOffset"),
	/**
	 * Numeric values with fixed precision and scale
	 */
	DECIMAL("Edm.Decimal"),
	/**
	 * IEEE 754 binary64 floating-point number (15-17 decimal digits)
	 */
	DOUBLE("Edm.Double"),
	/**
	 * Signed duration in days, hours, minutes, and (sub)seconds
	 */
	DURATION("Edm.Duration"),
	/**
	 * 16-byte (128-bit) unique identifier
	 */
	GUID("Edm.Guid"),
	/**
	 * Signed 16-bit integer
	 */
	INT16("Edm.Int16"),
	/**
	 * Signed 32-bit integer
	 */
	INT32("Edm.Int32"),
	/**
	 * Signed 64-bit integer
	 */
	INT64("Edm.Int64"),
	/**
	 * Signed 8-bit integer
	 */
	SBYTE("Edm.SByte"),
	/**
	 * IEEE 754 binary32 floating-point number (6-9 decimal digits)
	 */
	SINGLE("Edm.Single"),
	/**
	 * Binary data stream
	 */
	STREAM("Edm.Stream"),
	/**
	 * Clock time 00:00-23:59:59.999999999999
	 */
	TIMEOFDAY("Edm.TimeOfDay"),
	/**
	 * Abstract base type for all Geography types
	 */
	Geography("Edm.Geography"),
	/**
	 * A point in a round-earth coordinate system
	 */
	GeographyPoint("Edm.GeographyPoint"),
	/**
	 * Line string in a round-earth coordinate system
	 */
	GeographyLineString("Edm.GeographyLineString"),
	/**
	 * Polygon in a round-earth coordinate system
	 */
	GeographyPolygon("Edm.GeographyPolygon"),
	/**
	 * Collection of points in a round-earth coordinate system
	 */
	GeographyMultiPoint("Edm.GeographyMultiPoint"),
	/**
	 * Collection of line strings in a round-earth coordinate system
	 */
	GeographyMultiLineString("Edm.GeographyMultiLineString"),
	/**
	 * Collection of polygons in a round-earth coordinate system
	 */
	GeographyMultiPolygon("Edm.GeographyMultiPolygon"),
	/**
	 * Collection of arbitrary Geography values
	 */
	GeographyCollection("Edm.GeographyCollection"),
	/**
	 * Abstract base type for all Geometry types
	 */
	Geometry("Edm.Geometry"),
	/**
	 * Point in a flat-earth coordinate system
	 */
	GeometryPoint("Edm.GeometryPoint"),
	/**
	 * Line string in a flat-earth coordinate system
	 */
	GeometryLineString("Edm.GeometryLineString"),
	/**
	 * Polygon in a flat-earth coordinate system
	 */
	GeometryPolygon("Edm.GeometryPolygon"),
	/**
	 * Collection of points in a flat-earth coordinate system
	 */
	GeometryMultiPoint("Edm.GeometryMultiPoint"),
	/**
	 * Collection of line strings in a flat-earth coordinate system
	 */
	GeometryMultiLineString("Edm.GeometryMultiLineString"),
	/**
	 * Collection of polygons in a flat-earth coordinate system
	 */
	GeometryMultiPolygon("Edm.GeometryMultiPolygon"),
	/**
	 * Collection of arbitrary Geometry values
	 */
	GeometryCollection("Edm.GeometryCollection");

	private String text;

	ODataTypes(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public static Object convert(Object jdbcobject) {
		if (jdbcobject == null) {
			return null;
		} else {
			return jdbcobject.toString();
		}
	}

	/**
	 * Convert an oData parameter like 'key1' to the corresponding JDBC object so stmt.setObject() can be called with it.
	 * 
	 * @param odataparameter
	 * @param columnmetadata 
	 * @return
	 */
	public static Object convertToJDBC(String odataparameter, EntityTypeProperty columnmetadata) {
		return odataparameter;
	}
}
