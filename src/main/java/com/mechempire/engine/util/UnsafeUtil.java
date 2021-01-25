package com.mechempire.engine.util;

import com.mechempire.engine.runtime.engine.Engine;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * package: com.mechempire.engine.util
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-24 21:30
 */
@Slf4j
public class UnsafeUtil {

    public final static Unsafe unsafe = getUnsafe();

    public static Unsafe getUnsafe() {
        Unsafe unsafe = null;
        try {
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            unsafe = (Unsafe) singleoneInstanceField.get(null);
        } catch (Exception e) {
            log.error("getUnsafe error: {}", e.getMessage(), e);
        }

        return unsafe;
    }

    public static long getFieldOffset(Class<?> clazz, String fieldName) {
        try {
            return unsafe.objectFieldOffset(Engine.class.getDeclaredField("status"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
