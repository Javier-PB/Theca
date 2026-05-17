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
 
 @Value("${aws.profile:default}")
 private String awsProfile;
 
 @Value("${aws.s3.bucket-name}")
 private String bucketName;
 
 @PostConstruct
 public void init() {
     if (System.getenv("CI") != null && System.getenv("CI").equals("true")) {
         System.out.println("Running in CI mode, skipping AWS initialization");
         return;
     }
     GestorClientesServiciosAWS.initProfile(awsProfile);
     System.out.println("AWS profile configured: " + awsProfile);
 }
 
}
