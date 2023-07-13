package com.enhantec.wms.backend.utils.common;

public class ExceptionHelper {

    public static void throwRfFulfillLogicException(String message) throws FulfillLogicException {

        throw new FulfillLogicException(message);

    }

}
