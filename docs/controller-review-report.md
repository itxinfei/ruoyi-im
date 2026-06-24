# RuoYi-IM Controller Layer Code Review Report

**Review Date**: 2026-06-24
**Scope**: All 63 controller files under `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/`
**Reviewed Files**: 26 priority controllers (security-critical + admin)

---

## BLOCKER (4 issues)

### B-1. Plaintext passwords in URL query string
- **File**: `ImUserController.java:256-268`
- **Category**: Security
- **Description**: `changePassword()` uses `@RequestParam` for oldPassword and newPassword. Passwords appear in URL, server access logs, browser history, and proxy logs.
- **Fix**: Use `@RequestBody` with a dedicated DTO like `ChangePasswordRequest`.

### B-2. Unprivileged user can disable any account
- **File**: `ImUserController.java:212-217`
- **Category**: Security
- **Description**: `changeStatus()` performs no role check. Any authenticated user can disable another user's account by passing their ID.
- **Fix**: Add admin role verification before the status update.

### B-3. Sensitive word reload endpoint is unprotected
- **File**: `ImSensitiveWordController.java:87-91`
- **Category**: Security
- **Description**: `POST /api/im/sensitiveWord/reload` lacks any authorization annotation. Any logged-in user can force-reload the dictionary.
- **Fix**: Add `@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")`.

### B-4. File metadata leak via ID enumeration
- **File**: `ImFileController.java:279-283`
- **Category**: Security
- **Description**: `GET /api/im/file/{fileId}` returns file info to any authenticated caller without verifying ownership or group membership.
- **Fix**: Validate that the requester owns the file or belongs to the relevant conversation/group.

---

## CRITICAL (11 issues)

### C-1. Header injection via unsanitized filename
- **File**: `ImFileController.java:142, 228`
- **Category**: Security
- **Description**: Filename is concatenated directly into `Content-Disposition` header. A crafted filename with `"` or CRLF characters could inject headers.
- **Fix**: Encode filename with RFC 6266 or `URLEncoder.encode()`.

### C-2. Unbounded pageSize in message list
- **File**: `ImMessageController.java:78-93`
- **Category**: Security / Performance
- **Description**: `limit` and `pageSize` params accept any integer. Requesting millions of records could exhaust memory.
- **Fix**: Clamp with `Math.min(effectiveLimit, 100)`.

### C-3. Unbounded limit in recent files query
- **File**: `ImCloudDriveController.java:348-360`
- **Category**: Security / Performance
- **Description**: `limit` param has no upper bound. A single request could load the entire file table.
- **Fix**: Add `@Max(100)` or clamp in controller.

### C-4. Unbounded thumbnail dimensions
- **File**: `ImFilePreviewController.java:58-66`
- **Category**: Security / Performance
- **Description**: `width` and `height` accept any integer. Requesting a 999999x999999 thumbnail could exhaust server memory/CPU.
- **Fix**: Validate `width <= 2000 && height <= 2000`.

### C-5. No size limit on batch delete lists
- **File**: `ImFileController.java:310-317`, `ImMessageController.java:116`
- **Category**: Security / Performance
- **Description**: `batchDeleteFiles()` and `batchDeleteMessages()` accept unbounded lists. An attacker could send thousands of IDs in one request.
- **Fix**: Add `if (fileIds.size() > 100) return Result.fail("单次最多100条")`.

### C-6. Fastjson JSONObject deserialization risk
- **File**: `ImGroupBotWebhookController.java:43`
- **Category**: Security
- **Description**: Webhook payload uses `com.alibaba.fastjson.JSONObject`. Fastjson has a history of critical deserialization RCE vulnerabilities.
- **Fix**: Migrate to Jackson `ObjectNode` or at minimum configure fastjson with `SafeMode`.

### C-7. Webhook endpoint has no rate limiting
- **File**: `ImGroupBotWebhookController.java:38-54`
- **Category**: Security
- **Description**: `POST /api/im/bot/webhook/{token}` is publicly accessible with no rate limit. Enables brute-force token guessing and DoS.
- **Fix**: Add rate limiting via Spring `@RateLimiter` or API gateway.

### C-8. No batch size limit on group member operations
- **File**: `ImGroupController.java:158-163`, `ImGroupController.java:177-182`
- **Category**: Security / Performance
- **Description**: `addMembers()` and `removeMembers()` accept unbounded `List<Long>`. Could be used to add/remove thousands of members atomically.
- **Fix**: Validate `userIds.size() <= 500` (or the group member limit).

### C-9. Mapper injected directly into controller
- **File**: `ImMessageController.java:50-51`, `ImUserAdminController.java:34`
- **Category**: Standards / Architecture
- **Description**: `ImMessageMapper` and `ImUserMapper` are injected into controllers, bypassing the service layer. This violates layered architecture and enables uncontrolled DB access.
- **Fix**: Move all mapper calls to the service layer.

