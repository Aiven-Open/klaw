package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class SchemaDetailsPerEnv {
  // This is the id of the Schema as it relates to this topic
  private int id;
  // This is the version of this Schema that is returned for this topic
  private int version;
  // If a newer version of the schema exsists its version will be here
  private int nextVersion;
  // If an older version of the schema exsists its version will be here
  private int prevVersion;
  // This is the compatibility set in the Schema Registry e.g. NOT_SET/BACKWARD/FORWARD/FULL/NONE
  // This can return 'Couldn't retrieve' if it is unable to retrieve that information from the
  // schema registry. Or there is an issue with the data saved in the DB.
  private String compatibility;
  // The content is the actual schema
  private String content;
  // This is the Schema Registry env that the schema relates to
  private String env;
  // lets the UI know if it should show a next button
  private boolean showNext;
  // lets the UI know if it should show a next button
  private boolean showPrev;
  // simple boolean to identify if this is the latest schema on the topic for that environment.
  private boolean latest;
}
