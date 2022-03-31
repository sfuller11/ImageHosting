package com.example.ImageHosting.controller;

import com.example.ImageHosting.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;


@RestController
public class FileController {

    private final FileService fileService;

    private static long get64LeastSignificantBitsForVersion1() {
        Random random = new Random();
        long random63BitLong = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
        long variant3BitFlag = 0x8000000000000000L;
        return random63BitLong + variant3BitFlag;
    }


    @PostMapping("/images")
    public String uploadImage(@RequestParam String fileName, @RequestParam MultipartFile body) throws IOException {
        String UUID = String.valueOf(get64LeastSignificantBitsForVersion1());

        S3Client client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket("vlonebucket17")
                .key(UUID)
                .build();

        var requestBody = RequestBody.fromInputStream(body.getInputStream(), body.getSize());

        client.putObject(request, requestBody);

        return "Success: " + UUID;

    }

    @GetMapping("/images")
    public ResponseEntity<?> downloadImage(@RequestParam String fileName) throws IOException {
        S3Client client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket("vlonebucket17")
                .key(fileName)
                .build();


        var response = client.getObject(request);
        InputStreamResource body = new InputStreamResource(response);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION)
               .body(body);
    }

    @GetMapping("/images/list")
    public String listImage() throws IOException {
        S3Client client = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();

        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket("vlonebucket17")
                .build();

        ListObjectsResponse res = client.listObjects(request);
        List<S3Object> objects = res.contents();
        List result = new ArrayList();

        for (ListIterator iterVals = objects.listIterator(); iterVals.hasNext(); ) {
            S3Object myValue = (S3Object) iterVals.next();
            result.add(" \n Name: " + myValue.key());
        }


        return result.toString();
    }






    @Autowired
    public FileController(FileService fileService){
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file")MultipartFile multipartFile) {
        return ResponseEntity.ok(fileService.storeFile(multipartFile));
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable("filename") String filename, HttpServletRequest request) {
        UrlResource fileResource = fileService.getFile(filename);

        try{
            String contentType;
            contentType = request.getServletContext().getMimeType(fileResource.getFile().getAbsolutePath());

            if(contentType == null){
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION)
                    .body(fileResource);
        } catch (IOException e) {
            throw new RuntimeException("Could not determine file type.");
        }
    }
}
