/**
 * Descripción: test unitario para el gestor de clientes de AWS.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.s3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GestorClientesServiciosAWSTest {

    @BeforeEach
    void setUp() {
        GestorClientesServiciosAWS.initCredentials("test-access-key", "test-secret-key");
    }

    @Test
    void testInitCredentials() {
        assertDoesNotThrow(() -> GestorClientesServiciosAWS.initCredentials("key", "secret"));
    }
    
    @Test
    void testInitCredentialsWithSessionToken() {
        assertDoesNotThrow(() -> GestorClientesServiciosAWS.initCredentials("key", "secret", "session-token"));
    }
    
    @Test
    void testInitProfile() {
        assertDoesNotThrow(() -> GestorClientesServiciosAWS.initProfile("default"));
    }

    @Test
    void testGetClientePrefirmadorBucketS3() {
        assertDoesNotThrow(() -> GestorClientesServiciosAWS.getClientePrefirmadorBucketS3());
    }
    
    @Test
    void testRegionAWSIsConfigured() {
        assertNotNull(GestorClientesServiciosAWS.REGION_AWS);
        assertEquals("us-east-1", GestorClientesServiciosAWS.REGION_AWS.id());
    }
    
}