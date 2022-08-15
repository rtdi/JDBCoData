package nl.inergy.odata;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import nl.inergy.odata.service.Roles;

@ApplicationScoped
@ApplicationPath("api")
@DeclareRoles({ Roles.odataRole })
@BasicAuthenticationMechanismDefinition(realmName = "snowflake")
public class ODataMain extends Application {
}
