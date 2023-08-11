package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchemaDetailsPerEnv {
  // This is the id of the Schema as it relates to this topic
  @NotNull private int id;
  // This is the version of this Schema that is returned for this topic
  @NotNull private int version;
  // If a newer version of the schema exsists its version will be here
  @NotNull private int nextVersion;
  // If an older version of the schema exsists its version will be here
  @NotNull private int prevVersion;
  // This is the compatibility set in the Schema Registry e.g. NOT_SET/BACKWARD/FORWARD/FULL/NONE
  // This can return 'Couldn't retrieve' if it is unable to retrieve that information from the
  // schema registry. Or there is an issue with the data saved in the DB.
  @NotNull private String compatibility;
  // The content is the actual schema
  @NotNull private String content;
  // This is the Schema Registry env that the schema relates to
  @NotNull private String env;
  // lets the UI know if it should show a next button
  @NotNull private boolean showNext;
  // lets the UI know if it should show a next button
  @NotNull private boolean showPrev;
  // simple boolean to identify if this is the latest schema on the topic for that environment.
  @NotNull private boolean latest;
  // Indicates if this schema env is restricted to only allow new schemas through promotion.
  @NotNull private boolean promoteOnly;
}
