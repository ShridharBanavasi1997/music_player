package com.cumulations.music_player.rest_controller;

import com.cumulations.music_player.components.CommonFunctions;
import com.cumulations.music_player.dto.MessageResponse;
import com.cumulations.music_player.dto.entity.UserInfo;
import com.cumulations.music_player.dto.request_body.PasswordResetRequestBody;
import com.cumulations.music_player.dto.request_body.UserUpdateRequestBody;
import com.cumulations.music_player.repo.UserInfoRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Routh", description = "Endpoint for user's API")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class UserController {

    @Autowired
    private CommonFunctions commonFunctions;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/info")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "User information")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "success",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfo.class),
                            examples = {@ExampleObject("{\"id\": \"string\",\"firstName\": \"string\",\"lastName\": \"string\",\"email\": \"string\"}")}
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "success",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            )
    })
    public ResponseEntity<?> getUser(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            return userInfoRepo.findByEmail(user.getUsername()).map(userInfo -> {
                userInfo.setRoles(null);
                return ResponseEntity.ok().body(userInfo);
            }).orElseThrow(() -> {
                throw new RuntimeException("User Not Found");
            });
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }


    @PutMapping("/update")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "User update", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "user data",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserUpdateRequestBody.class)
            )
    )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "success",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfo.class),
                            examples = {@ExampleObject("{\"id\": \"string\",\"firstName\": \"string\",\"lastName\": \"string\",\"email\": \"string\"}")}
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
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequestBody userUpdateRequestBody, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            return userInfoRepo.findByEmail(user.getUsername()).map(userInfo -> {
                if (userUpdateRequestBody.getFirstName() != null)
                    userInfo.setFirstName(userUpdateRequestBody.getFirstName());
                if (userUpdateRequestBody.getLastName() != null)
                    userInfo.setLastName(userUpdateRequestBody.getLastName());
                userInfoRepo.save(userInfo);
                userInfo.setRoles(null);
                return ResponseEntity.ok().body(userInfo);
            }).orElseThrow(() -> {
                throw new RuntimeException("User Not Found");
            });
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @PutMapping("/password/reset")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Password update", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "password data",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PasswordResetRequestBody.class)
            )
    )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "success",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":200,\"message\":\"success\"}")}
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Fail",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":400,\"message\":\"Old password is not correct\"}")}
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
    public ResponseEntity<?> updatePassword(@RequestBody PasswordResetRequestBody passwordResetRequestBody, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            return userInfoRepo.findByEmail(user.getUsername()).map(userInfo -> {
                if (!passwordEncoder.matches(passwordResetRequestBody.getOldPassword(), userInfo.getPassword()))
                    return ResponseEntity.badRequest().body(new MessageResponse(HttpStatus.BAD_REQUEST.value(), "Old password is not correct"));
                userInfo.setPassword(passwordEncoder.encode(passwordResetRequestBody.getNewPassword()));
                userInfoRepo.save(userInfo);
                return ResponseEntity.ok().body(new MessageResponse(HttpStatus.OK.value(), "success"));
            }).orElseThrow(() -> {
                throw new RuntimeException("User Not Found");
            });
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
}
