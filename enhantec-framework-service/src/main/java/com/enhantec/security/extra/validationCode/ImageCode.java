package com.enhantec.security.extra.validationCode;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

@Data
public class ImageCode {

     private final BufferedImage image;
    private final String code;
    private final LocalDateTime expiredTime;

    public ImageCode(BufferedImage image, String code, int expireInSeconds){
        this.image = image;
        this.code= code;
        this.expiredTime = LocalDateTime.now().plusSeconds(expireInSeconds);
    }

    boolean isExpired(){
        return  LocalDateTime.now().isAfter(this.expiredTime);
    }

}
