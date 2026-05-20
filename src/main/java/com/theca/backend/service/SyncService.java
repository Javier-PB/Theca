/**
 * Descripción: Servicio para gestionar la sincronización offline/online.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 1 may 2026
 */

package com.theca.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theca.backend.dto.sync.CambioDTO;
import com.theca.backend.dto.sync.SyncPushRequestDTO;
import com.theca.backend.dto.sync.SyncPullResponseDTO;
import com.theca.backend.entity.Cola;
import com.theca.backend.enums.EntidadCola;
import com.theca.backend.enums.OperacionCola;
import com.theca.backend.repository.ColaRepository;
import com.theca.backend.s3.GestorObjetosS3;

@Service
public class SyncService {

    @Autowired
    private ColaRepository colaRepository;
    
    @Autowired
    private GestorObjetosS3 gestorObjetosS3;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Método que recibe cambios del cliente y los guarda en la cola:
    public void pushChanges(SyncPushRequestDTO request) {
        System.out.println("=== PUSH CHANGES RECEIVED ===");
        System.out.println("Número de cambios: " + request.getCambios().size());
        
        for (CambioDTO cambio : request.getCambios()) {
            System.out.println("--- Procesando cambio ---");
            System.out.println("  entidad: " + cambio.getEntidad());
            System.out.println("  operacion: " + cambio.getOperacion());
            System.out.println("  archivoBase64 null? " + (cambio.getArchivoBase64() == null));
            if (cambio.getArchivoBase64() != null) {
                System.out.println("  archivoBase64 length: " + cambio.getArchivoBase64().length());
                System.out.println("  archivoNombre: " + cambio.getArchivoNombre());
                System.out.println("  archivoContentType: " + cambio.getArchivoContentType());
            }
            
            String archivoKey = procesarArchivoOffline(cambio);
            System.out.println("  archivoKey obtenido: " + archivoKey);
            
            String datosJsonFinal = limpiarYActualizarJson(cambio.getDatosJson(), archivoKey);
            
            Cola registro = new Cola();
            registro.setEntidad(EntidadCola.valueOf(cambio.getEntidad()));
            registro.setIdEntidad(cambio.getIdEntidad());
            registro.setOperacion(OperacionCola.valueOf(cambio.getOperacion()));
            registro.setJsonDatosCambiados(datosJsonFinal);
            registro.setFechaModificacion(LocalDateTime.now());
            registro.setSincronizado(false);
            
            colaRepository.save(registro);
        }
    }
    
    // Método para procesar un archivo offline y subirlo a S3:
    private String procesarArchivoOffline(CambioDTO cambio) {
        System.out.println(">>> ENTRANDO A procesarArchivoOffline <<<");
        
        if (!"RECURSO".equals(cambio.getEntidad())) {
            System.out.println("  No es RECURSO, es: " + cambio.getEntidad());
            return null;
        }
        if (!"CREATE".equals(cambio.getOperacion())) {
            System.out.println("  No es CREATE, es: " + cambio.getOperacion());
            return null;
        }
        if (cambio.getArchivoBase64() == null) {
            System.out.println("  archivoBase64 es NULL");
            return null;
        }
        if (cambio.getArchivoBase64().isEmpty()) {
            System.out.println("  archivoBase64 está VACÍO");
            return null;
        }
        
        System.out.println("  ✅ Pasa todas las validaciones");
        System.out.println("  archivoBase64 length: " + cambio.getArchivoBase64().length());
        
        try {
            byte[] archivoBytes = Base64.getDecoder().decode(cambio.getArchivoBase64());
            System.out.println("  Bytes decodificados: " + archivoBytes.length);
            
            String key = "offline-demo/" + System.currentTimeMillis() + "-" + 
                         (cambio.getArchivoNombre() != null ? cambio.getArchivoNombre() : "archivo.bin");
            System.out.println("  Key generada: " + key);
            
            String contentType = cambio.getArchivoContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }
            System.out.println("  ContentType: " + contentType);
            
            gestorObjetosS3.subirArchivoDesdeBytes(key, archivoBytes, contentType);
            
            System.out.println("  ✅ Archivo subido exitosamente a S3");
            return key;
            
        } catch (Exception e) {
            System.err.println("  ❌ ERROR en procesarArchivoOffline: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Método para limpiar campos temporales del JSON y añadir el archivoKey:
    private String limpiarYActualizarJson(String datosJson, String archivoKey) {
        if (datosJson == null || datosJson.isEmpty()) {
            return datosJson;
        }
        
        try {
            JsonNode node = objectMapper.readTree(datosJson);
            
            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                
                objectNode.remove("archivoBase64");
                objectNode.remove("archivoNombre");
                objectNode.remove("archivoContentType");
                
                if (archivoKey != null && !archivoKey.isEmpty()) {
                    objectNode.put("archivoKey", archivoKey);
                }
                
                return objectMapper.writeValueAsString(objectNode);
            }
        } catch (Exception e) {}
        
        return datosJson;
    }

    // Método que devuelve los cambios pendientes al cliente:
    public SyncPullResponseDTO pullChanges() {
        List<Cola> pendientes = colaRepository.findBySincronizadoFalse();
        List<CambioDTO> cambios = new ArrayList<>();
        
        for (Cola cola : pendientes) {
            CambioDTO cambio = new CambioDTO();
            cambio.setEntidad(cola.getEntidad().name());
            cambio.setIdEntidad(cola.getIdEntidad());
            cambio.setOperacion(cola.getOperacion().name());
            cambio.setDatosJson(cola.getJsonDatosCambiados());
            cambios.add(cambio);
        }
        
        return new SyncPullResponseDTO(cambios, LocalDateTime.now().toString());
    }
    
    // Método que marca los cambios como sincronizados:
    public void markAsSynced(List<String> ids) {
        for (String id : ids) {
            colaRepository.findById(id).ifPresent(cola -> {
                cola.setSincronizado(true);
                colaRepository.save(cola);
            });
        }
    }
    
}