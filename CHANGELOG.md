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
- [#46](https://github.com/ARGOeu/argo-mon-status-api/pull/46) ARGO-5243 Design & Implement Group Membership Endpoints Using AGM
- [#48](https://github.com/ARGOeu/argo-mon-status-api/pull/48) ARGO-5245 Add Admin Endpoint to Create Tenant Group and Expose Group Status
- [#49](https://github.com/ARGOeu/argo-mon-status-api/pull/49) ARGO-5248 Protect automation endpoints
- [#50](https://github.com/ARGOeu/argo-mon-status-api/pull/50) ARGO-5263 Add execution mode for tenant jobs
- [#53](https://github.com/ARGOeu/argo-mon-status-api/pull/53) ARGO-5279 Introduce job properties for tenant status events
- [#56](https://github.com/ARGOeu/argo-mon-status-api/pull/56) ARGO-5280 Add execution mode for tenant jobs
- [#57](https://github.com/ARGOeu/argo-mon-status-api/pull/57) ARGO-5281 Tenant Invitation Management
- [#58](https://github.com/ARGOeu/argo-mon-status-api/pull/58) ARGO-5298 Include User Tenants in Users Response
- [#59](https://github.com/ARGOeu/argo-mon-status-api/pull/59) ARGO-5300 Initialize manual jobs in events for jobs in tenant's status 
- [#61](https://github.com/ARGOeu/argo-mon-status-api/pull/61) ARGO-5304 Add init_compute_engine as a job in the status of tenant
- [#63](https://github.com/ARGOeu/argo-mon-status-api/pull/63) ARGO-5302 Add search and pagination support to admin members endpoint
- [#65](https://github.com/ARGOeu/argo-mon-status-api/pull/65) ARGO-5308 Allow super admin and admin to revoke invitations
- [#69](https://github.com/ARGOeu/argo-mon-status-api/pull/69) ARGO-5301 Mock WebApi in Dev profile
- [#81](https://github.com/ARGOeu/argo-mon-status-api/pull/81) ARGO-5342 Create GET endpoints for Profiles
- [#82](https://github.com/ARGOeu/argo-mon-status-api/pull/82) ARGO-5344 Status-api support the ability to create status pages by tenant name
- [#83](https://github.com/ARGOeu/argo-mon-status-api/pull/83) ARGO-5351 Support Multiple Roles in Authorization Entitlement Checks
- [#84](https://github.com/ARGOeu/argo-mon-status-api/pull/84) ARGO-5336 check tenant readiness - quarkus api
- [#86](https://github.com/ARGOeu/argo-mon-status-api/pull/86) ARGO-5365 Add conversion from page/size parameters to first/max for pagination
- [#85](https://github.com/ARGOeu/argo-mon-status-api/pull/85) ARGO-5361 Fetch Tenant's Reports
- [#91](https://github.com/ARGOeu/argo-mon-status-api/pull/91) ARGO-5369 Exclude CHECK_READINESS from jobs list in status
- [#93](https://github.com/ARGOeu/argo-mon-status-api/pull/93) ARGO-5373 Reports order by active first
- [#94](https://github.com/ARGOeu/argo-mon-status-api/pull/94/) ARGO-5374 List tenant status pages based on tenant role
- [#95](https://github.com/ARGOeu/argo-mon-status-api/pull/95) ARGO-5378 Create seperate endpoint to notify ams about CHECK READINESS event
- [#96](https://github.com/ARGOeu/argo-mon-status-api/pull/96) ARGO-5382 Implement System Health Check Endpoint
- [#97](https://github.com/ARGOeu/argo-mon-status-api/pull/97) ARGO-5384 Re-Appear CHECK_READINESS in job list
- [#98](https://github.com/ARGOeu/argo-mon-status-api/pull/98) ARGO-5386 Change tenant admin access to endpoints
- [#102](https://github.com/ARGOeu/argo-mon-status-api/pull/102) ARGO-5396 Improve error messages in Argo Monitoring Status
- [#103](https://github.com/ARGOeu/argo-mon-status-api/pull/103) ARGO-5398 Expose uid in User Profile and Status Members responses
- [#104](https://github.com/ARGOeu/argo-mon-status-api/pull/104) ARGO-5401 Remove endpoints from Admin Resource that are common with Tenant Resource
- [#106](https://github.com/ARGOeu/argo-mon-status-api/pull/106) ARGO-5419 ARGO-5432 ARGO-5433 CRUD topology
- [#108](https://github.com/ARGOeu/argo-mon-status-api/pull/108) ARGO-5443 Support node info in tenants and set reports as default
- [#112](https://github.com/ARGOeu/argo-mon-status-api/pull/112) ARGO-5461: Sync Web API Tenants to Local DB (Dev Environment Only)
- [#115](https://github.com/ARGOeu/argo-mon-status-api/pull/115) ARGO-5477: Support force parameter when creating new topology items

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
- [#78](https://github.com/ARGOeu/argo-mon-status-api/pull/78) ARGO-5340 Improve invitation links and async group assignment on accept
- [#79](https://github.com/ARGOeu/argo-mon-status-api/pull/79) ARGO-5348 Fix report id to fetch report
- [#80](https://github.com/ARGOeu/argo-mon-status-api/pull/80) ARGO-5349: Fix add user to tenant group via invitation
- [#87](https://github.com/ARGOeu/argo-mon-status-api/pull/87) ARGO-5366 Fix @POST to @GET to fetch reports
- [#90](https://github.com/ARGOeu/argo-mon-status-api/pull/90) ARGO-5368 Fix Filter Tags to tenant's report
- [#92](https://github.com/ARGOeu/argo-mon-status-api/pull/92) ARGO-5372: List tenant status pages based on tenant role
- [#99](https://github.com/ARGOeu/argo-mon-status-api/pull/99) ARGO-5387 Improve error handling (Group Management + Invitations)
- [#100](https://github.com/ARGOeu/argo-mon-status-api/pull/100) ARGO-5391 FIX "id to load is required for loading" error when accessing tenant admin the v1/tenants/contact-types
- [#101](https://github.com/ARGOeu/argo-mon-status-api/pull/101) ARGO-5393 Improve API error message when required path parameter is missing
- [#105](https://github.com/ARGOeu/argo-mon-status-api/pull/105) ARGO-5414 Add JavaDoc descriptions to service and repository methods
- [#107](https://github.com/ARGOeu/argo-mon-status-api/pull/107) ARGO-5431: Status-api returns 502 in reports when a tenant is not fully ready
- [#109](https://github.com/ARGOeu/argo-mon-status-api/pull/109) ARGO-5446 Validate tenant initialization when x-tenant-id header is used
- [#111](https://github.com/ARGOeu/argo-mon-status-api/pull/111) ARGO-5470 DELETE topology if already exists while trying to recreate

### Removed

-[#15](https://github.com/ARGOeu/argo-mon-status-api/pull/15) ARGO-5183 Remove User DB & Update Keycloak Scope
