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



package com.enhantec.common.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/file")
public class FileController {

    String uploadTestFolder = "/Users/johnw/WorkFolder/EnhantecProducts/enhantec-framework/enhantec-main/src/main/java/com/enhantec/common/controllers/testFolder";

    @PostMapping("/upload")
    public FileInfo uploadFile(MultipartFile file, String type, String[] fileParams) throws IOException {
        System.out.println(file.getName());
        System.out.println(file.getOriginalFilename());
        System.out.println(file.getSize());
        long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        String targetFileName = file.getOriginalFilename() + "_" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + timeStamp;
        File targetFile = new File(uploadTestFolder + "/" + type + "/", timeStamp + ".txt");
        file.transferTo(targetFile);

        return new FileInfo(String.valueOf(timeStamp), file.getOriginalFilename(), targetFile.getAbsolutePath());
    }

    @GetMapping("/download/{type}/{id}")
    public void download(@PathVariable String id, @PathVariable String type, HttpServletRequest request, HttpServletResponse response) {
        try (InputStream inputStream = new FileInputStream(new File(uploadTestFolder + "/" + type + "/" + id + ".txt"));
             OutputStream outputStream = response.getOutputStream();
        ) {
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition","attachment;" +id + ".txt");
            IOUtils.copy(inputStream,outputStream);
            outputStream.flush();
        } catch (Exception e) {

        }
    }
}
