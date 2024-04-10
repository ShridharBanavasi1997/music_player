package com.cumulations.music_player.rest_controller;

import com.cumulations.music_player.dto.MessageResponse;
import com.cumulations.music_player.dto.entity.UserInfo;
import com.cumulations.music_player.repo.RoleRepo;
import com.cumulations.music_player.repo.UserInfoRepo;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Routh", description = "Endpoints for admins api")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class AdminController {

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private RoleRepo roleRepo;

    @GetMapping("/all_user")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "All user information")
    @ApiResponse(
            description = "success",
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(
                                  implementation = UserInfo.class
                            )
                    )
            )
    )
    public ResponseEntity<?> allUser(){
        return ResponseEntity.ok().body(userInfoRepo.findAll());
    }

    @PostMapping("/creators/add")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "make user as creator")
    @ApiResponses(value = {
            @ApiResponse(
                    description = "success",
                    responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfo.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fail",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":404,\"message\":\"User Not Found\"}")}
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Fail",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
    public ResponseEntity<?> makeAsCreator(@RequestParam(name="userId") String userId){
        try{
            Optional<UserInfo> optionalUserInfo=userInfoRepo.findById(userId);
            if(!optionalUserInfo.isPresent())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(HttpStatus.NOT_FOUND.value(),"User Not Found" ));
            UserInfo userInfo = optionalUserInfo.get();
            userInfo.addRole(roleRepo.findByName("ROLE_CREATOR").get());
            userInfoRepo.save(userInfo);
            return ResponseEntity.ok().body(userInfo);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }



}
