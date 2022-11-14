package io.rtdi.appcontainer.odata.entity.metadata;

import java.sql.JDBCType;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.rtdi.appcontainer.odata.ODataTypes;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlAttribute;

public class EntityTypeProperty extends ODataBase {
	private String name;
	private ODataTypes odatatype;
	private Integer maxlength;
	private Boolean nullable;
	private Integer precision;
	private Integer scale;

	public final static Integer SALESFORCE_STRING_MAX_LENGTH = 255;

	public EntityTypeProperty() {};

	public EntityTypeProperty(String name, JDBCType type, String typename, Integer length, Integer scale) {
		this.name = name;
		addAnnotation(ODataUtils.SOURCEDATATYPE, typename);
		ODataTypes odatatype;
		Integer maxlength = null;
		if (typename != null && typename.equals("GEOGRAPHY")) {
			/*
			 * Snowflake supports a GEOMETRY data type with the JDBCType VARCHAR
			 */
			odatatype = ODataTypes.Geography;
			maxlength = length;
		} else {
			switch (type) {
			case BIGINT:
				odatatype = ODataTypes.INT64;
				break;
			case BINARY:
			case BLOB:
			case LONGVARBINARY:
			case VARBINARY:
				odatatype = ODataTypes.BINARY;
				maxlength = length;
				break;
			case BIT:
			case BOOLEAN:
				odatatype = ODataTypes.BOOLEAN;
				break;
			case DATE:
				odatatype = ODataTypes.DATE;
				break;
			case DECIMAL:
			case NUMERIC:
				odatatype = ODataTypes.DECIMAL;
				this.precision = length;
				this.scale = scale;
				break;
			case DOUBLE:
				odatatype = ODataTypes.DOUBLE;
				break;
			case FLOAT:
			case REAL:
				odatatype = ODataTypes.SINGLE;
				break;
			case INTEGER:
				odatatype = ODataTypes.INT32;
				break;
			case ROWID:
				odatatype = ODataTypes.STRING;
				break;
			case SMALLINT:
				odatatype = ODataTypes.INT16;
				break;
			case TIME:
			case TIME_WITH_TIMEZONE:
				odatatype = ODataTypes.TIMEOFDAY;
				break;
			case TIMESTAMP:
			case TIMESTAMP_WITH_TIMEZONE:
				odatatype = ODataTypes.DATETIMEOFFSET;
				break;
			case TINYINT:
				odatatype = ODataTypes.SBYTE;
				break;
			/*
				default also handles:
						case CHAR:
						case NVARCHAR:
						case VARCHAR:
						case CLOB:
						case LONGNVARCHAR:
						case LONGVARCHAR:
						case NCHAR:
						case NCLOB:
						case SQLXML:
				so, it handles all unknowns and strings basically
			 */
			default:
				odatatype = ODataTypes.STRING;
				maxlength = length;
				// Salesforce can not deal Long Text in external datasource connections, so we limit the maxLength here
				if (System.getenv("SALESFORCE_TOGGLE") != null &&
						System.getenv("SALESFORCE_TOGGLE").toLowerCase(Locale.ROOT).equals("true"))
					maxlength = SALESFORCE_STRING_MAX_LENGTH;
				break;
			}
		}
		this.odatatype = odatatype;
		if (maxlength != null) {
			this.maxlength = maxlength;
		}
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	@XmlAttribute(name = "Name")
	@JsonIgnore
	public String getName() {
		return name;
	}
	
	@XmlAttribute(name = "Type")
	@JsonProperty(ODataUtils.TYPE)
	public String getType() {
		if (this.odatatype != null) {
			return this.odatatype.getText();
		} else {
			return null;
		}
	}

	@XmlAttribute(name = "MaxLength")
	@JsonProperty(ODataUtils.MAXLENGTH)
	public Integer getMaxLength() {
		return this.maxlength;
	}

	@XmlAttribute(name = "Nullable")
	@JsonProperty(ODataUtils.NULLABLE)
	public Boolean getNullable() {
		return this.nullable;
	}

	@XmlAttribute(name = "Precision")
	@JsonProperty(ODataUtils.PRECISION)
	public Integer getPrecision() {
		return this.precision;
	}

	@XmlAttribute(name = "Scale")
	@JsonProperty(ODataUtils.SCALE)
	public Integer getScale() {
		return this.scale;
	}

	@JsonIgnore
	public ODataTypes getODataType() {
		return odatatype;
	}
}