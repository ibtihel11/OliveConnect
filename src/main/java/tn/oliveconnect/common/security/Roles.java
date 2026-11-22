package tn.oliveconnect.common.security;

import java.util.Set;

/** Valeurs de l'énumération Role (diagramme de classes). Dans les JWT : claim "role" = ce nom, sans préfixe. */
public final class Roles {

    public static final String AGRICULTEUR = "AGRICULTEUR";
    public static final String RESPONSABLE_COLLECTE = "RESPONSABLE_COLLECTE";
    public static final String CHEF_EQUIPE = "CHEF_EQUIPE";
    public static final String RESPONSABLE_LOGISTIQUE = "RESPONSABLE_LOGISTIQUE";
    public static final String RESPONSABLE_PRESSOIR = "RESPONSABLE_PRESSOIR";
    public static final String ADMINISTRATEUR = "ADMINISTRATEUR";

    public static final Set<String> TOUS = Set.of(
            AGRICULTEUR, RESPONSABLE_COLLECTE, CHEF_EQUIPE,
            RESPONSABLE_LOGISTIQUE, RESPONSABLE_PRESSOIR, ADMINISTRATEUR);

    private Roles() {
    }
}
