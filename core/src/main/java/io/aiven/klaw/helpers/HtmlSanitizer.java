package io.aiven.klaw.helpers;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Utility class for HTML sanitization to prevent XSS attacks. Provides a shared sanitization policy
 * that allows safe HTML formatting for documentation while blocking dangerous content.
 */
public class HtmlSanitizer {

  // OWASP HTML Sanitizer policy - allows safe HTML formatting for documentation
  private static final PolicyFactory HTML_SANITIZER =
      new HtmlPolicyBuilder()
          .allowElements(
              "p",
              "br",
              "strong",
              "em",
              "u",
              "h1",
              "h2",
              "h3",
              "h4",
              "h5",
              "h6",
              "ul",
              "ol",
              "li",
              "blockquote",
              "code",
              "pre",
              "a",
              "img")
          .allowAttributes("href")
          .onElements("a")
          .allowAttributes("src", "alt", "title")
          .onElements("img")
          .allowUrlProtocols("http", "https")
          .requireRelNofollowOnLinks()
          .toFactory();

  /**
   * Sanitize HTML to prevent XSS attacks while preserving safe HTML formatting. Uses OWASP Java
   * HTML Sanitizer for robust, secure sanitization.
   *
   * @param html the HTML string to sanitize
   * @return sanitized HTML string, or empty string if input is null or empty
   */
  public static String sanitize(String html) {
    if (html == null || html.isEmpty()) {
      return html;
    }
    return HTML_SANITIZER.sanitize(html);
  }
}
