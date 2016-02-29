package com.iklimov.tictactoe.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/** The Objectify object model for device registrations we are persisting */
@Entity
public class Player {

    @Id
    Long id;

    @Index
    private String regId;

    private String name;

    public Player() {}

    public String getRegId() {
        return regId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }
}