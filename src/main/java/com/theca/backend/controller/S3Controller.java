/**
 * Descripción: controlador para la operaciones con S3.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.theca.backend.dto.s3.S3DownloadUrlDTO;
import com.theca.backend.dto.s3.S3UploadUrlDTO;
import com.theca.backend.s3.GestorObjetosS3;
import com.theca.backend.security.jwt.JwtUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@Tag(name = "S3", description = "Endpoints para gestionar archivos en S3")
@RequestMapping("/api/s3")
public class S3Controller {

    @Autowired
    private GestorObjetosS3 gestorObjetosS3;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/upload-url")
    @Operation(summary = "Obtener URL prefirmada para subir un archivo", 
               description = "Devuelve una URL firmada para subir un archivo directamente a S3")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URL obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<S3UploadUrlDTO> getUploadUrl(
            @RequestParam String fileName,
            @RequestParam String contentType,
            HttpServletRequest request) {
        
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        
        String key = userId + "/" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString() + "-" + fileName;
        
        String uploadUrl = gestorObjetosS3.obtenerURLPutDocumentoEnS3(key, contentType);
        
        return ResponseEntity.ok(new S3UploadUrlDTO(uploadUrl, key));
    }
    
    @GetMapping("/download-url")
    @Operation(summary = "Obtener URL prefirmada para descargar un archivo",
               description = "Devuelve una URL firmada para descargar un archivo de S3")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URL obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso prohibido"),
        @ApiResponse(responseCode = "404", description = "Archivo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<S3DownloadUrlDTO> getDownloadUrl(
            @RequestParam String key,
            HttpServletRequest request) {
        
        String decodedKey = key;
        try {
            decodedKey = java.net.URLDecoder.decode(key, "UTF-8");
        } catch (Exception e) {
            decodedKey = key;
        }
        
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        
        if (!decodedKey.startsWith(userId + "/")) {
            return ResponseEntity.status(403).build();
        }
        
        String downloadUrl = gestorObjetosS3.obtenerURLGetDocumentoEnS3(decodedKey);
        
        return ResponseEntity.ok(new S3DownloadUrlDTO(downloadUrl, decodedKey));
    }
    
    // Método auxiliar para extraer el token del header:
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    // Método auxiliar para extraer el userId del token:
    private String getUserIdFromToken(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token != null && jwtUtils.validateJwtToken(token)) {
            return jwtUtils.getUserIdFromJwtToken(token);
        }
        return null;
    }
    
}