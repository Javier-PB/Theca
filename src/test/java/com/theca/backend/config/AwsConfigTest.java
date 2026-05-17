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
    void testInitDoesNotThrowException() {
        AwsConfig awsConfig = new AwsConfig();
        ReflectionTestUtils.setField(awsConfig, "accessKeyId", "test-key");
        ReflectionTestUtils.setField(awsConfig, "secretAccessKey", "test-secret");
        
        assertDoesNotThrow(() -> awsConfig.init());
    }
    
    @Test
    void testInitWithEmptyCredentials() {
        AwsConfig awsConfig = new AwsConfig();
        ReflectionTestUtils.setField(awsConfig, "accessKeyId", "");
        ReflectionTestUtils.setField(awsConfig, "secretAccessKey", "");
        
        assertDoesNotThrow(() -> awsConfig.init());
    }
    
}