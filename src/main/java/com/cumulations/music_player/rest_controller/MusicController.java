package com.cumulations.music_player.rest_controller;

import com.cumulations.music_player.dto.MessageResponse;
import com.cumulations.music_player.dto.entity.Album;
import com.cumulations.music_player.dto.entity.Music;
import com.cumulations.music_player.repo.AlbumRepo;
import com.cumulations.music_player.repo.MusicRepo;
import com.cumulations.music_player.repo.UserInfoRepo;
import com.cumulations.music_player.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/music")
@Tag(name = "Music Routh", description = "Endpoints for music api")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class MusicController {

    @Autowired
    private AlbumRepo albumRepo;

    @Autowired
    private MusicRepo musicRepo;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/album")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "create album")
    @ApiResponses(value = {
            @ApiResponse(
                    description = "success",
                    responseCode = "201",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Album.class)
                    )
            ),
    })
    public ResponseEntity<?> createAlbum(@RequestParam(name = "name") String name) {
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(albumRepo.save(new Album(name)));
    }

    @GetMapping("/albums")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "get all albums")
    @ApiResponses(value = {
            @ApiResponse(
                    description = "success",
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = Album.class)
                            )
                    )
            ),
    })
    public ResponseEntity<?> getAllAlbums() {
        return ResponseEntity.ok().body(albumRepo.findAll());
    }

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "create music")
    @ApiResponses(value = {
            @ApiResponse(
                    description = "success",
                    responseCode = "201",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":201,\"message\":\"success\"}")}
                    )
            ),
            @ApiResponse(
                    description = "fail",
                    responseCode = "400",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":400,\"message\":\"messages\"}")}
                    )
            ),
            @ApiResponse(
                    description = "fail",
                    responseCode = "500",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":500,\"message\":\"messages\"}")}
                    )
            )
    })
    public ResponseEntity<?> createMusic(@RequestParam(name = "title") String title, @RequestParam("music_file") MultipartFile musicFile, @RequestParam(name = "albumId", required = false) Integer albumId, @RequestParam(name = "metaData", required = false) Object metaData, Authentication authentication) {
        Music music = new Music();
        try {
            if (title == null)
                return ResponseEntity.badRequest().body(new MessageResponse(HttpStatus.BAD_REQUEST.value(), "Need title of the music"));

            music.setTitle(title);

            if (musicFile == null || !musicFile.getContentType().equals("audio/mpeg"))
                return ResponseEntity.badRequest().body(new MessageResponse(HttpStatus.BAD_REQUEST.value(), "Need music file"));

            String fileName = fileStorageService.storeFile(musicFile);

            String fileLocationUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/musics/download/")
                    .path(fileName)
                    .toUriString();
            music.setLocation(fileLocationUri);

            music.setMetaData(metaData);

            if (albumId != null && albumRepo.existsById(albumId)) {
                music.setAlbum(albumRepo.findById(albumId).get());
            }

            User user = (User) authentication.getPrincipal();
            music.setCreator(userInfoRepo.findByEmail(user.getUsername()).get());

            musicRepo.save(music);
            return ResponseEntity.status(HttpStatus.CREATED.value()).body(new MessageResponse(HttpStatus.CREATED.value(), "success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @GetMapping("/all")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "get music")
    @ApiResponses(value = {
            @ApiResponse(
                    description = "success",
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = Music.class)
                            )
                    )
            )
    })
    public ResponseEntity<?> getAllMusic() {
        try {
            return ResponseEntity.ok().body(musicRepo.findAll().stream().map(data -> {
                data.setCreator(null);
                return data;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error"));
        }
    }

    @GetMapping("/download/{fileName}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "get music by file name")
    @ApiResponses(value = {
            @ApiResponse(
                    description = "success",
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Resource.class)
                    )
            ),
            @ApiResponse(
                    description = "fail",
                    responseCode = "500",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getMusicByFileName(@PathVariable String fileName, HttpServletRequest request) {
        try {
            System.out.println("getMusicByFileName");

            Resource resource = fileStorageService.loadFileAsResource(fileName);
            System.out.println(resource.getFilename());
            String contentType =  request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if(contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error"));
        }
    }

    @GetMapping("/most-songs")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "get album with most songs")
    public ResponseEntity<Album> getAlbumWithMostSongs() {

        List<Album> albums = albumRepo.findAllWithSongsOrderedBySongCountDesc();
        if (!albums.isEmpty()) {
            return ResponseEntity.ok(albums.get(0));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
