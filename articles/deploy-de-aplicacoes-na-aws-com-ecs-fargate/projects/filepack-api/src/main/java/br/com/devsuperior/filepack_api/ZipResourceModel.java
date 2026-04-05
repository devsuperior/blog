package br.com.devsuperior.filepack_api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.core.io.Resource;

/**
 * Model que encapsula um recurso ZIP criptografado e gerencia o ciclo de vida
 * dos arquivos temporários associados.
 * 
 * Esta classe segue o pattern Resource Holder, garantindo que todos os arquivos
 * temporários criados durante o processo de compactação sejam devidamente
 * limpos após o uso.
 */
public class ZipResourceModel {
    
    private final Resource resource;
    private final String filename;
    private final long size;
    private final List<File> tempFilesToCleanup;

    public ZipResourceModel(Resource resource, String filename, long size, List<File> tempFilesToCleanup) {
        this.resource = resource;
        this.filename = filename;
        this.size = size;
        this.tempFilesToCleanup = tempFilesToCleanup;
    }

    public Resource getResource() {
        return resource;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    /**
     * Limpa todos os arquivos temporários associados a este recurso.
     * Este método deve ser chamado após o recurso ser utilizado para
     * evitar acúmulo de arquivos temporários no sistema.
     */
    public void cleanup() {
        for (File tempFile : tempFilesToCleanup) {
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException e) {
                // Log de erro, mas não interrompe a limpeza dos demais arquivos
                e.printStackTrace();
            }
        }
    }
}
