package io.rtdi.appcontainer.odata.entity.metadata;

import java.sql.JDBCType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.rtdi.appcontainer.odata.ODataTypes;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlAttribute;

public class EntityTypeProperty extends ODataBase {
	private String name;
	private String odatatype;
	private Integer maxlength;
	private Boolean nullable;
	private Integer precision;
	private Integer scale;
	
	public EntityTypeProperty() {};

	public EntityTypeProperty(String name, JDBCType type, Integer length, Integer scale) {
		this.name = name;
		String odatatype = ODataTypes.STRING.getText();
		Integer maxlength = null; 
		switch (type) {
		case BIGINT:
			odatatype = ODataTypes.INT64.getText();
			break;
		case BINARY:
		case BLOB:
		case LONGVARBINARY:
		case VARBINARY:
			odatatype = ODataTypes.BINARY.getText();
			maxlength = length;
			break;
		case BIT:
		case BOOLEAN:
			odatatype = ODataTypes.BOOLEAN.getText();
			break;
		case CHAR:
		case NVARCHAR:
		case VARCHAR:
			maxlength = length;
		case CLOB:
		case LONGNVARCHAR:
		case LONGVARCHAR:
		case NCHAR:
		case NCLOB:
		case SQLXML:
			odatatype = ODataTypes.STRING.getText();
			break;
		case DATE:
			odatatype = ODataTypes.DATE.getText();
			break;
		case DECIMAL:
		case NUMERIC:
			odatatype = ODataTypes.DECIMAL.getText();
			this.precision = maxlength;
			this.scale = scale;
			break;
		case DOUBLE:
			odatatype = ODataTypes.DOUBLE.getText();
			break;
		case FLOAT:
		case REAL:
			odatatype = ODataTypes.SINGLE.getText();
			break;
		case INTEGER:
			odatatype = ODataTypes.INT32.getText();
			break;
		case ROWID:
			odatatype = ODataTypes.STRING.getText();
			break;
		case SMALLINT:
			odatatype = ODataTypes.INT16.getText();
			break;
		case TIME:
		case TIME_WITH_TIMEZONE:
			odatatype = ODataTypes.TIMEOFDAY.getText();
			break;
		case TIMESTAMP:
		case TIMESTAMP_WITH_TIMEZONE:
			odatatype = ODataTypes.DATETIMEOFFSET.getText();
			break;
		case TINYINT:
			odatatype = ODataTypes.SBYTE.getText();
			break;
		default:
			break;
		
		}
		this.odatatype = odatatype;
		if (maxlength != null) {
			this.maxlength = maxlength;
		}
		// this.put(ODataUtils.JDBCDATATYPE, type.name());
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
		return this.odatatype;
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

}