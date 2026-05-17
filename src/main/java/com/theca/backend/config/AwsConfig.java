/**
 * Descripción: configuración de AWS.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.theca.backend.s3.GestorClientesServiciosAWS;

import jakarta.annotation.PostConstruct;

@Configuration
public class AwsConfig {
    
    @Value("${aws.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;
    
    @PostConstruct
    public void init() {
        if (accessKeyId != null && !accessKeyId.isEmpty() 
            && secretAccessKey != null && !secretAccessKey.isEmpty()) {
            GestorClientesServiciosAWS.initCredentials(accessKeyId, secretAccessKey);
            System.out.println("AWS credentials initialized");
        } else {
            System.out.println("AWS credentials not configured. S3 features will not work in local.");
        }
    }
    
}
