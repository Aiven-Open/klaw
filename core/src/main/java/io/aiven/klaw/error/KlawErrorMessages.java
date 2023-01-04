package io.aiven.klaw.error;

public class KlawErrorMessages {

  public static final String ACTIVE_DIRECTORY_ERR_CODE_101 = "AD101";
  public static final String ACTIVE_DIRECTORY_ERR_CODE_102 = "AD102";
  public static final String ACTIVE_DIRECTORY_ERR_CODE_103 = "AD103";
  public static final String ACTIVE_DIRECTORY_ERR_CODE_104 = "AD104";

  public static final String AD_ERROR_101_NO_MATCHING_ROLE =
      "No matching role is configured. Please make sure only one matching role"
          + " from Klaw is configured in AD. Denying login !!";

  public static final String AD_ERROR_102_NO_MATCHING_TEAM =
      "No matching team is configured. Please make sure only one matching team"
          + " from Klaw is configured in AD. Denying login !!";

  public static final String AD_ERROR_103_MULTIPLE_MATCHING_ROLE =
      "Multiple matching roles are configured. Please make sure only one matching role"
          + " from Klaw is configured in AD. Denying login !!";

  public static final String AD_ERROR_104_MULTIPLE_MATCHING_TEAM =
      "Multiple matching teams are configured. Please make sure only one matching team"
          + " from Klaw is configured in AD. Denying login !!";
}
