package com.mojang.api.profiles;

import java.util.UUID;

public interface ProfileRepository {

    public Profile[] findProfilesByNames(String... names);

    public Profile findProfileById(UUID uuid);

}
