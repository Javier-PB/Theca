/**
 * Descripción: clase para gestionar los servicios AWS.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class GestorClientesServiciosAWS {
    
    public static final Region REGION_AWS = Region.US_EAST_1;
    
    private static String accessKeyId;
    private static String secretAccessKey;
    
    // Se inicializa con las credenciales desde variables de entorno:
    public static void initCredentials(String accessKey, String secretKey) {
        accessKeyId = accessKey;
        secretAccessKey = secretKey;
    }
    
    private GestorClientesServiciosAWS() {}
    
    public static S3Presigner getClientePrefirmadorBucketS3() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        return S3Presigner.builder()
                .region(REGION_AWS)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
    
}
