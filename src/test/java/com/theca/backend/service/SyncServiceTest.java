/**
 * Descripción: test unitario para SyncService.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 1 may 2026
 */

package com.theca.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theca.backend.dto.sync.CambioDTO;
import com.theca.backend.dto.sync.SyncPullResponseDTO;
import com.theca.backend.dto.sync.SyncPushRequestDTO;
import com.theca.backend.entity.Cola;
import com.theca.backend.enums.EntidadCola;
import com.theca.backend.enums.OperacionCola;
import com.theca.backend.repository.ColaRepository;
import com.theca.backend.s3.GestorObjetosS3;

@ExtendWith(MockitoExtension.class)
public class SyncServiceTest {

    @Mock
    private ColaRepository colaRepository;

    @Mock
    private GestorObjetosS3 gestorObjetosS3;

    @InjectMocks
    private SyncService syncService;

    private CambioDTO cambio;
    private CambioDTO cambioConArchivo;

    @BeforeEach
    void setUp() {
        cambio = new CambioDTO();
        cambio.setEntidad("RECURSO");
        cambio.setIdEntidad("123");
        cambio.setOperacion("CREATE");
        cambio.setDatosJson("{\"foo\":\"bar\"}");

        cambioConArchivo = new CambioDTO();
        cambioConArchivo.setEntidad("RECURSO");
        cambioConArchivo.setIdEntidad("");
        cambioConArchivo.setOperacion("CREATE");
        
        String testBase64 = Base64.getEncoder().encodeToString("contenido de prueba".getBytes());
        cambioConArchivo.setArchivoBase64(testBase64);
        cambioConArchivo.setArchivoNombre("test.pdf");
        cambioConArchivo.setArchivoContentType("application/pdf");
        cambioConArchivo.setDatosJson("{\"titulo\":\"Test\",\"archivoBase64\":\"" + testBase64 + "\"}");
    }

    @Test
    void pushChanges_ShouldSaveEachCambio() {
        SyncPushRequestDTO req = new SyncPushRequestDTO();
        req.setCambios(Arrays.asList(cambio));

        when(colaRepository.save(any(Cola.class))).thenAnswer(i -> i.getArgument(0));

        syncService.pushChanges(req);

        ArgumentCaptor<Cola> captor = ArgumentCaptor.forClass(Cola.class);
        verify(colaRepository, times(1)).save(captor.capture());

        Cola saved = captor.getValue();
        assertNotNull(saved.getFechaModificacion());
        assertEquals(EntidadCola.RECURSO, saved.getEntidad());
        assertEquals("123", saved.getIdEntidad());
        assertEquals(OperacionCola.CREATE, saved.getOperacion());
        assertEquals("{\"foo\":\"bar\"}", saved.getJsonDatosCambiados());
        assertFalse(saved.isSincronizado());
    }

    @Test
    void pushChanges_WithArchivoOffline_ShouldProcessAndSave() {
        SyncPushRequestDTO req = new SyncPushRequestDTO();
        req.setCambios(Arrays.asList(cambioConArchivo));

        when(colaRepository.save(any(Cola.class))).thenAnswer(i -> i.getArgument(0));
        when(gestorObjetosS3.subirArchivoDesdeBytes(anyString(), any(byte[].class), anyString()))
            .thenReturn("offline-demo/123456-test.pdf");

        syncService.pushChanges(req);

        verify(gestorObjetosS3, times(1)).subirArchivoDesdeBytes(
            anyString(), 
            any(byte[].class), 
            eq("application/pdf")
        );

        ArgumentCaptor<Cola> captor = ArgumentCaptor.forClass(Cola.class);
        verify(colaRepository, times(1)).save(captor.capture());

        Cola saved = captor.getValue();
        String jsonGuardado = saved.getJsonDatosCambiados();
        
        assertFalse(jsonGuardado.contains("archivoBase64"));
        assertFalse(jsonGuardado.contains("archivoNombre"));
        assertFalse(jsonGuardado.contains("archivoContentType"));
        
        assertTrue(jsonGuardado.contains("archivoKey"));
    }

    @Test
    void pushChanges_WithArchivoOfflineButNotRecurso_ShouldNotProcess() {
        CambioDTO cambioNoRecurso = new CambioDTO();
        cambioNoRecurso.setEntidad("AUTOR");
        cambioNoRecurso.setOperacion("CREATE");
        cambioNoRecurso.setArchivoBase64("base64falso");
        cambioNoRecurso.setDatosJson("{\"nombre\":\"Autor\"}");

        SyncPushRequestDTO req = new SyncPushRequestDTO();
        req.setCambios(Arrays.asList(cambioNoRecurso));

        when(colaRepository.save(any(Cola.class))).thenAnswer(i -> i.getArgument(0));

        syncService.pushChanges(req);

        verify(gestorObjetosS3, never()).subirArchivoDesdeBytes(anyString(), any(byte[].class), anyString());
    }

