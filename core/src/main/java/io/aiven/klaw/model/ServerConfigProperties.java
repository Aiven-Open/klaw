package io.aiven.klaw.model;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerConfigProperties {

  private String id;

  private String key;

  private String value;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServerConfigProperties that)) return false;

    if (!Objects.equals(id, that.id)) return false;
    return Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (key != null ? key.hashCode() : 0);
    return result;
  }
}
