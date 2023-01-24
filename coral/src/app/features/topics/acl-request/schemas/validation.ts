// Source: https://github.com/sindresorhus/ip-regex/blob/main/index.js
const ipv4 =
  "(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)){3}";
const ipv6segment = "[a-fA-F\\d]{1,4}";
const ipv6 = `
(?:
(?:${ipv6segment}:){7}(?:${ipv6segment}|:)|                                            // 1:2:3:4:5:6:7::  1:2:3:4:5:6:7:8
(?:${ipv6segment}:){6}(?:${ipv4}|:${ipv6segment}|:)|                                   // 1:2:3:4:5:6::    1:2:3:4:5:6::8   1:2:3:4:5:6::8  1:2:3:4:5:6::1.2.3.4
(?:${ipv6segment}:){5}(?::${ipv4}|(?::${ipv6segment}){1,2}|:)|                         // 1:2:3:4:5::      1:2:3:4:5::7:8   1:2:3:4:5::8    1:2:3:4:5::7:1.2.3.4
(?:${ipv6segment}:){4}(?:(?::${ipv6segment}){0,1}:${ipv4}|(?::${ipv6segment}){1,3}|:)| // 1:2:3:4::        1:2:3:4::6:7:8   1:2:3:4::8      1:2:3:4::6:7:1.2.3.4
(?:${ipv6segment}:){3}(?:(?::${ipv6segment}){0,2}:${ipv4}|(?::${ipv6segment}){1,4}|:)| // 1:2:3::          1:2:3::5:6:7:8   1:2:3::8        1:2:3::5:6:7:1.2.3.4
(?:${ipv6segment}:){2}(?:(?::${ipv6segment}){0,3}:${ipv4}|(?::${ipv6segment}){1,5}|:)| // 1:2::            1:2::4:5:6:7:8   1:2::8          1:2::4:5:6:7:1.2.3.4
(?:${ipv6segment}:){1}(?:(?::${ipv6segment}){0,4}:${ipv4}|(?::${ipv6segment}){1,6}|:)| // 1::              1::3:4:5:6:7:8   1::8            1::3:4:5:6:7:1.2.3.4
(?::(?:(?::${ipv6segment}){0,5}:${ipv4}|(?::${ipv6segment}){1,7}|:))                   // ::2:3:4:5:6:7:8  ::2:3:4:5:6:7:8  ::8             ::1.2.3.4
)(?:%[0-9a-zA-Z]{1,})?                                                                 // %eth0            %1
`
  .replace(/\s*\/\/.*$/gm, "")
  .replace(/\n/g, "")
  .trim();

const isIpRegex = new RegExp(`(?:^${ipv4}$)|(?:^${ipv6}$)`);

const validateAclPrincipleValue = (value: string[] | undefined) => {
  return value !== undefined && value.length >= 1;
};

export { isIpRegex, validateAclPrincipleValue };
