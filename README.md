## Changelog

### v5.2.3
    - Mollie payments

### V5.3.0
    - Migration Spring boot 2 to 3.5.10
    - ldap authentication removed
    - OpenID authentication added
    - Database MUST be postgresql 14 or higher. Sample database in docker-compose.yml file
    - maven builder removed
    - gradle builder added
    - two profiles: development and production

## profile 'development'

    bootRun --args='--spring.profiles.active=development'

Uses the internal in memory H2 database with skeletal data.sql setup.
Uses the internal database for user management.
This will setup five test users, once for each use case group:

Username and password are the same:
    magazijnmedewerker
    admins
    infobalie
    metadatabeheer
    delivery (is also in the admins group)
    guest (does not belong to any group - use case here is the OpenID login without additional roles)
    developer

Also see application-development.yml

## profile 'production'

    bootRun --args='--spring.profiles.active=production'

Will use the postgresql driver to connect to postgres.
Will use the OpenID authentication.

Use the docker-compose.yml file to spinup a database. Place a database dump in the restore folder in the root of the project

Also see application-production.yml