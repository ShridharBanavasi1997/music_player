package com.cumulations.music_player.rest_controller;

import com.cumulations.music_player.components.CommonFunctions;
import com.cumulations.music_player.components.JwtComponent;
import com.cumulations.music_player.dto.MessageResponse;
import com.cumulations.music_player.dto.entity.RefreshToken;
import com.cumulations.music_player.dto.entity.Role;
import com.cumulations.music_player.dto.entity.UserInfo;
import com.cumulations.music_player.dto.exception.TokenRefreshException;
import com.cumulations.music_player.dto.request_body.Credentials;
import com.cumulations.music_player.dto.response_body.LoginResponse;
import com.cumulations.music_player.repo.RoleRepo;
import com.cumulations.music_player.repo.UserInfoRepo;
import com.cumulations.music_player.service.RefreshTokenService;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Routh", description = "Endpoints for authentication")
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtComponent jwtComponent;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CommonFunctions commonFunctions;


    @Operation(summary = "login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login Success", content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            }),
            @ApiResponse(responseCode = "401", description = "Login Fail", content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            })})
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtComponent.generateJwtToken(authentication);

            User userInfo = (User) authentication.getPrincipal();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfo.getUsername());
            String jwtRefreshToken = jwtComponent.generateJwtRefreshToken(refreshToken.getToken());
            return new ResponseEntity(new LoginResponse(userInfo.getUsername(), accessToken, jwtRefreshToken, jwtComponent.getIssuedTime(accessToken), jwtComponent.getExpiredAT(accessToken)), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity(new MessageResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "signup", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "user info", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserInfo.class)
    ),
            required = true))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "UserCreated", content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfo.class),
                            examples = {@ExampleObject("{\"id\": \"string\",\"firstName\": \"string\",\"lastName\": \"string\",\"email\": \"string\"}")}
                    )
            })})
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserInfo userInfo) {
        try {
            if (userInfoRepo.existsByEmail(userInfo.getEmail()))
                return new ResponseEntity<String>("User Already Exist with this Email", HttpStatus.BAD_REQUEST);

            UserInfo addedUser = new UserInfo(userInfo.getFirstName(), userInfo.getLastName(), userInfo.getEmail(), passwordEncoder.encode(userInfo.getPassword()));

            Role role = roleRepo.findByName("ROLE_USER").get();

            addedUser.addRole(role);
            userInfoRepo.saveUserInfo(addedUser);
            userInfo.setRoles(null);
            return new ResponseEntity<UserInfo>(addedUser, HttpStatus.CREATED);
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage()));
        }
    }

    @Operation(summary = "logout")
    @ApiResponses(value = {
            @ApiResponse(description = "Logout success", responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":200,\"message\":\"success\"}")}
                    )),
            @ApiResponse(description = "Logout Fail", responseCode = "500",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = {@ExampleObject("{\"status\":500,\"message\":\"Error Message\"}")}
                    ))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        try {
            String jwtToken = commonFunctions.parseJwt(request);
            if (jwtToken == null || !jwtComponent.validateJwtToken(jwtToken)) {
                throw new RuntimeException("Not a valid token");
            }
            String userEmail = jwtComponent.getSubjectFromJwtToken(jwtToken);
            if ((userEmail == null) && (userEmail.length() == 0)) {
                throw new RuntimeException("Not a valid token");
            }

            int value = refreshTokenService.deleteByEmail(userEmail);
            return ResponseEntity.ok()
                    .body(new MessageResponse(HttpStatus.OK.value(), "success"));

        } catch (Exception e) {
            logger.error(e.toString());
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @Operation(summary = "refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            }),
            @ApiResponse(responseCode = "500", description = "Fail", content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            })})
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String jwtRefreshToken = commonFunctions.parseJwt(request);
            if (jwtRefreshToken == null || !jwtComponent.validateJwtToken(jwtRefreshToken)) {
                throw new RuntimeException("Not a valid token");
            }
            String refreshToken = jwtComponent.getSubjectFromJwtToken(jwtRefreshToken);
            if ((refreshToken == null) && (refreshToken.length() == 0)) {
                throw new RuntimeException("Not a valid token");
            }

            return refreshTokenService.findByToken(refreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUserInfo)
                    .map(user -> {
                        String accessToken = jwtComponent.generateJwtToken(user.getEmail());

                        return new ResponseEntity(new LoginResponse(user.getEmail(), accessToken, jwtRefreshToken, jwtComponent.getIssuedTime(accessToken), jwtComponent.getExpiredAT(accessToken)), HttpStatus.OK);
                    })
                    .orElseThrow(() -> new TokenRefreshException(refreshToken,
                            "Refresh token is not in database!"));


        } catch (Exception e) {
            logger.error(e.toString());
            return ResponseEntity.internalServerError().body(new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
}
