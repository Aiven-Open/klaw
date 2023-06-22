package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.requests.ResetEntityCache;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UtilControllerServiceTest {
  private UtilMethods utilMethods;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private MailUtils mailService;
  @Mock private UserDetails userDetails;
  @Mock private ManageDatabase manageDatabase;
  @Mock private HandleDbRequestsJdbc handleDbRequests;

  private UtilControllerService utilControllerService;
  private UserInfo userInfo;

  @BeforeEach
  void setUp() {
    utilMethods = new UtilMethods();
    utilControllerService = new UtilControllerService();
    ReflectionTestUtils.setField(utilControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(utilControllerService, "mailService", mailService);
    userInfo = utilMethods.getUserInfoMockDao();
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  @Test
  public void resetCache() {
    ResetEntityCache resetEntityCache = utilMethods.getResetEntityCache();
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn("anonymousUser");
    ApiResponse apiResponse = utilControllerService.resetCache(resetEntityCache);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void resetCacheNotAuthorized() {
    ResetEntityCache resetEntityCache = utilMethods.getResetEntityCache();
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn("testuser");
    ApiResponse apiResponse = utilControllerService.resetCache(resetEntityCache);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  public UserDetails userDetails(String username, String password) {

    return new UserDetails() {
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
      }

      @Override
      public String getPassword() {
        return password;
      }

      @Override
      public String getUsername() {
        return username;
      }

      @Override
      public boolean isAccountNonExpired() {
        return false;
      }

      @Override
      public boolean isAccountNonLocked() {
        return false;
      }

      @Override
      public boolean isCredentialsNonExpired() {
        return false;
      }

      @Override
      public boolean isEnabled() {
        return false;
      }
    };
  }
}
