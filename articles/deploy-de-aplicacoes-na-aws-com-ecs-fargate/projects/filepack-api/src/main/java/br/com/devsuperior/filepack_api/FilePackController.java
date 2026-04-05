package br.com.devsuperior.filepack_api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/filepack")
public class FilePackController {

    private final FilePackService filePackService;

    public FilePackController(FilePackService filePackService) {
        this.filePackService = filePackService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/zip")
    public ResponseEntity<Resource> createEncryptedZip(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("password") String password) throws IOException {

        ZipResourceModel zipResource = filePackService.createEncryptedZipResource(files, password);

        try {
            // Lê o arquivo ZIP para memória antes de deletar
            Path zipPath = zipResource.getResource().getFile().toPath();
            byte[] zipBytes = Files.readAllBytes(zipPath);
            
            // Cria um recurso em memória
            ByteArrayResource resource = new ByteArrayResource(zipBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipResource.getFilename() + "\"")
                    .contentLength(zipBytes.length)
                    .body(resource);

        } finally {
            // Agora é seguro limpar os arquivos temporários
            zipResource.cleanup();
        }
    }
}
