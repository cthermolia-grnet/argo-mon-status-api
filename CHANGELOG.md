# Changelog

---

All notable changes to this project will be documented in this file.

According to [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) , the `Unreleased` section serves the following purposes:

-   People can see what changes they might expect in upcoming releases.
-   At release time, you can move the `Unreleased` section changes into a new release version section.

## Types of changes

---

-   `Added` for new features.
-   `Changed` for changes in existing functionality.
-   `Removed` for now removed features.
-   `Fixed` for any bug fixes.
-   `Security` in case of vulnerabilities.
-   `Deprecated` for soon-to-be removed features.


## Unreleased
---

### Added

- [#1](https://github.com/ARGOeu/argo-mon-status-api/pull/1) ARGO-5136 Add Status Pages API and Implement /v1/encrypt & /v1/reports Endpoints
- [#3](https://github.com/ARGOeu/argo-mon-status-api/pull/3) ARGO-5145 Implement Endpoints for Status Page API (Status, Status Pages & CRUD Operations)
- [#5](https://github.com/ARGOeu/argo-mon-status-api/pull/5) ARGO-5147 argo-mon-status-api pom issue preventing jenkins deployment
- [#9](https://github.com/ARGOeu/argo-mon-status-api/pull/9) ARGO-5158 Implement UserEndpoint for user registration and profile management
- [#10](https://github.com/ARGOeu/argo-mon-status-api/pull/10) ARGO-5162 Implement Logo Upload Support for Status Pages
- [#11](https://github.com/ARGOeu/argo-mon-status-api/pull/11) ARGO-5166 Implement entitlement-based access control for Status Pages API
- [#14](https://github.com/ARGOeu/argo-mon-status-api/pull/14) ARGO-5182: Implement Project Metadata Management (CRUD) in Status Pages API
- [#24](https://github.com/ARGOeu/argo-mon-status-api/pull/24) ARGO-5178 ARGO-5179 ARGO-5177 Create/Read/Update Tenant
- [#13](https://github.com/ARGOeu/argo-mon-status-api/pull/13) ARGO-51176 Delete tenant
- [#23](https://github.com/ARGOeu/argo-mon-status-api/pull/23) ARGO-5188 Get List of Tenants
- [#31](https://github.com/ARGOeu/argo-mon-status-api/pull/31) ARGO-5199: Add search,sort and order and unique name validation for Projects
- [#33](https://github.com/ARGOeu/argo-mon-status-api/pull/33) ARGO-5174 Add Contacts to tenant
- [#34](https://github.com/ARGOeu/argo-mon-status-api/pull/34) ARGO-5206 Assign Multiple Projects to a Tenant & Tenant–Project Managment
- [#35](https://github.com/ARGOeu/argo-mon-status-api/pull/35) ARGO-5173 Add infrastracture related metadata to tenant
- [#39](https://github.com/ARGOeu/argo-mon-status-api/pull/39) ARGO-5225 Implement Keycloak AGM Group Management Integration for Tenants
- [#40](https://github.com/ARGOeu/argo-mon-status-api/pull/40) ARGO-5218 ARGO-5219 Add Tenant Status Field to Store Background Job Execution State/Implement PUT /v1/tenant/{tenantId}/status Endpoint for Updating Tenant Status #40
- [#42](https://github.com/ARGOeu/argo-mon-status-api/pull/42/) ARGO-5223: Implement tenants list endpoint with role-based filtering for admin and viewer roles
- [#44](https://github.com/ARGOeu/argo-mon-status-api/pull/44) ARGO-5229 Expose user group memberships and roles in user profile
- [#46](https://github.com/ARGOeu/argo-mon-status-api/pull/46) Design & Implement Group Membership Endpoints Using AGM
- [#48](https://github.com/ARGOeu/argo-mon-status-api/pull/48) ARGO-5245 Add Admin Endpoint to Create Tenant Group and Expose Group Status
- [#49](https://github.com/ARGOeu/argo-mon-status-api/pull/49) ARGO-5248 Protect automation endpoints
- [#50](https://github.com/ARGOeu/argo-mon-status-api/pull/50) ARGO-5263 Add execution mode for tenant jobs
- [#53](https://github.com/ARGOeu/argo-mon-status-api/pull/53) ARGO-5279 Introduce job properties for tenant status events
- [#56](https://github.com/ARGOeu/argo-mon-status-api/pull/56) ARGO-5280 Add execution mode for tenant jobs
- [#57](https://github.com/ARGOeu/argo-mon-status-api/pull/57) ARGO-5281 Tenant Invitation Management
- [#58](https://github.com/ARGOeu/argo-mon-status-api/pull/58) ARGO-5298 Include User Tenants in Users Response
- [#59](https://github.com/ARGOeu/argo-mon-status-api/pull/59) ARGO-5300 Initialize manual jobs in events for jobs in tenant's status 
- [#61](https://github.com/ARGOeu/argo-mon-status-api/pull/1)  ARGO-5304 Add init_compute_engine as a job in the status of tenant
- [#63](https://github.com/ARGOeu/argo-mon-status-api/pull/63) ARGO-5302 Add search and pagination support to admin members endpoint
- [#65](https://github.com/ARGOeu/argo-mon-status-api/pull/65) ARGO-5308 Allow super admin and admin to revoke invitations
- [#69](https://github.com/ARGOeu/argo-mon-status-api/pull/69) ARGO-5301 Mock WebApi in Dev profile #69

### Fix

- [#7](https://github.com/ARGOeu/argo-mon-status-api/pull/7) ARGO-5152: Update Keycloak Redirect URIs and Enable CORS for Local UI Development
- [#8](https://github.com/ARGOeu/argo-mon-status-api/pull/8) ARGO-5156 ARGO-MON-STATUS-API: Downgrade project postgres version to 11
- [#25](https://github.com/ARGOeu/argo-mon-status-api/pull/25) ARGO-5190 Fix website in tenant to be empty #25
- [#26](https://github.com/ARGOeu/argo-mon-status-api/pull/26) ARGO-5194 Fix the update of the tenant to not contain checks
- [#28](https://github.com/ARGOeu/argo-mon-status-api/pull/28) ARGO-5200 FIx Upload Image
- [#29](https://github.com/ARGOeu/argo-mon-status-api/pull/29) ARGO-5202 Fix leftover logo files when updating StatusPage logos and Tenant images
- [#32](https://github.com/ARGOeu/argo-mon-status-api/pull/32) ARGO-5203 ARGO-5204 Handle Database Failures/ Apply one search param to tenant and also sort, order to be defined by user
- [#37](https://github.com/ARGOeu/argo-mon-status-api/pull/37) ARGO-5210 [Status] - api Get contacts add tenant id and name
- [#41](https://github.com/ARGOeu/argo-mon-status-api/pull/41) ARGO-5236 Prevent Project deletion when project belongs to tenant
- [#47](https://github.com/ARGOeu/argo-mon-status-api/pull/47) ARGO-5244 Fix WebApi application.properties configuration
- [#51](https://github.com/ARGOeu/argo-mon-status-api/pull/51) ARGO-5276 Restrict super_admin bypass on automation endpoints
- [#52](https://github.com/ARGOeu/argo-mon-status-api/pull/52) ARGO-5278 Improve tenant tests: consistent mock tenant ID and AMS isolation
- [#54](https://github.com/ARGOeu/argo-mon-status-api/pull/54) ARGO-5282 Harmonize EventStatus, EventName enum values to always be UPPERCASE
- [#60](https://github.com/ARGOeu/argo-mon-status-api/pull/60) ARGO-5281 Tenant Invitation Management (fix)
- [#64](https://github.com/ARGOeu/argo-mon-status-api/pull/64) ARGO-5309 Permit Tenant Name with Nums & add missing manual init_monitoring_box job
- [#66](https://github.com/ARGOeu/argo-mon-status-api/pull/66) ARGO-5310 Allow Super Admins to search members by user ID
- [#67](https://github.com/ARGOeu/argo-mon-status-api/pull/67) ARGO-5314 Tenant users wiped out in web-api envrihub and aquainfra
- [#70](https://github.com/ARGOeu/argo-mon-status-api/pull/70) ARGO-5324 Enforce tenant name immutability and validate contacts list
- [#72](https://github.com/ARGOeu/argo-mon-status-api/pull/72) ARGO-5333 Prevent db_conf.database Override on Tenant Update & Consolidate Web API Update Call
- [#75](https://github.com/ARGOeu/argo-mon-status-api/pull/75) ARGO-5337 Use voperson_id as username
- [#73](https://github.com/ARGOeu/argo-mon-status-api/pull/70) ARGO-5336 Enable Trust-All TLS Configuration for argo-web-api REST Client in Dev Profile
- [#77](https://github.com/ARGOeu/argo-mon-status-api/pull/77) ARGO-5319: Make the web-api error better for users


### Removed

-[#15](https://github.com/ARGOeu/argo-mon-status-api/pull/15) ARGO-5183 Remove User DB & Update Keycloak Scope
