package com.enhantec.test.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockServer {

    public static void main(String[] args) throws IOException {

        configureFor(8062);
        removeAllMappings();

        mock("/order/1", "1");

    }

    private  static void mock(String url, String file) throws IOException {
        ClassPathResource resource = new ClassPathResource("mockData/response/"+file+".txt");
        String content = StringUtils.join(FileUtils.readLines(resource.getFile(),"UTF-8").toArray(),'\n');
        stubFor(get(urlPathEqualTo(url)).willReturn(aResponse().withBody(content).withStatus(200)));
    }
}
