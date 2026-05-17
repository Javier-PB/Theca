/**
 * Descripción: clase para gestionar los objetos en S3.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.s3;

import java.net.URL;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class GestorObjetosS3 {
    
    @Value("${aws.s3.bucket-name}")
    private String nombreBucket;
    
    @Value("${aws.s3.url-expiration-minutes:10}")
    private int tiempoVidaMinutosUrl;
    
    public String obtenerURLPutDocumentoEnS3(String clave, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(nombreBucket)
                .key(clave)
                .contentType(contentType)
                .build();
        
        S3Presigner presigner = GestorClientesServiciosAWS.getClientePrefirmadorBucketS3();
        
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(tiempoVidaMinutosUrl))
                .putObjectRequest(putObjectRequest)
                .build();
        
        URL presignedUrl = presigner.presignPutObject(presignRequest).url();
        presigner.close();
        
        return presignedUrl.toString();
    }
    
    public String obtenerURLGetDocumentoEnS3(String clave) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(nombreBucket)
                .key(clave)
                .build();
        
        S3Presigner presigner = GestorClientesServiciosAWS.getClientePrefirmadorBucketS3();
        
        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(builder -> builder
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(tiempoVidaMinutosUrl)));
        
        URL presignedUrl = presignedRequest.url();
        presigner.close();
        
        return presignedUrl.toString();
    }
    
}
