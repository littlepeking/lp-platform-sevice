/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.security.extra.validationCode;

import com.enhantec.config.properties.ApplicationProperties;
import lombok.Data;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Data
public class ImageValidateCodeGenerator implements ValidateCodeGenerator{

    private ApplicationProperties applicationProperties;

    private char mapTable[]={
            'a','b','c','d','e','f',
            'g','h','i','j','k','l',
            'm','n','o','p','q','r',
            's','t','u','v','w','x',
            'y','z','0','1','2','3',
            '4','5','6','7','8','9'};

    public ImageCode generateImageCode(HttpServletRequest request) {
        int width = ServletRequestUtils.getIntParameter(request,"width",applicationProperties.getValidationCode().getImage().getWidth());
        int height = ServletRequestUtils.getIntParameter(request,"height",applicationProperties.getValidationCode().getImage().getHeight());


        if(width<=0)width=60;
        if(height<=0)height=20;
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        //Getting the graphics context
        Graphics g = image.getGraphics();
        //Setting the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        //Picture frame
        g.setColor(Color.black);
        g.drawRect(0,0,width-1,height-1);
        //Take the randomly generated authentication code
        String strEnsure = "";
        //4 represents a 4-bit verification code. If you want to generate more bits, increase the value
        for(int i=0; i<applicationProperties.getValidationCode().getImage().getLength(); ++i) {
            strEnsure+=mapTable[(int)(mapTable.length*Math.random())];
        }
        //Displays the authentication code in the image, and adds a drawString statement if you want to generate more bits of authentication code
        g.setColor(Color.black);
        g.setFont(new Font("Atlantic Inline",Font.PLAIN,18));
        g.drawString(strEnsure,8,17);
        //I randomly generate 10 interference points
        Random rand = new Random();
        for (int i=0;i<10;i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            g.drawOval(x,y,1,1);
        }
        //Freeing the graph context
        g.dispose();

        ImageCode imageCode = new ImageCode(image,strEnsure,applicationProperties.getValidationCode().getImage().getExpireIn());

        return imageCode;

    }
}
