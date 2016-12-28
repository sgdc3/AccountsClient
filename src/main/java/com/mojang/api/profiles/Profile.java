package com.mojang.api.profiles;

import java.util.UUID;

public class Profile {

    private final UUID id;
    private final String name;

    public Profile(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
