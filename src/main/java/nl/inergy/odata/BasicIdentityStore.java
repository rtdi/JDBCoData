package nl.inergy.odata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import nl.inergy.odata.service.Roles;
import java.util.HashSet;

import static jakarta.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static java.util.Arrays.asList;

@ApplicationScoped
public class BasicIdentityStore implements IdentityStore {

    static private final String clientUser = System.getenv("ODATA_CLIENT_USER");
    static private final String clientPassword = System.getenv("ODATA_CLIENT_PASSWORD");

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {
        if (usernamePasswordCredential.compareTo(clientUser, clientPassword)) {
            return new CredentialValidationResult(clientUser, new HashSet<>(asList(Roles.odataRole)));
        }
        return INVALID_RESULT;
    }
}
