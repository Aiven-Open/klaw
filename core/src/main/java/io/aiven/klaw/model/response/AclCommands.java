package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class AclCommands {
  private String result;
  private String aclCommandSsl;
  private String aclCommandPlaintext;
}
