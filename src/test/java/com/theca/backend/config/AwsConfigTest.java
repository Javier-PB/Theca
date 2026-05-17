/**
 * Descripción: test unitario para la configuración de AWS.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AwsConfigTest {

    @Test
    void testInitDoesNotThrowExceptionWithProfile() {
        AwsConfig awsConfig = new AwsConfig();
        ReflectionTestUtils.setField(awsConfig, "awsProfile", "default");
        ReflectionTestUtils.setField(awsConfig, "bucketName", "test-bucket");
        
        assertDoesNotThrow(() -> awsConfig.init());
    }
    
    @Test
    void testInitWithEmptyProfile() {
        AwsConfig awsConfig = new AwsConfig();
        ReflectionTestUtils.setField(awsConfig, "awsProfile", "");
        ReflectionTestUtils.setField(awsConfig, "bucketName", "test-bucket");
        
        assertDoesNotThrow(() -> awsConfig.init());
    }
    
    @Test
    void testInitWithNullProfile() {
        AwsConfig awsConfig = new AwsConfig();
        ReflectionTestUtils.setField(awsConfig, "awsProfile", null);
        ReflectionTestUtils.setField(awsConfig, "bucketName", "test-bucket");
        
        assertDoesNotThrow(() -> awsConfig.init());
    }
    
}