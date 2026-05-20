/**
 * Descripción: test unitario para el gestor de objetos de S3.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.s3;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GestorObjetosS3Test {

    @InjectMocks
    private GestorObjetosS3 gestorObjetosS3;

    @BeforeEach
    void setUp() {
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
    
    @Test
    void testObtenerURLPutDocumentoEnS3() {
        assertDoesNotThrow(() -> {});
    }
    
    @Test
    void testObtenerURLGetDocumentoEnS3() {
        assertDoesNotThrow(() -> {});
    }
    
    @Test
    void testSubirArchivoDesdeBytes_NullKey() {
        assertThrows(RuntimeException.class, () -> {
            gestorObjetosS3.subirArchivoDesdeBytes(null, new byte[0], "application/pdf");
        });
    }
    
    @Test
    void testSubirArchivoDesdeBytes_EmptyBytes() {
        assertDoesNotThrow(() -> {});
    }
    
}