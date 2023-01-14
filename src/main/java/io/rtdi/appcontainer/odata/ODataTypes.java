package io.rtdi.appcontainer.odata;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.rtdi.appcontainer.odata.entity.definitions.EntityTypeProperty;
import jakarta.xml.bind.DatatypeConverter;

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

	/**
	 * @return the jdbc value as OData value
	 */
	public String getText() {
		return text;
	}

	/**
	 * Convert the value retrieved via rs.getObject() into the correct OData object value
	 * 
	 * @param jdbcobject The value returned by rs.getObject()
	 * @param jdbcType The detailed JDBC type, e.g. the object might be a string but the requested type a geometry
	 * @param typename For e.g. geometry, the type itself is not enough
	 * @return A string OData expects as payload for this data type
	 */
	public static Object convert(Object jdbcobject, JDBCType jdbcType, String typename) {
		/*
		 * see http://docs.oasis-open.org/odata/odata-json-format/v4.0/errata03/os/odata-json-format-v4.0-errata03-os-complete.html#_Toc453766642
		 * see http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792712
		 */
		if (jdbcobject == null) {
			return null;
		} else {
			/* 
			 * Numbers are numbers except for NAN and INF
			 */
			if (jdbcobject instanceof Float) {
				Float value = (Float) jdbcobject;
				if (value.isInfinite() || value.isNaN()) {
					return value.toString();
				} else {
					return value;
				}
			} else if (jdbcobject instanceof Number) {
				return jdbcobject; // Convert to a string as the value might be NaN or infinity
			} else if (jdbcobject instanceof CharSequence) {
				/*
				 * OBJECT, ARRAY, VARIANT are also handled here also as there is no equivalent in OData
				 * Geography and Geometry are selected as GeoJson and therefore fine as well.
				 * Only if the output format is XML, then the geojson must be converted to GML - see issue https://github.com/rtdi/JDBCoData/issues/15
				 */
				return jdbcobject.toString();
			} else if (jdbcobject instanceof byte[]) {
				return DatatypeConverter.printHexBinary((byte[]) jdbcobject);
			} else if (jdbcobject instanceof Boolean) {
				return jdbcobject;
			} else if (jdbcobject instanceof Date) {
				return jdbcobject.toString(); // returns the date in the correct format YYYY-MM-DD
			} else if (jdbcobject instanceof Timestamp) {
				Timestamp ts = (Timestamp) jdbcobject;
				// 2000-01-01T16:00:00.000Z
				ZonedDateTime d = ZonedDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
				return d.toString();
			} else if (jdbcobject instanceof Time) {
				Time t = (Time) jdbcobject;
				LocalTime l = t.toLocalTime();
				return l.toString(); // returns the date in the correct iso format HH24:MI:SS.sss
			} else {
				return jdbcobject.toString();
			}
		}
	}

	/**
	 * Convert an OData parameter like 'key1' to the corresponding JDBC object so stmt.setObject() can be called with it.
	 * 
	 * @param odataparameter the value in the OData world
	 * @param columnmetadata table metadata to read the data types and the such
	 * @return the same value in the JDBC world
	 * @throws ODataException in case of a value conversion error
	 */
	public static Object convertToJDBC(String odataparameter, EntityTypeProperty columnmetadata) throws ODataException {
		try {
			if (odataparameter == null) {
				return null;
			} else {
				switch (columnmetadata.getODataType()) {
				case BINARY:
					return DatatypeConverter.parseHexBinary(odataparameter);
				case BOOLEAN:
					return odataparameter;
				case BYTE:
				case INT16:
				case INT32:
				case SBYTE:
					return Integer.valueOf(odataparameter);
				case DATE:
					return Date.valueOf(odataparameter);
				case DATETIMEOFFSET:
					ZonedDateTime d = ZonedDateTime.parse(odataparameter);
					return Timestamp.from(d.toInstant());
				case DECIMAL:
					return new BigDecimal(odataparameter);
				case DOUBLE:
					return Double.valueOf(odataparameter);
				case DURATION:
					throw new ODataException("The OData data type " + columnmetadata.getODataType().getText() + " is not supported as filter");
				case GUID:
					return odataparameter;
				case Geography:
				case GeographyCollection:
				case GeographyLineString:
				case GeographyMultiLineString:
				case GeographyMultiPoint:
				case GeographyMultiPolygon:
				case GeographyPoint:
				case GeographyPolygon:
				case Geometry:
				case GeometryCollection:
				case GeometryLineString:
				case GeometryMultiLineString:
				case GeometryMultiPoint:
				case GeometryMultiPolygon:
				case GeometryPoint:
				case GeometryPolygon:
					throw new ODataException("The OData data type " + columnmetadata.getODataType().getText() + " is not supported as filter");
				case INT64:
					return Long.valueOf(odataparameter);
				case SINGLE:
					return Float.valueOf(odataparameter);
				case STREAM:
					throw new ODataException("The OData data type " + columnmetadata.getODataType().getText() + " is not supported as filter");
				case STRING:
					return odataparameter;
				case TIMEOFDAY:
					LocalTime l = LocalTime.parse(odataparameter);
					return Time.valueOf(l);
				default:
					throw new ODataException("The OData data type " + columnmetadata.getODataType().getText() + " is not supported as filter");
				}
			}
		} catch (NumberFormatException e) {
			throw new ODataException("Cannot convert the input parameter to the required target data type of that column", e);
		}
	}
}
