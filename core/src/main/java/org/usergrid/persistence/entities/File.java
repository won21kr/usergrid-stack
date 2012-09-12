package org.usergrid.persistence.entities;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.usergrid.persistence.TypedEntity;
import org.usergrid.persistence.annotations.EntityCollection;
import org.usergrid.persistence.annotations.EntityDictionary;
import org.usergrid.persistence.annotations.EntityProperty;

public class File extends TypedEntity{
	
	public static final String ENTITY_TYPE = "file";

	public static final String PROPERTY_UUID = "uuid";

	public static final String PROPERTY_PATH = "path";

	public static final String PROPERTY_DIR = "dir";

	public static final String PROPERTY_SIZE = "size";
	
	@EntityProperty(indexed = true, fulltextIndexed = false, required = true, indexedInConnections = true, aliasProperty = true, pathBasedName = true, mutable = false, unique = true)
	protected String path;


	@EntityProperty(indexed = true, fulltextIndexed = false, required = true, basic = true)
	protected String dir;

	@EntityProperty(indexed = true, fulltextIndexed = false, required = true, basic = true)
	protected String size;


	@EntityDictionary(keyType = java.lang.String.class)
	protected Set<String> connections;
	
	@EntityDictionary(keyType = java.lang.String.class, valueType = java.lang.String.class)
	protected Map<String, String> rolenames;

	@EntityDictionary(keyType = java.lang.String.class)
	protected Set<String> permissions;


	@EntityCollection(type = "role", linkedCollection = "files")
	protected List<UUID> roles;

	public File() {
		// id = UUIDUtils.newTimeUUID();
	}

	public File(UUID id) {
		uuid = id;
	}
	
	@JsonSerialize(include = Inclusion.NON_NULL)
	public Set<String> getConnections() {
		return connections;
	}

	public void setConnections(Set<String> connections) {
		this.connections = connections;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	public Map<String, String> getRolenames() {
		return rolenames;
	}

	public void setRolenames(Map<String, String> rolenames) {
		this.rolenames = rolenames;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	public List<UUID> getRoles() {
		return roles;
	}

	public void setRoles(List<UUID> roles) {
		this.roles = roles;
	}
	
	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}

