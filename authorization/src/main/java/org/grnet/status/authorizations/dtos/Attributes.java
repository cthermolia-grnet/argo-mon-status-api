package org.grnet.status.authorizations.dtos;

import java.util.List;

public class Attributes {

    public List<String> uid;

    private List<String> localEntitlements;

    public List<String> getLocalEntitlements() {
        return localEntitlements;
    }
}