### C-10. QR code content is predictable
- **File**: `ImGroupController.java:275`
- **Category**: Security
- **Description**: QR content is `"ruoyi-im://join-group?" + id` — trivially guessable. Anyone can generate join QR codes for any group.
- **Fix**: Include a signed token or random nonce: `"ruoyi-im://join-group?token=" + signedToken`.

### C-11. No protection against admin self-deletion
- **File**: `ImUserAdminController.java:166-174`
- **Category**: Security / Logic
- **Description**: Admin can delete their own account via `DELETE /api/admin/users/{id}`, which could lock out the only admin.
- **Fix**: Add `if (id.equals(currentUserId)) return Result.fail("不能删除自己")`.

---

## MAJOR (14 issues)

### M-1. Generic Exception catching swallows root cause
- **Files**: `ImAuthController.java:184`, `ImCloudDriveController.java` (all methods), `ImVideoCallController.java` (all methods)
- **Category**: Error Handling
- **Description**: `catch (Exception e)` blocks catch everything including NPE and IllegalArgumentException. The original error context is lost; callers only see a generic message like "创建失败".
- **Fix**: Catch specific exceptions (BusinessException, etc.) and rethrow unexpected ones.

### M-2. RuntimeException thrown instead of BusinessException
- **File**: `ImGroupController.java:287`, `ImFileController.java:358`
- **Category**: Standards
- **Description**: `throw new RuntimeException("生成二维码失败", e)` bypasses the global exception handler that converts BusinessException to proper HTTP responses. Clients receive HTTP 500 instead of a structured error.
- **Fix**: Replace with `throw new BusinessException(ApiErrorCode.QR_GENERATION_FAILED)`.

### M-3. Missing @Valid on request body
- **Files**: `ImGroupController.java:160` (userIds), `ImSensitiveWordController.java:38`, `ImGroupAdminController.java:136`
- **Category**: Input Validation
- **Description**: Request bodies lack `@Valid` annotation. DTOs with `@NotNull`/`@NotBlank` constraints are not enforced.
- **Fix**: Add `@Valid` before `@RequestBody` parameters.

### M-4. No @ApiOperation annotations
- **Files**: All controllers
- **Category**: Standards / Documentation
- **Description**: Zero Swagger/OpenAPI annotations across the entire controller layer. API documentation cannot be auto-generated.
- **Fix**: Add `@ApiOperation` to every endpoint method and `@Api` to every controller class.

### M-5. Inconsistent return type for getCallInfo
- **File**: `ImVideoCallController.java:149`
- **Category**: Standards
- **Description**: `getCallInfo()` returns `Result<Object>` — untyped. Same issue with `getUserActiveCall()` returning `Result<Object>`.
- **Fix**: Define a proper VO class (e.g., `VideoCallInfoVO`).

### M-6. NumberFormatException not handled in batch user query
- **File**: `ImUserController.java:125-129`
- **Category**: Error Handling
- **Description**: `Long::valueOf` in the stream will throw `NumberFormatException` if any ID in the comma-separated string is not a valid number. No try-catch wraps this.
- **Fix**: Wrap parsing in try-catch and return a meaningful error.

### M-7. Form data validation missing in approval creation
- **File**: `ImApprovalController.java:46-51`
- **Category**: Input Validation
- **Description**: `formData` is `Map<String, Object>` with no schema validation. Any JSON object is accepted regardless of the template's expected fields.
- **Fix**: Validate formData keys/values against the template definition in the service layer.

### M-8. Signal data accepted as raw string
- **File**: `ImVideoCallController.java:186-199`
- **Category**: Input Validation
- **Description**: `signalData` is accepted as `@RequestBody String` with no validation. Malformed WebRTC signaling data could crash downstream parsers.
- **Fix**: Validate JSON structure or use a typed DTO.

### M-9. isImageFile missing dot-index safety check
- **File**: `ImFileController.java:451-456`
- **Category**: Logic
- **Description**: `fileName.lastIndexOf('.')` could return -1 for extensionless files, causing `substring(0)` to return the full filename. Then `.equals("jpg")` comparison would fail silently but the logic is fragile.
- **Fix**: Check `dotIndex > 0` before calling `substring()`, matching the pattern already used in `getContentType()`.

### M-10. BeanUtils.copyProperties used for admin group update
- **File**: `ImGroupAdminController.java:142`
- **Category**: Security
- **Description**: `BeanUtils.copyProperties(request, existGroup)` copies ALL matching fields from the request to the entity. If the request DTO ever gains a `role` or `isDeleted` field, it could be exploited to modify protected columns.
- **Fix**: Use explicit field mapping or a MapStruct mapper.

