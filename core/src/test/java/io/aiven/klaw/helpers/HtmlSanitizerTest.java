package io.aiven.klaw.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Tests for HtmlSanitizer to ensure XSS protection and proper element/attribute filtering. */
class HtmlSanitizerTest {

  @Test
  void testSanitizeNullInput() {
    assertThat(HtmlSanitizer.sanitize(null)).isNull();
  }

  @Test
  void testSanitizeEmptyInput() {
    assertThat(HtmlSanitizer.sanitize("")).isEmpty();
  }

  @Test
  void testSanitizePlainText() {
    String input = "This is plain text without HTML";
    assertThat(HtmlSanitizer.sanitize(input)).isEqualTo(input);
  }

  @Test
  void testAllowedElements() {
    String input =
        "<p>Paragraph</p><br><strong>Bold</strong><em>Italic</em><u>Underline</u>"
            + "<h1>H1</h1><h2>H2</h2><ul><li>Item</li></ul><ol><li>Ordered</li></ol>"
            + "<blockquote>Quote</blockquote><code>code</code><pre>pre</pre>";
    String result = HtmlSanitizer.sanitize(input);
    assertThat(result).contains("<p>Paragraph</p>");
    assertThat(result).contains("br"); // OWASP sanitizer converts <br> to <br />
    assertThat(result).contains("<strong>Bold</strong>");
    assertThat(result).contains("<em>Italic</em>");
    assertThat(result).contains("<h1>H1</h1>");
    assertThat(result).contains("<ul>");
    assertThat(result).contains("<li>Item</li>");
  }

  @Test
  void testAllowedAttributes() {
    String input =
        "<a href=\"https://example.com\">Link</a>"
            + "<img src=\"https://example.com/image.jpg\" alt=\"Image\" title=\"Title\">";
    String result = HtmlSanitizer.sanitize(input);
    assertThat(result).contains("href=\"https://example.com\"");
    assertThat(result).contains("rel=\"nofollow\"");
    assertThat(result).contains("src=\"https://example.com/image.jpg\"");
    assertThat(result).contains("alt=\"Image\"");
    // HTTP and HTTPS protocols are both allowed
    assertThat(HtmlSanitizer.sanitize("<a href=\"http://example.com\">Link</a>"))
        .contains("http://example.com");
  }

  @Test
  void testBlockedDangerousElements() {
    // Script tags
    String result1 = HtmlSanitizer.sanitize("<script>alert('XSS')</script>");
    assertThat(result1).doesNotContain("<script>").doesNotContain("alert");

    // Iframes and embeds
    assertThat(HtmlSanitizer.sanitize("<iframe src=\"evil.com\"></iframe>"))
        .doesNotContain("<iframe>");
    assertThat(HtmlSanitizer.sanitize("<object data=\"evil.swf\"></object>"))
        .doesNotContain("<object>");
    assertThat(HtmlSanitizer.sanitize("<embed src=\"evil.swf\">")).doesNotContain("<embed>");
  }

  @Test
  void testBlockedDangerousAttributes() {
    // Event handlers
    assertThat(HtmlSanitizer.sanitize("<p onclick=\"alert('XSS')\">Click</p>"))
        .contains("<p>Click</p>")
        .doesNotContain("onclick");
    assertThat(HtmlSanitizer.sanitize("<img src=\"x\" onerror=\"alert('XSS')\">"))
        .doesNotContain("onerror");

    // Dangerous protocols
    assertThat(HtmlSanitizer.sanitize("<a href=\"javascript:alert('XSS')\">Click</a>"))
        .doesNotContain("javascript:");
    assertThat(
            HtmlSanitizer.sanitize(
                "<a href=\"data:text/html,<script>alert('XSS')</script>\">Click</a>"))
        .doesNotContain("data:");

    // Style attribute
    assertThat(HtmlSanitizer.sanitize("<p style=\"color:red\">Text</p>"))
        .contains("<p>Text</p>")
        .doesNotContain("style");
  }

  @Test
  void testComplexScenarios() {
    // Mixed allowed and blocked
    String mixed =
        "<p>Safe text</p><script>alert('XSS')</script><strong>Bold</strong><iframe></iframe>";
    String result1 = HtmlSanitizer.sanitize(mixed);
    assertThat(result1).contains("<p>Safe text</p>").contains("<strong>Bold</strong>");
    assertThat(result1).doesNotContain("<script>").doesNotContain("<iframe>");

    // Nested elements
    String nested = "<p><strong><em>Nested formatting</em></strong></p>";
    String result2 = HtmlSanitizer.sanitize(nested);
    assertThat(result2).contains("<p>").contains("<strong>").contains("<em>Nested formatting</em>");

    // Real-world documentation example
    String doc =
        "<h1>Topic Documentation</h1>"
            + "<p>This topic is used for <strong>important</strong> data processing.</p>"
            + "<ul><li>Feature 1</li><li>Feature 2</li></ul>"
            + "<p>See <a href=\"https://docs.example.com\">documentation</a> for more info.</p>"
            + "<pre><code>kafka-topics --create --topic mytopic</code></pre>";
    String result3 = HtmlSanitizer.sanitize(doc);
    assertThat(result3)
        .contains("<h1>Topic Documentation</h1>")
        .contains("<strong>important</strong>")
        .contains("<ul>")
        .contains("<a href=\"https://docs.example.com\"")
        .contains("<pre>")
        .contains("<code>")
        .doesNotContain("<script>");
  }
}
