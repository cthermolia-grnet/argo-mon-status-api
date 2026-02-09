package org.grnet.status.dtos.profile.operation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Operation {

    private String name;

    @JsonProperty("truth_table")
    private List<TruthTableEntry> truthTable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TruthTableEntry> getTruthTable() {
        return truthTable;
    }

    public void setTruthTable(List<TruthTableEntry> truthTable) {
        this.truthTable = truthTable;
    }
}

