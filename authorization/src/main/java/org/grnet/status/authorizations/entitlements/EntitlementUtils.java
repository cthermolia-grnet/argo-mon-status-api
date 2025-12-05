package org.grnet.status.authorizations.entitlements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EntitlementUtils {

    private static final Pattern PATTERN =
            Pattern.compile(".*group:((?:[^:]+)(?::[^:]+)*):role=([^:]+)$");

    public static Entitlement parse(String raw) {

        var m = PATTERN.matcher(raw);
        if (!m.matches()) {
            return null;
        }

        var full = m.group(1);
        var role = m.group(2);

        String[] parts = full.split(":");
        List<String> hierarchy = new ArrayList<>();

        for (var p : parts) {
            hierarchy.add(p);
        }

        // subgroup1 is the authorization group
        var group = (hierarchy.size() > 1) ? hierarchy.get(1) : hierarchy.get(0);

        return new Entitlement(group, hierarchy, role, raw);
    }

    public static List<Entitlement> parseEntitlements(List<String> raws) {
        var list = new ArrayList<Entitlement>();
        if (raws == null) {
            return list;
        }

        for (var r : raws) {
            Entitlement e = parse(r);
            if (e != null) {
                list.add(e);
            }
        }

        return list;
    }
}
