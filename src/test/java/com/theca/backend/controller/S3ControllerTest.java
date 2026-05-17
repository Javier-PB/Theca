/**
 * Descripción: test unitario para controlador de S3.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.theca.backend.dto.s3.S3DownloadUrlDTO;
import com.theca.backend.dto.s3.S3UploadUrlDTO;
import com.theca.backend.s3.GestorObjetosS3;
import com.theca.backend.security.jwt.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class S3ControllerTest {

    @Mock
    private GestorObjetosS3 gestorObjetosS3;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private S3Controller s3Controller;

    private final String mockUserId = "user123";
    private final String mockToken = "mock.jwt.token";
    private final String mockUploadUrl = "https://s3.amazonaws.com/bucket/upload-url";
    private final String mockDownloadUrl = "https://s3.amazonaws.com/bucket/download-url";

    @BeforeEach
    void setUp() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + mockToken);
    }

    @Test
    void getUploadUrl_ShouldReturnUploadUrl_WhenValidRequest() {
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);
        when(gestorObjetosS3.obtenerURLPutDocumentoEnS3(anyString(), anyString())).thenReturn(mockUploadUrl);

        ResponseEntity<S3UploadUrlDTO> response = s3Controller.getUploadUrl("test.pdf", "application/pdf", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockUploadUrl, response.getBody().getUploadUrl());
        assertNotNull(response.getBody().getKey());
        assertTrue(response.getBody().getKey().startsWith(mockUserId + "/"));
        
        verify(gestorObjetosS3).obtenerURLPutDocumentoEnS3(anyString(), eq("application/pdf"));
    }

    @Test
    void getUploadUrl_ShouldReturnBadRequest_WhenTokenIsInvalid() {
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(false);

        ResponseEntity<S3UploadUrlDTO> response = s3Controller.getUploadUrl("test.pdf", "application/pdf", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(gestorObjetosS3, never()).obtenerURLPutDocumentoEnS3(anyString(), anyString());
    }

    @Test
    void getUploadUrl_ShouldReturnBadRequest_WhenNoAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<S3UploadUrlDTO> response = s3Controller.getUploadUrl("test.pdf", "application/pdf", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(gestorObjetosS3, never()).obtenerURLPutDocumentoEnS3(anyString(), anyString());
    }

    @Test
    void getUploadUrl_ShouldGenerateUniqueKeyForEachRequest() {
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);
        when(gestorObjetosS3.obtenerURLPutDocumentoEnS3(anyString(), anyString())).thenReturn(mockUploadUrl);

        ResponseEntity<S3UploadUrlDTO> response1 = s3Controller.getUploadUrl("test.pdf", "application/pdf", request);
        ResponseEntity<S3UploadUrlDTO> response2 = s3Controller.getUploadUrl("test.pdf", "application/pdf", request);

        assertNotEquals(response1.getBody().getKey(), response2.getBody().getKey());
    }

    @Test
    void getDownloadUrl_ShouldReturnDownloadUrl_WhenValidRequestWithUnencodedKey() {
        String key = mockUserId + "/123-test-key.pdf";
        
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);
        when(gestorObjetosS3.obtenerURLGetDocumentoEnS3(key)).thenReturn(mockDownloadUrl);

        ResponseEntity<S3DownloadUrlDTO> response = s3Controller.getDownloadUrl(key, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockDownloadUrl, response.getBody().getDownloadUrl());
        assertEquals(key, response.getBody().getKey());
        
        verify(gestorObjetosS3).obtenerURLGetDocumentoEnS3(key);
    }

    @Test
    void getDownloadUrl_ShouldReturnDownloadUrl_WhenValidRequestWithEncodedKey() throws Exception {
        String originalKey = mockUserId + "/123-test-key.pdf";
        String encodedKey = URLEncoder.encode(originalKey, StandardCharsets.UTF_8.toString());
        
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);
        when(gestorObjetosS3.obtenerURLGetDocumentoEnS3(originalKey)).thenReturn(mockDownloadUrl);

        ResponseEntity<S3DownloadUrlDTO> response = s3Controller.getDownloadUrl(encodedKey, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockDownloadUrl, response.getBody().getDownloadUrl());
        assertEquals(originalKey, response.getBody().getKey());
        
        verify(gestorObjetosS3).obtenerURLGetDocumentoEnS3(originalKey);
    }

    @Test
    void getDownloadUrl_ShouldReturnDownloadUrl_WhenKeyHasSpecialCharacters() throws Exception {
        String originalKey = mockUserId + "/test-file with spaces & special chars (version 1).pdf";
        String encodedKey = URLEncoder.encode(originalKey, StandardCharsets.UTF_8.toString());
        
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);
        when(gestorObjetosS3.obtenerURLGetDocumentoEnS3(originalKey)).thenReturn(mockDownloadUrl);

        ResponseEntity<S3DownloadUrlDTO> response = s3Controller.getDownloadUrl(encodedKey, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(originalKey, response.getBody().getKey());
        
        verify(gestorObjetosS3).obtenerURLGetDocumentoEnS3(originalKey);
    }

    @Test
    void getDownloadUrl_ShouldReturnBadRequest_WhenTokenIsInvalid() {
        String key = mockUserId + "/123-test-key.pdf";
        
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(false);

        ResponseEntity<S3DownloadUrlDTO> response = s3Controller.getDownloadUrl(key, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(gestorObjetosS3, never()).obtenerURLGetDocumentoEnS3(anyString());
    }

    @Test
    void getDownloadUrl_ShouldReturnForbidden_WhenKeyDoesNotBelongToUser() {
        String key = "otherUser/123-test-key.pdf";
        
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);

        ResponseEntity<S3DownloadUrlDTO> response = s3Controller.getDownloadUrl(key, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(gestorObjetosS3, never()).obtenerURLGetDocumentoEnS3(anyString());
    }

    @Test
    void getDownloadUrl_ShouldReturnForbidden_WhenKeyIsMalformed() {
        String key = "no-user-prefix.pdf";
        
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);

        ResponseEntity<S3DownloadUrlDTO> response = s3Controller.getDownloadUrl(key, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(gestorObjetosS3, never()).obtenerURLGetDocumentoEnS3(anyString());
    }

    @Test
    void getDownloadUrl_ShouldHandleDecodingError_Gracefully() {
        String invalidKey = "user123/invalid%";
        
        when(jwtUtils.validateJwtToken(mockToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(mockToken)).thenReturn(mockUserId);
        
        when(gestorObjetosS3.obtenerURLGetDocumentoEnS3(invalidKey)).thenReturn(mockDownloadUrl);

        ResponseEntity<S3DownloadUrlDTO> response = s3Controller.getDownloadUrl(invalidKey, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gestorObjetosS3).obtenerURLGetDocumentoEnS3(invalidKey);
    }
    
}