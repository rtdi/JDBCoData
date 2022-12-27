package io.rtdi.appcontainer.odata;

public class ODataIdentifier {
	public static final String ENTITYTPE = "ROW";
	public static final String ENTITYSETNAME = "RS";
	public static final String ENTITYNAME = "DBOBJECT";
	private String dbschema;
	private String dbobjectname;
	private String entitysetname;
	private String entityname;
	private String entitytype;
	private String identifier;
	private String namespace;

	public ODataIdentifier(String dbschema, String dbobjectname, String entityname, String entitysetname, String entitytype) {
		super();
		this.dbschema = dbschema;
		this.dbobjectname = dbobjectname;
		this.entitysetname = entitysetname;
		this.entityname = entityname;
		this.entitytype = entitytype;
		this.identifier = createSqlIdentifier(dbschema, dbobjectname);
	}

	/**
	 * Create an identifier with the real db object names.
	 * Used for the /odata/schemas/ endpoint.
	 * 
	 * @param dbschema
	 * @param dbobjectname
	 */
	public ODataIdentifier(String dbschema, String dbobjectname) {
		this(dbschema, dbobjectname, createEntityType(dbschema, dbobjectname), ENTITYSETNAME, ENTITYTPE);
		this.namespace = createNamespace(dbschema, dbobjectname);
	}
	
	/**
	 * Create an identifier with a given entityname which is also used as namespace.
	 * 
	 * @param dbschema
	 * @param dbobjectname
	 * @param entityname
	 */
	public ODataIdentifier(String dbschema, String dbobjectname, String entityname) {
		this(dbschema, dbobjectname, entityname, ENTITYSETNAME, ENTITYTPE);
		this.namespace =  entityname;
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
	
	public String getNamespace() {
		return namespace;
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
	

	public static String createEntityType(String dbschema, String dbobjectname) {
		return dbschema + "_" + dbobjectname;
	}

	public static String createNamespace(String dbschema, String dbobjectname) {
		return dbschema + "." + dbobjectname;
	}
	
	public static String createSqlIdentifier(String dbschema, String dbobjectname) {
		return '"' + dbschema + "\".\"" + dbobjectname + '"';
	}
}
