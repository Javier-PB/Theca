/**
 * Descripción: test unitario para el gestor de objetos de S3.
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
import org.springframework.test.util.ReflectionTestUtils;

class GestorObjetosS3Test {

    private GestorObjetosS3 gestorObjetosS3;

    @BeforeEach
    void setUp() {
        gestorObjetosS3 = new GestorObjetosS3();
        ReflectionTestUtils.setField(gestorObjetosS3, "nombreBucket", "test-bucket");
        ReflectionTestUtils.setField(gestorObjetosS3, "tiempoVidaMinutosUrl", 10);
    }

    @Test
    void testConstructor() {
        assertNotNull(gestorObjetosS3);
    }

    @Test
    void testNombreBucketConfigurado() {
        String nombreBucket = (String) ReflectionTestUtils.getField(gestorObjetosS3, "nombreBucket");
        assertEquals("test-bucket", nombreBucket);
    }

    @Test
    void testTiempoVidaConfigurado() {
        Integer tiempoVida = (Integer) ReflectionTestUtils.getField(gestorObjetosS3, "tiempoVidaMinutosUrl");
        assertEquals(10, tiempoVida);
    }
    
}
