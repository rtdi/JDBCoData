package io.rtdi.appcontainer.odata;

public class ODataIdentifier {
//	public static final String ENTITYTYPE = "ROW";
//	public static final String ENTITYSETNAME = "RS";
//	public static final String ENTITYNAME = "DBOBJECT";
	private String dbschema;
	private String dbobjectname;
	private String entitysetname;
	private String entityname;
	private String entitytype;
	private String identifier;

	public ODataIdentifier(String dbschema, String dbobjectname, String entityname, String entitysetname, String entitytype) {
		super();
		this.dbschema = dbschema;
		this.dbobjectname = dbobjectname;
		this.entitysetname = entitysetname;
		this.entityname = entityname;
		this.entitytype = entitytype;
		this.identifier = createIdentifier(dbschema, dbobjectname);
	}

	public ODataIdentifier(String dbschema, String dbobjectname) {
		this(dbschema, dbobjectname, dbobjectname, dbobjectname, ENTITYTYPE);
	}
	
	public String getDBSchema() {
		return dbschema;
	}

	public String getDBObjectName() {
		return dbobjectname;
	}

	public String getEntitySetName() {
		return entitysetname;
	}

	public String getEntityName() {
		return entityname;
	}

	public String getEntityType() {
		return entitytype;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof ODataIdentifier) {
			return identifier.equals(((ODataIdentifier) o).getIdentifier());
		} else {
			return false;
		}
	}
	
	public static String createIdentifier(String dbschema, String dbobjectname) {
		return '"' + dbschema + "\".\"" + dbobjectname + '"';
	}
}
