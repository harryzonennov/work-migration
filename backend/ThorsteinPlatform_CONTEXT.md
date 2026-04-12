
# ThorsteinPlatform Migration Context

## Original Project Info
- Project: ThorsteinPlatform
- Java Version: Java 8
- Spring Version: Spring 4.x (XML config)
- Hibernate: 5.x
- Purpose: Platform foundation - User, Auth, Permission

## Key Components to Migrate
### Entities (map to new package: common/)
- LogonUser.java          → common/model/LogonUser.java
- Role.java               → common/model/Role.java
- UserRole.java           → common/model/UserRole.java
- LogonUserOrgReference.java → common/model/LogonUserOrgReference.java

### APIs to Migrate
- POST /login              → POST /api/v1/auth/login
- POST /logout             → POST /api/v1/auth/logout
- GET  /user/list          → GET  /api/v1/common/users
- POST /user/create        → POST /api/v1/common/users
- GET  /role/list          → GET  /api/v1/common/roles

### Old Auth Mechanism
- Session-based authentication (XML config)
- Custom filter: AuthenticationFilter.java
- → REPLACE WITH: JWT + Spring Security 6

### DB Tables to Migrate
- LogonUser              → platform.LogonUser
- Role                   → platform.Role
- UserRole               → platform.UserRole
- LogonUserOrgReference  → platform.LogonUserOrgReference

### Known Issues in Old Code
- Passwords stored as MD5 (MUST upgrade to BCrypt)
- Session timeout hardcoded to 30 min
- No token refresh mechanism
- XML-based Spring config (convert to annotation)

## Migration Priority: HIGH (Other modules depend on this)