### M-11. Statistics endpoint leaks storage data
- **File**: `ImFileController.java:342-346`
- **Category**: Security
- **Description**: `GET /api/im/file/statistics` returns storage statistics without checking who is calling. Any authenticated user can see server-wide storage usage.
- **Fix**: Scope to the current user's statistics or require admin role.

### M-12. Inconsistent role check approach
- **Files**: `ImUserController.java:84-87` vs `ImGroupAdminController.java:28`
- **Category**: Standards
- **Description**: Some controllers use manual `SecurityUtils.getLoginUserRole()` checks while others use `@PreAuthorize`. The project should standardize on one approach.
- **Fix**: Standardize on `@PreAuthorize` for declarative security.

### M-13. AI chat endpoint lacks rate limiting
- **File**: `ImAIController.java:38-42`
- **Category**: Security / Performance
- **Description**: `POST /api/im/ai/chat` calls an LLM service with no rate limit. A single user could generate unlimited API calls, causing significant cost.
- **Fix**: Add per-user rate limiting (e.g., 10 requests/minute).

### M-14. File download by path does not track downloader
- **File**: `ImFileController.java:176-248`
- **Category**: Logic
- **Description**: `downloadFileByPath()` authenticates the user but never records the download (unlike `downloadFile()` which calls `imFileService.downloadFile()`). Download analytics are incomplete.
- **Fix**: Call `imFileService.recordAccess()` after successful file read.

---

## MINOR (6 issues)

### m-1. Duplicate Javadoc block
- **File**: `ImAuthController.java:189-203`
- **Category**: Standards
- **Description**: Two Javadoc blocks exist for `validateToken()` — one orphaned at line 189 and the actual one at line 198.
- **Fix**: Remove the orphaned block.

### m-2. Unused import
- **File**: `ImMessageController.java:18` (ImWebSocketEndpoint), `ImUserController.java:15-16` (Authentication, SecurityContextHolder)
- **Category**: Standards
- **Description**: Imports that are never referenced in the code.
- **Fix**: Remove unused imports.

### m-3. Missing @ApiParam on PathVariable/RequestParam
- **Files**: All controllers
- **Category**: Standards / Documentation
- **Description**: No `@ApiParam` annotations on any parameters. API docs will lack parameter descriptions.
- **Fix**: Add `@ApiParam(description = "...")` to all endpoint parameters.

### m-4. Logging user input without sanitization
- **File**: `ImAuthController.java:58`
- **Category**: Security
- **Description**: `logger.info("收到登录请求 - 用户名: {}", request.getUsername())` — if username contains format specifiers or special chars, it could cause log injection.
- **Fix**: Sanitize or limit logged user input length.

### m-5. Inner DTO class in controller
- **File**: `ImGroupsController.java:107-117`, `ImContactController.java:324-360`
- **Category**: Standards
- **Description**: `RoleUpdateRequest`, `GroupRenameRequest`, and `MoveToGroupRequest` are defined as static inner classes inside controllers. They should live in the `dto` package for reuse and testability.
- **Fix**: Move to `com.ruoyi.im.dto.group.RoleUpdateRequest` etc.

### m-6. @DateTimeFormat pattern inconsistency
- **Files**: `ImMessageAdminController.java:59` vs `ImAttendanceController.java:106`
- **Category**: Standards
- **Description**: One uses `yyyy-MM-dd HH:mm:ss`, the other uses `yyyy-MM-dd`. The project should define a standard date/time format constant.
- **Fix**: Extract format strings to a constants class.

---

## Summary Table

| Severity | Count | Key Themes |
|----------|-------|------------|
| BLOCKER  | 4     | Credential exposure, missing authz on state-changing endpoints |
| CRITICAL | 11    | Header injection, unbounded params, architecture violations |
| MAJOR    | 14    | Error swallowing, missing validation, inconsistent patterns |
| MINOR    | 6     | Documentation gaps, dead code, style inconsistencies |
| **Total**| **35**| |

## Top 5 Actionable Priorities

1. **Fix password exposure** (B-1): Change `changePassword` to use `@RequestBody` DTO immediately.
2. **Add permission checks** (B-2, B-3, B-4): Secure `changeStatus`, `reload`, and `getFileById` endpoints.
3. **Cap all pagination params** (C-2, C-3, C-4, C-5): Add upper bounds to prevent resource exhaustion.
4. **Replace fastjson** (C-6): Migrate webhook to Jackson to eliminate deserialization attack surface.
5. **Add batch size limits** (C-5, C-8): Prevent DoS via unbounded list parameters.
