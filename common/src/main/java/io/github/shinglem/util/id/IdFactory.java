package io.github.shinglem.util.id;

import io.github.shinglem.util.id.impl.SnowFlake;
import io.github.shinglem.util.id.impl.Uuid;

public class IdFactory {

    public IdGenerator getIdGenerator(String name) throws IdException {
        if ("SnowFlake".equals(name)) {
            return new SnowFlake();
        }else if("UUID".equals(name)) {
            return new Uuid();
        }else {
            throw new IdException("IdGenerator not support : " + name);
        }
    }

}