    @Test
    void pushChanges_WithArchivoOfflineButNotCreate_ShouldNotProcess() {
        CambioDTO cambioUpdate = new CambioDTO();
        cambioUpdate.setEntidad("RECURSO");
        cambioUpdate.setOperacion("UPDATE");
        cambioUpdate.setArchivoBase64("base64falso");
        cambioUpdate.setDatosJson("{\"titulo\":\"Actualizado\"}");

        SyncPushRequestDTO req = new SyncPushRequestDTO();
        req.setCambios(Arrays.asList(cambioUpdate));

        when(colaRepository.save(any(Cola.class))).thenAnswer(i -> i.getArgument(0));

        syncService.pushChanges(req);

        verify(gestorObjetosS3, never()).subirArchivoDesdeBytes(anyString(), any(byte[].class), anyString());
    }

    @Test
    void pushChanges_WithArchivoOfflineButEmptyBase64_ShouldNotProcess() {
        CambioDTO cambioVacio = new CambioDTO();
        cambioVacio.setEntidad("RECURSO");
        cambioVacio.setOperacion("CREATE");
        cambioVacio.setArchivoBase64("");
        cambioVacio.setDatosJson("{\"titulo\":\"Test\"}");

        SyncPushRequestDTO req = new SyncPushRequestDTO();
        req.setCambios(Arrays.asList(cambioVacio));

        when(colaRepository.save(any(Cola.class))).thenAnswer(i -> i.getArgument(0));

        syncService.pushChanges(req);

        verify(gestorObjetosS3, never()).subirArchivoDesdeBytes(anyString(), any(byte[].class), anyString());
    }

    @Test
    void pullChanges_ShouldReturnMappedCambios() {
        Cola c1 = new Cola();
        c1.setId("1");
        c1.setEntidad(EntidadCola.ETIQUETA);
        c1.setIdEntidad("e1");
        c1.setOperacion(OperacionCola.UPDATE);
        c1.setJsonDatosCambiados("{\"k\":\"v\"}");
        c1.setFechaModificacion(LocalDateTime.now());
        c1.setSincronizado(false);

        when(colaRepository.findBySincronizadoFalse()).thenReturn(Arrays.asList(c1));

        SyncPullResponseDTO resp = syncService.pullChanges();
        List<CambioDTO> cambios = resp.getCambios();

        assertNotNull(cambios);
        assertEquals(1, cambios.size());
        assertEquals("ETIQUETA", cambios.get(0).getEntidad());
        assertEquals("e1", cambios.get(0).getIdEntidad());
        assertEquals("UPDATE", cambios.get(0).getOperacion());
        assertEquals("{\"k\":\"v\"}", cambios.get(0).getDatosJson());
    }

    @Test
    void markAsSynced_ShouldSetSincronizadoTrueAndSave() {
        Cola c = new Cola();
        c.setId("abc");
        c.setSincronizado(false);

        when(colaRepository.findById("abc")).thenReturn(Optional.of(c));

        syncService.markAsSynced(Arrays.asList("abc"));

        verify(colaRepository, times(1)).findById("abc");
        verify(colaRepository, times(1)).save(c);
        assertEquals(true, c.isSincronizado());
    }
    
    @Test
    void limpiarYActualizarJson_ShouldRemoveArchivoFields() {
        SyncPushRequestDTO req = new SyncPushRequestDTO();
        req.setCambios(Arrays.asList(cambioConArchivo));

        when(colaRepository.save(any(Cola.class))).thenAnswer(i -> i.getArgument(0));
        when(gestorObjetosS3.subirArchivoDesdeBytes(anyString(), any(byte[].class), anyString()))
            .thenReturn("test-key");

        syncService.pushChanges(req);

        ArgumentCaptor<Cola> captor = ArgumentCaptor.forClass(Cola.class);
        verify(colaRepository, times(1)).save(captor.capture());
        
        String json = captor.getValue().getJsonDatosCambiados();
        
        assertTrue(json.contains("archivoKey"));
        assertFalse(json.contains("archivoBase64"));
        assertFalse(json.contains("archivoNombre"));
        assertFalse(json.contains("archivoContentType"));
    }
    
}